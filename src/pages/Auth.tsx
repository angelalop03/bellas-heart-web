import { useState, type FormEvent, type ReactNode } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, MailCheck } from 'lucide-react';
import { supabase, friendlyError } from '../lib/supabase';
import { useAuth } from '../lib/auth';
import { Backdrop, PageHead, useToast } from '../components/ui';
import { BACKDROPS } from '../lib/assets';

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
}: {
  id: string;
  value: string;
  onChange: (v: string) => void;
  autoComplete: string;
  placeholder?: string;
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
        minLength={6}
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
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const { error } = await supabase.auth.signInWithPassword({ email: email.trim(), password });
    setBusy(false);
    if (error) return setError(friendlyError(error));
    const from = (location.state as { from?: string } | null)?.from;
    navigate(from && from !== '/entrar' ? from : '/inicio', { replace: true });
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
          <PasswordInput id="password" value={password} onChange={setPassword} autoComplete="current-password" />
        </div>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Entrando…' : 'Entrar'}
        </button>
        <div className="spread">
          <Link to="/recuperar">¿Has olvidado la contraseña?</Link>
          <Link to="/registro">Crear cuenta</Link>
        </div>
      </form>
    </AuthLayout>
  );
}

export function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', firstName: '', lastName: '' });
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [pendingEmail, setPendingEmail] = useState<string | null>(null);
  const set = (key: keyof typeof form) => (e: { target: { value: string } }) => setForm((f) => ({ ...f, [key]: e.target.value }));

  async function submit(e: FormEvent) {
    e.preventDefault();
    const username = form.username.trim();
    if (!/^[\p{L}\p{N}_.-]{3,30}$/u.test(username)) {
      return setError('El nombre de usuario debe tener entre 3 y 30 letras, números, puntos o guiones.');
    }
    setBusy(true);
    setError(null);
    const { data: available, error: checkError } = await supabase.rpc('username_available', { p_username: username });
    if (checkError) {
      setBusy(false);
      return setError(friendlyError(checkError));
    }
    if (!available) {
      setBusy(false);
      return setError('Ese nombre de usuario ya está en uso.');
    }
    const { data, error } = await supabase.auth.signUp({
      email: form.email.trim(),
      password: form.password,
      options: {
        emailRedirectTo: `${window.location.origin}/inicio`,
        data: { username, first_name: form.firstName.trim(), last_name: form.lastName.trim() },
      },
    });
    setBusy(false);
    if (error) return setError(friendlyError(error));
    if (data.session) navigate('/inicio', { replace: true });
    else setPendingEmail(form.email.trim());
  }

  if (pendingEmail) {
    return (
      <AuthLayout title="Revisa tu email">
        <div className="stack" style={{ textAlign: 'center', justifyItems: 'center' }}>
          <MailCheck size={48} color="var(--pink-400)" />
          <p>
            Te hemos enviado un enlace a <strong>{pendingEmail}</strong> para confirmar tu cuenta.
          </p>
          <p className="muted">Ábrelo desde este dispositivo y volverás al juego con la sesión iniciada.</p>
          <Link to="/entrar" className="btn btn-block">Ir a iniciar sesión</Link>
        </div>
      </AuthLayout>
    );
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
          <PasswordInput id="password" value={form.password} onChange={(v) => setForm((f) => ({ ...f, password: v }))} autoComplete="new-password" placeholder="Mínimo 6 caracteres" />
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

export function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const { error } = await supabase.auth.resetPasswordForEmail(email.trim(), {
      redirectTo: `${window.location.origin}/nueva-contrasena`,
    });
    setBusy(false);
    if (error) return setError(friendlyError(error));
    setSent(true);
  }

  return (
    <AuthLayout title="Recuperar contraseña" back="/entrar">
      {sent ? (
        <div className="stack">
          <div className="alert alert-ok">Si existe una cuenta con ese email, recibirás un enlace para elegir una contraseña nueva.</div>
          <Link to="/entrar" className="btn btn-block">Volver a iniciar sesión</Link>
        </div>
      ) : (
        <form className="form" onSubmit={submit}>
          <p className="muted">Escribe el email de tu cuenta y te enviaremos un enlace.</p>
          <div className="field">
            <label htmlFor="email">Email</label>
            <input id="email" className="input" type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" inputMode="email" required />
          </div>
          {error && <div className="alert alert-error" role="alert">{error}</div>}
          <button className="btn btn-primary btn-block" disabled={busy}>
            {busy ? 'Enviando…' : 'Enviar enlace'}
          </button>
        </form>
      )}
    </AuthLayout>
  );
}

export function ResetPassword() {
  const navigate = useNavigate();
  const toast = useToast();
  const { session, clearRecovery } = useAuth();
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (password !== confirm) return setError('Las contraseñas no coinciden.');
    setBusy(true);
    setError(null);
    const { error } = await supabase.auth.updateUser({ password });
    setBusy(false);
    if (error) return setError(friendlyError(error));
    clearRecovery();
    toast('Contraseña actualizada');
    navigate('/inicio', { replace: true });
  }

  return (
    <AuthLayout title="Nueva contraseña" back="/entrar">
      {!session ? (
        <div className="alert alert-info">Abre este enlace desde el email de recuperación para poder cambiar la contraseña.</div>
      ) : (
        <form className="form" onSubmit={submit}>
          <div className="field">
            <label htmlFor="password">Nueva contraseña</label>
            <PasswordInput id="password" value={password} onChange={setPassword} autoComplete="new-password" />
          </div>
          <div className="field">
            <label htmlFor="confirm">Repite la contraseña</label>
            <PasswordInput id="confirm" value={confirm} onChange={setConfirm} autoComplete="new-password" />
          </div>
          {error && <div className="alert alert-error" role="alert">{error}</div>}
          <button className="btn btn-primary btn-block" disabled={busy}>
            {busy ? 'Guardando…' : 'Guardar contraseña'}
          </button>
        </form>
      )}
    </AuthLayout>
  );
}
