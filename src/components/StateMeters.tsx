import { HeartCrack, Brain } from 'lucide-react';
import { meterPercent, PSYCH_INFO, psychState, RELATION_INFO, relationState } from '../lib/states';

/**
 * Cómo está Bella y cómo está la relación. Más relleno = peor
 * (en el juego original, los puntos suman malestar y tensión).
 */
export function StateMeters({ pointsM, pointsR, compact }: { pointsM: number; pointsR: number; compact?: boolean }) {
  const psych = PSYCH_INFO[psychState(pointsM)];
  const rel = RELATION_INFO[relationState(pointsR)];
  const items = [
    { key: 'm', icon: <Brain size={compact ? 15 : 18} />, title: 'Bella', info: psych, value: meterPercent(pointsM) },
    { key: 'r', icon: <HeartCrack size={compact ? 15 : 18} />, title: 'Relación', info: rel, value: meterPercent(pointsR) },
  ];
  return (
    <div className={`meters ${compact ? 'meters-compact' : ''}`}>
      {items.map((item) => (
        <div
          className="meter"
          key={item.key}
          role="meter"
          aria-label={`${item.title}: ${item.info.label}`}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={item.value}
        >
          <div className="meter-head">
            <span className="meter-title">
              {item.icon} {item.title}
            </span>
            <span className="meter-label" style={{ color: item.info.color }}>
              {item.info.label}
            </span>
          </div>
          <div className="meter-track">
            <div className="meter-fill" style={{ width: `${Math.max(item.value, 3)}%`, background: item.info.color }} />
          </div>
        </div>
      ))}
    </div>
  );
}
