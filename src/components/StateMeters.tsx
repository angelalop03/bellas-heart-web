import { HeartCrack, Brain } from 'lucide-react';
import {
  METER_TICKS,
  meterPercent,
  PSYCH_INFO,
  PSYCH_THRESHOLDS,
  psychState,
  RELATION_INFO,
  RELATION_THRESHOLDS,
  relationState,
} from '../lib/states';

/**
 * Malestar de Bella y tensión con David. Más relleno = más malestar o más choque.
 * La tensión sube también cuando Bella pone límites: no es «peor» en sí misma.
 * El relleno va de lavanda a frambuesa y las marcas indican dónde cambia el estado.
 */
export function StateMeters({ pointsM, pointsR, compact }: { pointsM: number; pointsR: number; compact?: boolean }) {
  const psych = PSYCH_INFO[psychState(pointsM)];
  const rel = RELATION_INFO[relationState(pointsR)];
  const items = [
    { key: 'm', icon: <Brain size={compact ? 14 : 17} />, title: 'Bella', info: psych, value: meterPercent(pointsM, PSYCH_THRESHOLDS) },
    {
      key: 'r',
      icon: <HeartCrack size={compact ? 14 : 17} />,
      title: compact ? 'Tensión' : 'Tensión con David',
      info: rel,
      value: meterPercent(pointsR, RELATION_THRESHOLDS),
    },
  ];
  return (
    <div className={`meters ${compact ? 'meters-compact' : ''}`}>
      {items.map((item) => {
        const width = Math.max(item.value, 4);
        return (
          <div
            className="meter"
            key={item.key}
            role="meter"
            aria-label={`${item.title}: ${item.info.label}`}
            aria-valuemin={0}
            aria-valuemax={100}
            aria-valuenow={Math.round(item.value)}
          >
            <div className="meter-head">
              <span className="meter-title">
                {item.icon} {item.title}
              </span>
              <span
                className="meter-label"
                style={{ color: item.info.color, borderColor: `${item.info.color}66`, background: `${item.info.color}1f` }}
              >
                {item.info.label}
              </span>
            </div>
            <div className="meter-track">
              <div
                className="meter-fill"
                style={{
                  width: `${width}%`,
                  // El degradado ocupa toda la pista, así el color del extremo indica la intensidad
                  backgroundSize: `${(100 / width) * 100}% 100%`,
                  boxShadow: `0 0 12px ${item.info.color}80`,
                }}
              />
              {METER_TICKS.map((t) => (
                <span key={t} className="meter-tick" style={{ left: `${t}%` }} aria-hidden="true" />
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
