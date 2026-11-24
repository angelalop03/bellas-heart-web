import { createClient } from '@supabase/supabase-js';

const url = import.meta.env.VITE_SUPABASE_URL as string | undefined;
const key = import.meta.env.VITE_SUPABASE_ANON_KEY as string | undefined;

export const supabaseConfigured = Boolean(url && key);

export const supabase = createClient(url ?? 'http://localhost', key ?? 'missing-key', {
  auth: { persistSession: true, autoRefreshToken: true, detectSessionInUrl: true },
});

/** Traduce los errores de Supabase a mensajes comprensibles. */
export function friendlyError(error: unknown): string {
  const message = (error as { message?: string } | null)?.message ?? String(error ?? '');
  const map: [RegExp, string][] = [
    [/invalid login credentials/i, 'El email o la contraseña no son correctos.'],
    [/email not confirmed/i, 'Confirma tu email antes de entrar: revisa tu bandeja de entrada.'],
    [/user already registered/i, 'Ya existe una cuenta con ese email.'],
    [/password should be at least/i, 'La contraseña debe tener al menos 6 caracteres.'],
    [/rate limit|too many requests/i, 'Demasiados intentos. Espera un momento y vuelve a probar.'],
    [/failed to fetch|network/i, 'No hay conexión. Comprueba tu internet e inténtalo de nuevo.'],
    [/duplicate key.*username|profiles_username_key/i, 'Ese nombre de usuario ya está en uso.'],
    [/new password should be different/i, 'La nueva contraseña debe ser distinta de la actual.'],
  ];
  for (const [re, text] of map) if (re.test(message)) return text;
  return message || 'Ha ocurrido un error inesperado.';
}
