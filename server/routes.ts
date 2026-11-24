import { query, queryOne } from './db.js';
import {
  checkLogin,
  clearCookie,
  getUser,
  hashPassword,
  publicUser,
  requireAdmin,
  requireUser,
  sessionCookie,
  verifyPassword,
  type UserRow,
} from './auth.js';
import { HttpError, json, readJson, text, toId } from './http.js';

type Handler = (request: Request, params: Record<string, string>) => Promise<Response>;

const AVATARS = ['default_image', 'Perfil_2', 'Perfil_3', 'Perfil_4'];
const USERNAME_RE = /^[\p{L}\p{N}_.-]{3,30}$/u;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD = 8;

function checkPassword(password: string) {
  if (password.length < MIN_PASSWORD) throw new HttpError(400, `La contraseña debe tener al menos ${MIN_PASSWORD} caracteres.`);
  if (password.length > 72) throw new HttpError(400, 'La contraseña es demasiado larga.');
}

function checkUsername(username: string) {
  if (!USERNAME_RE.test(username)) {
    throw new HttpError(400, 'El nombre de usuario debe tener entre 3 y 30 letras, números, puntos o guiones.');
  }
}

async function usernameTaken(username: string, exceptId?: string): Promise<boolean> {
  const row = await queryOne('select 1 from users where lower(username) = lower($1) and id is distinct from $2', [username, exceptId ?? null]);
  return Boolean(row);
}

/** Partida de la usuaria (o de cualquiera si es admin). */
async function loadGame(user: UserRow, id: number, allowAdmin = true) {
  const game = await queryOne<{ user_id: string } & Record<string, unknown>>('select * from games where id = $1', [id]);
  if (!game || (game.user_id !== user.id && !(allowAdmin && user.role === 'admin'))) {
    throw new HttpError(404, 'No encontramos esta partida');
  }
  return game;
}

