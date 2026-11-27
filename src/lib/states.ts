import type { PsychState, RelationState } from './types';

/**
 * Puntos en los que cambia el estado. Deben coincidir con las funciones SQL
 * psychological_state y relationship_state (db/migrations/003_puntuacion.sql).
 */
export const PSYCH_THRESHOLDS = [12, 25, 38];
export const RELATION_THRESHOLDS = [19, 24, 29];

export function psychState(points: number): PsychState {
  const p = Math.max(points, 0);
  const [a, b, c] = PSYCH_THRESHOLDS;
  if (p < a) return 'STABLE';
  if (p < b) return 'SLIGHTLY_ANXIOUS';
  if (p < c) return 'ANXIOUS';
  return 'DISTRESSED';
}

export function relationState(points: number): RelationState {
  const p = Math.max(points, 0);
  const [a, b, c] = RELATION_THRESHOLDS;
  if (p < a) return 'CONFIDENT';
  if (p < b) return 'LOW_TENSION';
  if (p < c) return 'HIGH_TENSION';
  return 'CRITICAL';
}

interface StateInfo {
  label: string;
  short: string;
  color: string;
  level: number; // 0..3
}

// Escala de color de la app (lavanda -> rosa -> frambuesa): cuanto más intenso, peor.
const LEVEL_COLORS = ['#c9b3ff', '#f2a5d6', '#f0659f', '#ff4f74'];

export const PSYCH_INFO: Record<PsychState, StateInfo> = {
  STABLE: { label: 'Estable', short: 'Estable', color: LEVEL_COLORS[0], level: 0 },
  SLIGHTLY_ANXIOUS: { label: 'Algo inquieta', short: 'Inquieta', color: LEVEL_COLORS[1], level: 1 },
  ANXIOUS: { label: 'Ansiosa', short: 'Ansiosa', color: LEVEL_COLORS[2], level: 2 },
  DISTRESSED: { label: 'Angustiada', short: 'Angustiada', color: LEVEL_COLORS[3], level: 3 },
};

export const RELATION_INFO: Record<RelationState, StateInfo> = {
  CONFIDENT: { label: 'Confianza', short: 'Confianza', color: LEVEL_COLORS[0], level: 0 },
  LOW_TENSION: { label: 'Tensión leve', short: 'Tensión leve', color: LEVEL_COLORS[1], level: 1 },
  HIGH_TENSION: { label: 'Tensión alta', short: 'Tensión alta', color: LEVEL_COLORS[2], level: 2 },
  CRITICAL: { label: 'Crítica', short: 'Crítica', color: LEVEL_COLORS[3], level: 3 },
};

/** En la barra, cada estado ocupa una cuarta parte: las marcas quedan al 25 %, 50 % y 75 %. */
export const METER_TICKS = [25, 50, 75];

/**
 * Porcentaje 0..100 de la barra. Se reparte por tramos según los umbrales, así la barra
 * refleja el estado aunque los puntos sean bajos; el último tramo mide lo mismo que el anterior.
 */
export function meterPercent(points: number, thresholds: number[]): number {
  const [a, b, c] = thresholds;
  const edges = [0, a, b, c, c + (c - b)];
  const p = Math.max(0, points);
  for (let i = 0; i < 4; i++) {
    if (p < edges[i + 1]) return 25 * i + (25 * (p - edges[i])) / (edges[i + 1] - edges[i]);
  }
  return 100;
}

/** Malestar y tensión siempre entre 0 y 100, igual que en la base de datos. */
export function clampPoints(points: number): number {
  return Math.min(100, Math.max(0, points));
}
