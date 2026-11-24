import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

export function Spinner({ label }: { label?: string }) {
  return (
    <div className="center-screen" role="status" aria-live="polite">
      <div style={{ display: 'grid', justifyItems: 'center', gap: 12 }}>
        <div className="spinner" />
        {label && <p className="muted">{label}</p>}
      </div>
    </div>
  );
}

/** Ilustración del juego a pantalla completa detrás del contenido. */
export function Backdrop({ src }: { src: string }) {
  return <div className="scenic" aria-hidden="true" style={{ backgroundImage: `url("${src}")` }} />;
}

export function PageHead({ title, back, action }: { title: string; back?: string | number; action?: ReactNode }) {
  const navigate = useNavigate();
  return (
    <header className="page-head">
      {back !== undefined && (
        <button
          type="button"
          className="btn btn-icon btn-ghost"
          aria-label="Volver"
          onClick={() => (typeof back === 'number' ? navigate(back) : navigate(back))}
        >
          <ArrowLeft size={24} />
        </button>
      )}
      <h1 style={{ flex: 1 }}>{title}</h1>
      {action}
    </header>
  );
}

export function Empty({ icon, title, children }: { icon?: ReactNode; title: string; children?: ReactNode }) {
  return (
    <div className="card empty">
      {icon}
      <p className="title-md">{title}</p>
      {children}
    </div>
  );
}

export function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: ReactNode }) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    ref.current?.querySelector<HTMLElement>('input, textarea, button')?.focus();
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);
  return (
    <div className="modal-backdrop" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="glass modal" role="dialog" aria-modal="true" aria-label={title} ref={ref}>
        <h2 className="title-md" style={{ fontSize: '1.25rem' }}>{title}</h2>
        {children}
      </div>
    </div>
  );
}

export function ConfirmDialog({
  title,
  message,
  confirmLabel = 'Confirmar',
  danger,
  busy,
  onConfirm,
  onCancel,
  children,
}: {
  title: string;
  message?: ReactNode;
  confirmLabel?: string;
  danger?: boolean;
  busy?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  children?: ReactNode;
}) {
  return (
    <Modal title={title} onClose={onCancel}>
      {message && <p className="muted">{message}</p>}
      {children}
      <div className="row" style={{ justifyContent: 'flex-end' }}>
        <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={busy}>
          Cancelar
        </button>
        <button type="button" className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`} onClick={onConfirm} disabled={busy}>
          {busy ? 'Un momento…' : confirmLabel}
        </button>
      </div>
    </Modal>
  );
}

const ToastContext = createContext<(message: string) => void>(() => undefined);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [message, setMessage] = useState<string | null>(null);
  const timer = useRef<number | undefined>(undefined);
  const show = useCallback((text: string) => {
    setMessage(text);
    window.clearTimeout(timer.current);
    timer.current = window.setTimeout(() => setMessage(null), 2800);
  }, []);
  return (
    <ToastContext.Provider value={show}>
      {children}
      {message && (
        <div className="toast" role="status" aria-live="polite">
          {message}
        </div>
      )}
    </ToastContext.Provider>
  );
}

export const useToast = () => useContext(ToastContext);

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', year: 'numeric' });
}