const routes: [string, string, Handler][] = [
  // ---------------- Cuentas ----------------
  ['GET', 'auth/me', async (request) => {
    const user = await getUser(request);
    return json({ user: user ? publicUser(user) : null });
  }],

  ['POST', 'auth/register', async (request) => {
    const body = await readJson(request);
    const email = text(body.email, 254).toLowerCase();
    const password = typeof body.password === 'string' ? body.password : '';
    const username = text(body.username, 30);
    if (!EMAIL_RE.test(email)) throw new HttpError(400, 'Escribe un email válido.');
    checkPassword(password);
    checkUsername(username);
    if (await queryOne('select 1 from users where email = $1', [email])) {
      throw new HttpError(409, 'Ya existe una cuenta con ese email.');
    }
    if (await usernameTaken(username)) throw new HttpError(409, 'Ese nombre de usuario ya está en uso.');
    const user = await queryOne<UserRow>(
      `insert into users (email, password_hash, username, first_name, last_name)
       values ($1, $2, $3, $4, $5) returning *`,
      [email, await hashPassword(password), username, text(body.first_name, 60), text(body.last_name, 80)],
    );
    return json({ user: publicUser(user!) }, { status: 201, headers: { 'Set-Cookie': await sessionCookie(request, user!) } });
  }],

  ['POST', 'auth/login', async (request) => {
    const body = await readJson(request);
    const email = text(body.email, 254).toLowerCase();
    const password = typeof body.password === 'string' ? body.password : '';
    const user = await checkLogin(email, password);
    return json({ user: publicUser(user) }, { headers: { 'Set-Cookie': await sessionCookie(request, user) } });
  }],

  ['POST', 'auth/logout', async (request) => json({ ok: true }, { headers: { 'Set-Cookie': clearCookie(request) } })],

  ['PATCH', 'me', async (request) => {
    const user = await requireUser(request);
    const body = await readJson(request);
    const username = text(body.username, 30) || user.username;
    checkUsername(username);
    if (await usernameTaken(username, user.id)) throw new HttpError(409, 'Ese nombre de usuario ya está en uso.');
    const avatar = AVATARS.includes(String(body.avatar)) ? String(body.avatar) : user.avatar;
    const updated = await queryOne<UserRow>(
      'update users set username = $2, first_name = $3, last_name = $4, avatar = $5 where id = $1 returning *',
      [user.id, username, text(body.first_name, 60), text(body.last_name, 80), avatar],
    );
    return json({ user: publicUser(updated!) });
  }],

  ['POST', 'me/password', async (request) => {
    const user = await requireUser(request);
    const body = await readJson(request);
    const current = typeof body.current_password === 'string' ? body.current_password : '';
    const next = typeof body.new_password === 'string' ? body.new_password : '';
    if (!(await verifyPassword(current, user.password_hash))) throw new HttpError(400, 'La contraseña actual no es correcta.');
    checkPassword(next);
    // Subir token_version cierra las sesiones abiertas en otros dispositivos
    const updated = await queryOne<UserRow>(
      'update users set password_hash = $2, token_version = token_version + 1 where id = $1 returning *',
      [user.id, await hashPassword(next)],
    );
    return json({ ok: true }, { headers: { 'Set-Cookie': await sessionCookie(request, updated!) } });
  }],

  ['DELETE', 'me', async (request) => {
    const user = await requireUser(request);
    const body = await readJson(request);
    const password = typeof body.password === 'string' ? body.password : '';
    if (!(await verifyPassword(password, user.password_hash))) throw new HttpError(400, 'La contraseña no es correcta.');
    await query('delete from users where id = $1', [user.id]);
    return json({ ok: true }, { headers: { 'Set-Cookie': clearCookie(request) } });
  }],

  // ---------------- Historias ----------------
  ['GET', 'histories', async () => {
    const rows = await query('select * from histories where published order by id');
    return json(rows);
  }],

  ['GET', 'histories/:id', async (request, { id }) => {
    const user = await getUser(request);
    const history = await queryOne<{ published: boolean }>('select * from histories where id = $1', [toId(id)]);
    if (!history || (!history.published && user?.role !== 'admin')) throw new HttpError(404, 'Esta historia no está disponible.');
    const [{ count }] = await query<{ count: number }>('select count(*)::int as count from chapters where history_id = $1', [toId(id)]);
    return json({ history, chapters: count });
  }],

  ['GET', 'story/:id', async (request, { id }) => {
    let historyId: number | null = null;
    let isPublic = true;
    if (id !== 'tutorial') {
      historyId = toId(id);
      const user = await getUser(request);
      const history = await queryOne<{ published: boolean }>('select published from histories where id = $1', [historyId]);
      if (!history || (!history.published && user?.role !== 'admin')) throw new HttpError(404, 'Esta historia no está disponible.');
      isPublic = history.published;
    }
    const row = await queryOne<{ story: unknown }>('select get_story($1) as story', [historyId]);
    // Las historias publicadas se cachean en la CDN de Vercel; las no publicadas (solo admin), nunca
    const cache = isPublic ? 'public, max-age=300, s-maxage=600, stale-while-revalidate=86400' : 'private, no-store';
    return json(row!.story, { headers: { 'Cache-Control': cache } });
  }],

  // ---------------- Partidas ----------------
  ['GET', 'games', async (request) => {
    const user = await requireUser(request);
    const rows = await query(
      `select g.*, h.title as history_title from games g join histories h on h.id = g.history_id
       where g.user_id = $1 order by g.updated_at desc`,
      [user.id],
    );
    return json(rows);
  }],

  ['GET', 'games/continue', async (request) => {
    const user = await requireUser(request);
    const row = await queryOne(
      `select g.*, h.title as history_title, c.name as chapter_name, c.chapter_number, s.background_image
         from games g
         join histories h on h.id = g.history_id
         left join chapters c on c.id = g.current_chapter_id
         left join scenes s on s.id = g.current_scene_id
        where g.user_id = $1 and g.state = 'IN_PROGRESS'
        order by g.updated_at desc limit 1`,
      [user.id],
    );
    return json(row);
  }],

  ['POST', 'games', async (request) => {
    const user = await requireUser(request);
    const body = await readJson(request);
    const row = await queryOne<{ id: number }>('select create_game($1, $2, $3) as id', [
      user.id,
      Number(body.history_id),
      text(body.name, 40),
    ]);
    return json({ id: row!.id }, { status: 201 });
  }],

  ['GET', 'games/:id', async (request, { id }) => {
    const user = await requireUser(request);
    const game = await loadGame(user, toId(id));
    const [chapters, progress] = await Promise.all([
      query('select * from chapters where history_id = $1 order by chapter_sort_key(chapter_number), id', [game.history_id]),
      query('select * from game_chapters where game_id = $1', [game.id]),
    ]);
    return json({ game, chapters, progress });
  }],

  ['DELETE', 'games/:id', async (request, { id }) => {
    const user = await requireUser(request);
    const game = await loadGame(user, toId(id), false);
    await query('delete from games where id = $1', [game.id]);
    return json({ ok: true });
  }],

  ['POST', 'games/:id/choose', async (request, { id }) => {
    const user = await requireUser(request);
    const body = await readJson(request);
    const row = await queryOne<{ result: unknown }>('select choose_option($1, $2, $3) as result', [
      user.id,
      toId(id),
      Number(body.option_id),
    ]);
    return json(row!.result);
  }],

  ['GET', 'games/:id/choices', async (request, { id }) => {
    const user = await requireUser(request);
    const game = await loadGame(user, toId(id));
    const choices = await query('select * from game_choices where game_id = $1 order by id', [game.id]);
    return json({ game, choices });
  }],

  // ---------------- Noticias ----------------
  ['GET', 'news', async () => json(await query('select * from news order by created_at desc, id desc'))],

  ['GET', 'news/:id', async (_request, { id }) => {
    const item = await queryOne('select * from news where id = $1', [toId(id)]);
    if (!item) throw new HttpError(404, 'Esta noticia ya no existe');
    return json(item);
  }],

  // ---------------- Administración ----------------
  ['POST', 'admin/news', async (request) => {
    await requireAdmin(request);
    const body = await readJson(request);
    const title = text(body.title, 300);
    const content = text(body.body, 20000);
    if (!title || !content) throw new HttpError(400, 'Escribe un título y un texto.');
    return json(await queryOne('insert into news (title, body) values ($1, $2) returning *', [title, content]), { status: 201 });
  }],

  ['PATCH', 'admin/news/:id', async (request, { id }) => {
    await requireAdmin(request);
    const body = await readJson(request);
    const title = text(body.title, 300);
    const content = text(body.body, 20000);
    if (!title || !content) throw new HttpError(400, 'Escribe un título y un texto.');
    const item = await queryOne('update news set title = $2, body = $3 where id = $1 returning *', [toId(id), title, content]);
    if (!item) throw new HttpError(404, 'Esta noticia ya no existe');
    return json(item);
  }],

  ['DELETE', 'admin/news/:id', async (request, { id }) => {
    await requireAdmin(request);
    await query('delete from news where id = $1', [toId(id)]);
    return json({ ok: true });
  }],

  ['GET', 'admin/players', async (request) => {
    await requireAdmin(request);
    const rows = await query(
      `select u.id, u.email, u.username, u.first_name, u.last_name, u.avatar, u.role, u.created_at,
              (select count(*)::int from games g where g.user_id = u.id) as games
         from users u order by u.created_at desc`,
    );
    return json(rows);
  }],

  ['GET', 'admin/players/:id', async (request, { id }) => {
    await requireAdmin(request);
    const player = await queryOne<UserRow>('select * from users where id = $1', [id]).catch(() => null);
    if (!player) throw new HttpError(404, 'No encontramos a esta jugadora');
    const games = await query('select * from games where user_id = $1 order by updated_at desc', [player.id]);
    return json({ player: publicUser(player), games });
  }],

  ['DELETE', 'admin/players/:id', async (request, { id }) => {
    const admin = await requireAdmin(request);
    if (id === admin.id) throw new HttpError(400, 'No puedes borrar tu propia cuenta desde aquí.');
    await query('delete from users where id = $1', [id]).catch(() => undefined);
    return json({ ok: true });
  }],
];

