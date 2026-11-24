import bcrypt from 'bcryptjs';
import { SignJWT, jwtVerify } from 'jose';
import { query, queryOne } from './db.js';
import { HttpError, parseCookies } from './http.js';

export interface UserRow {
  id: string;
  email: string;
  password_hash: string;
  username: string;
  first_name: string;
  last_name: string;
  avatar: string;
  role: 'player' | 'admin';
  token_version: number;
  created_at: string;
}

export type PublicUser = Omit<UserRow, 'password_hash' | 'token_version'>;

const COOKIE = 'bh_session';
const MAX_AGE = 60 * 60 * 24 * 30; // 30 días
const MAX_FAILED_LOGINS = 8;

let secret: Uint8Array | null = null;

/** La clave para firmar sesiones se genera en la base de datos al crearla (ver 001_schema.sql). */
async function getSecret(): Promise<Uint8Array> {
  if (!secret) {
    const row = await queryOne<{ value: string }>("select value from app_settings where key = 'session_secret'");
    if (!row) throw new Error('Falta session_secret en app_settings');
    secret = new TextEncoder().encode(row.value);
  }
  return secret;
}

export function publicUser(user: UserRow): PublicUser {
  const { password_hash: _hash, token_version: _version, ...rest } = user;
  return rest;
}

export function hashPassword(password: string): Promise<string> {
  return bcrypt.hash(password, 10);
}

export function verifyPassword(password: string, hash: string): Promise<boolean> {
  return bcrypt.compare(password, hash);
}

function isSecure(request: Request): boolean {
  return new URL(request.url).protocol === 'https:' || request.headers.get('x-forwarded-proto') === 'https';
}

export async function sessionCookie(request: Request, user: UserRow): Promise<string> {
  const token = await new SignJWT({ v: user.token_version })
    .setProtectedHeader({ alg: 'HS256' })
    .setSubject(user.id)
    .setIssuedAt()
    .setExpirationTime(`${MAX_AGE}s`)
    .sign(await getSecret());
  return `${COOKIE}=${token}; Path=/; HttpOnly; SameSite=Lax; Max-Age=${MAX_AGE}${isSecure(request) ? '; Secure' : ''}`;
}

export function clearCookie(request: Request): string {
  return `${COOKIE}=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0${isSecure(request) ? '; Secure' : ''}`;
}

/** Usuaria de la sesión, o null. Comprueba que la cuenta sigue existiendo y la versión del token. */
export async function getUser(request: Request): Promise<UserRow | null> {
  const token = parseCookies(request.headers.get('cookie'))[COOKIE];
  if (!token) return null;
  try {
    const { payload } = await jwtVerify(token, await getSecret(), { algorithms: ['HS256'] });
    if (!payload.sub) return null;
    const user = await queryOne<UserRow>('select * from users where id = $1', [payload.sub]);
    if (!user || user.token_version !== payload.v) return null;
    return user;
  } catch {
    return null;
  }
}

export async function requireUser(request: Request): Promise<UserRow> {
  const user = await getUser(request);
  if (!user) throw new HttpError(401, 'Necesitas iniciar sesión');
  return user;
}

export async function requireAdmin(request: Request): Promise<UserRow> {
  const user = await requireUser(request);
  if (user.role !== 'admin') throw new HttpError(403, 'Solo administración');
  return user;
}

// Hash de relleno para que comprobar un email inexistente tarde lo mismo que uno real
let dummyHash: string | null = null;
async function getDummyHash(): Promise<string> {
  dummyHash ??= await bcrypt.hash('bellas-heart-dummy', 10);
  return dummyHash;
}

export async function checkLogin(email: string, password: string): Promise<UserRow> {
  const [{ count }] = await query<{ count: number }>(
    "select count(*)::int as count from login_attempts where email = $1 and created_at > now() - interval '15 minutes'",
    [email],
  );
  if (count >= MAX_FAILED_LOGINS) {
    throw new HttpError(429, 'Demasiados intentos. Espera unos minutos y vuelve a probar.');
  }
  const user = await queryOne<UserRow>('select * from users where email = $1', [email]);
  const ok = await verifyPassword(password, user?.password_hash ?? (await getDummyHash()));
  if (!user || !ok) {
    await query('insert into login_attempts (email) values ($1)', [email]);
    await query("delete from login_attempts where created_at < now() - interval '1 day'");
    throw new HttpError(401, 'El email o la contraseña no son correctos.');
  }
  await query('delete from login_attempts where email = $1', [email]);
  return user;
}
