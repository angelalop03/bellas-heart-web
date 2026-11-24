import { useState, type FormEvent, type ReactNode } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { api, friendlyError } from '../lib/api';
import { useAuth } from '../lib/auth';
import { Backdrop, PageHead } from '../components/ui';
import { BACKDROPS } from '../lib/assets';

export const MIN_PASSWORD = 8;

function AuthLayout({ title, children, back = '/' }: { title: string; children: ReactNode; back?: string }) {
  return (
    <div className="auth">
      <Backdrop src={BACKDROPS.night} />
      <div className="auth-inner">
        <PageHead title={title} back={back} />
        <div className="glass auth-card">{children}</div>
      </div>
    </div>
  );
}

export function PasswordInput({
  id,
  value,
  onChange,
  autoComplete,
  placeholder,
  minLength = MIN_PASSWORD,
}: {
  id: string;
  value: string;
  onChange: (v: string) => void;
  autoComplete: string;
  placeholder?: string;
  minLength?: number;
}) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="password-wrap">
      <input
        id={id}
        className="input"
        type={visible ? 'text' : 'password'}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        autoComplete={autoComplete}
        placeholder={placeholder}
        minLength={minLength}
        maxLength={72}
        required
      />
      <button
        type="button"
        className="password-toggle"
        onClick={() => setVisible((v) => !v)}
        aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
      >
        {visible ? <EyeOff size={20} /> : <Eye size={20} />}
      </button>
    </div>
  );
}

export function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setUser } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const { user } = await api.login(email.trim(), password);
      setUser(user);
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from && from !== '/entrar' ? from : '/inicio', { replace: true });
    } catch (err) {
      setError(friendlyError(err));
      setBusy(false);
    }
  }

  return (
    <AuthLayout title="Iniciar sesión">
      <form className="form" onSubmit={submit}>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input id="email" className="input" type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" inputMode="email" required />
        </div>
        <div className="field">
          <label htmlFor="password">Contraseña</label>
          <PasswordInput id="password" value={password} onChange={setPassword} autoComplete="current-password" minLength={1} />
        </div>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Entrando…' : 'Entrar'}
        </button>
        <p className="faint" style={{ textAlign: 'center' }}>
          ¿Aún no tienes cuenta? <Link to="/registro">Crea una</Link>
        </p>
      </form>
    </AuthLayout>
  );
}

export function Register() {
  const navigate = useNavigate();
  const { setUser } = useAuth();
  const [form, setForm] = useState({ username: '', email: '', password: '', firstName: '', lastName: '' });
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const set = (key: keyof typeof form) => (e: { target: { value: string } }) => setForm((f) => ({ ...f, [key]: e.target.value }));

  async function submit(e: FormEvent) {
    e.preventDefault();
    const username = form.username.trim();
    if (!/^[\p{L}\p{N}_.-]{3,30}$/u.test(username)) {
      return setError('El nombre de usuario debe tener entre 3 y 30 letras, números, puntos o guiones.');
    }
    setBusy(true);
    setError(null);
    try {
      const { user } = await api.register({
        email: form.email.trim(),
        password: form.password,
        username,
        first_name: form.firstName.trim(),
        last_name: form.lastName.trim(),
      });
      setUser(user);
      navigate('/inicio', { replace: true });
    } catch (err) {
      setError(friendlyError(err));
      setBusy(false);
    }
  }

  return (
    <AuthLayout title="Crear cuenta">
      <form className="form" onSubmit={submit}>
        <div className="field">
          <label htmlFor="username">Nombre de usuario</label>
          <input id="username" className="input" value={form.username} onChange={set('username')} autoComplete="username" autoCapitalize="none" required />
        </div>
        <div className="grid-2" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))' }}>
          <div className="field">
            <label htmlFor="first">Nombre</label>
            <input id="first" className="input" value={form.firstName} onChange={set('firstName')} autoComplete="given-name" />
          </div>
          <div className="field">
            <label htmlFor="last">Apellidos</label>
            <input id="last" className="input" value={form.lastName} onChange={set('lastName')} autoComplete="family-name" />
          </div>
        </div>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input id="email" className="input" type="email" value={form.email} onChange={set('email')} autoComplete="email" inputMode="email" required />
        </div>
        <div className="field">
          <label htmlFor="password">Contraseña</label>
          <PasswordInput
            id="password"
            value={form.password}
            onChange={(v) => setForm((f) => ({ ...f, password: v }))}
            autoComplete="new-password"
            placeholder={`Mínimo ${MIN_PASSWORD} caracteres`}
          />
        </div>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
        <p className="faint" style={{ textAlign: 'center' }}>
          ¿Ya tienes cuenta? <Link to="/entrar">Inicia sesión</Link>
        </p>
      </form>
    </AuthLayout>
  );
}