function match(pattern: string, path: string): Record<string, string> | null {
  const a = pattern.split('/');
  const b = path.split('/');
  if (a.length !== b.length) return null;
  const params: Record<string, string> = {};
  for (let i = 0; i < a.length; i++) {
    if (a[i].startsWith(':')) params[a[i].slice(1)] = decodeURIComponent(b[i]);
    else if (a[i] !== b[i]) return null;
  }
  return params;
}

/** Punto de entrada de toda la API: /api/<ruta>. */
export async function handle(request: Request): Promise<Response> {
  const url = new URL(request.url);
  // En Vercel llega como /api?route=games/3 (ver vercel.json); en local como /api/games/3
  const path = (url.searchParams.get('route') ?? url.pathname.replace(/^\/api\/?/, '')).replace(/^\/+|\/+$/g, '');
  const method = request.method.toUpperCase();

  try {
    // Protección CSRF: las peticiones que cambian datos deben venir de la propia app
    if (method !== 'GET' && request.headers.get('x-requested-with') !== 'bellas-heart') {
      throw new HttpError(403, 'Petición no permitida');
    }
    // "games/continue" va antes que "games/:id" en la tabla, así que gana
    for (const [m, pattern, handler] of routes) {
      if (m !== method) continue;
      const params = match(pattern, path);
      if (params) return await handler(request, params);
    }
    throw new HttpError(404, 'Ruta no encontrada');
  } catch (error) {
    if (error instanceof HttpError) return json({ error: error.message }, { status: error.status });
    const pgError = error as { code?: string; message?: string };
    // Errores de las funciones SQL (raise exception) => mensaje para la jugadora
    if (pgError.code === 'P0001') return json({ error: pgError.message }, { status: 400 });
    if (pgError.code === '23505') return json({ error: 'Ese nombre ya está en uso.' }, { status: 409 });
    console.error('[api]', method, path, error);
    return json({ error: 'Ha ocurrido un error en el servidor. Inténtalo de nuevo.' }, { status: 500 });
  }
}
