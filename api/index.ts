// Función de Vercel que atiende toda la API (/api/*, ver vercel.json).
import { handle } from '../server/routes.js';

export const GET = handle;
export const POST = handle;
export const PATCH = handle;
export const DELETE = handle;
