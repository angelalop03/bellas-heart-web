import type { PsychState, RelationState } from './types';

// Mismos umbrales que el juego original (y que las funciones SQL).
export function psychState(points: number): PsychState {
  const p = Math.max(points, 0);
  if (p < 25) return 'STABLE';
  if (p < 50) return 'SLIGHTLY_ANXIOUS';
  if (p < 75) return 'ANXIOUS';
  return 'DISTRESSED';
}

export function relationState(points: number): RelationState {
  const p = Math.max(points, 0);
  if (p < 30) return 'CONFIDENT';
  if (p < 60) return 'LOW_TENSION';
  if (p < 80) return 'HIGH_TENSION';
  return 'CRITICAL';
}

interface StateInfo {
  label: string;
  short: string;
  color: string;
  level: number; // 0..3
}

export const PSYCH_INFO: Record<PsychState, StateInfo> = {
  STABLE: { label: 'Estable', short: 'Estable', color: '#6fd6a8', level: 0 },
  SLIGHTLY_ANXIOUS: { label: 'Algo inquieta', short: 'Inquieta', color: '#f7c66b', level: 1 },
  ANXIOUS: { label: 'Ansiosa', short: 'Ansiosa', color: '#ff9f6b', level: 2 },
  DISTRESSED: { label: 'Angustiada', short: 'Angustiada', color: '#ff6b81', level: 3 },
};

export const RELATION_INFO: Record<RelationState, StateInfo> = {
  CONFIDENT: { label: 'Confianza', short: 'Confianza', color: '#6fd6a8', level: 0 },
  LOW_TENSION: { label: 'Tensión leve', short: 'Tensión leve', color: '#f7c66b', level: 1 },
  HIGH_TENSION: { label: 'Tensión alta', short: 'Tensión alta', color: '#ff9f6b', level: 2 },
  CRITICAL: { label: 'Crítica', short: 'Crítica', color: '#ff6b81', level: 3 },
};

/** Porcentaje 0..100 para las barras (los puntos del juego van de 0 a ~100). */
export function meterPercent(points: number): number {
  return Math.min(100, Math.max(0, points));
}
