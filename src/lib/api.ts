import type { Chapter, ChooseResult, Game, GameChapter, GameChoice, History, News, Profile, StoryData } from './types';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`/api/${path}`, {
      method,
      credentials: 'same-origin',
      headers: {
        // Cabecera propia: el servidor rechaza cambios que no vengan de la app (CSRF)
        'X-Requested-With': 'bellas-heart',
        ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'No hay conexión. Comprueba tu internet e inténtalo de nuevo.');
  }
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new ApiError(response.status, (data as { error?: string } | null)?.error ?? 'Ha ocurrido un error inesperado.');
  return data as T;
}

/** Mensaje de error comprensible para mostrar en pantalla. */
export function friendlyError(error: unknown): string {
  if (error instanceof ApiError || error instanceof Error) return error.message;
  return 'Ha ocurrido un error inesperado.';
}

export type GameWithTitle = Game & { history_title: string };
export type ContinueGame = GameWithTitle & { chapter_name: string | null; chapter_number: string | null; background_image: string | null };
export type PlayerSummary = Profile & { email: string; games: number };

export const api = {
  // Cuentas
  me: () => request<{ user: Profile | null }>('GET', 'auth/me'),
  register: (data: { email: string; password: string; username: string; first_name: string; last_name: string }) =>
    request<{ user: Profile }>('POST', 'auth/register', data),
  login: (email: string, password: string) => request<{ user: Profile }>('POST', 'auth/login', { email, password }),
  logout: () => request<{ ok: true }>('POST', 'auth/logout', {}),
  updateProfile: (data: { username: string; first_name: string; last_name: string; avatar: string }) =>
    request<{ user: Profile }>('PATCH', 'me', data),
  changePassword: (current_password: string, new_password: string) =>
    request<{ ok: true }>('POST', 'me/password', { current_password, new_password }),
  deleteAccount: (password: string) => request<{ ok: true }>('DELETE', 'me', { password }),

  // Historias
  histories: () => request<History[]>('GET', 'histories'),
  history: (id: number) => request<{ history: History; chapters: number }>('GET', `histories/${id}`),
  story: (historyId: number | null) => request<StoryData>('GET', `story/${historyId ?? 'tutorial'}`),

  // Partidas
  games: () => request<GameWithTitle[]>('GET', 'games'),
  continueGame: () => request<ContinueGame | null>('GET', 'games/continue'),
  createGame: (history_id: number, name: string) => request<{ id: number }>('POST', 'games', { history_id, name }),
  game: (id: number) => request<{ game: Game; chapters: Chapter[]; progress: GameChapter[] }>('GET', `games/${id}`),
  deleteGame: (id: number) => request<{ ok: true }>('DELETE', `games/${id}`, {}),
  choose: (gameId: number, optionId: number) => request<ChooseResult>('POST', `games/${gameId}/choose`, { option_id: optionId }),
  choices: (gameId: number) => request<{ game: Game; choices: GameChoice[] }>('GET', `games/${gameId}/choices`),

  // Noticias
  news: () => request<News[]>('GET', 'news'),
  newsItem: (id: number) => request<News>('GET', `news/${id}`),

  // Administración
  createNews: (title: string, body: string) => request<News>('POST', 'admin/news', { title, body }),
  updateNews: (id: number, title: string, body: string) => request<News>('PATCH', `admin/news/${id}`, { title, body }),
  deleteNews: (id: number) => request<{ ok: true }>('DELETE', `admin/news/${id}`, {}),
  players: () => request<PlayerSummary[]>('GET', 'admin/players'),
  player: (id: string) => request<{ player: Profile & { email: string }; games: Game[] }>('GET', `admin/players/${id}`),
  deletePlayer: (id: string) => request<{ ok: true }>('DELETE', `admin/players/${id}`, {}),
};
