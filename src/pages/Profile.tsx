import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Check, Info, KeyRound, LogOut, Trash2 } from 'lucide-react';
import { supabase, friendlyError } from '../lib/supabase';
import { useAuth } from '../lib/auth';
import { Backdrop, ConfirmDialog, Modal, PageHead, Spinner, useToast } from '../components/ui';
import { InstallPrompt } from '../components/InstallPrompt';
import { PasswordInput } from './Auth';
import { AVATARS, BACKDROPS, img } from '../lib/assets';

export function ProfilePage() {
  const { profile, session, refreshProfile, signOut } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [form, setForm] = useState({ username: '', first_name: '', last_name: '', avatar: 'default_image' });
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [confirmLogout, setConfirmLogout] = useState(false);
  const [showDelete, setShowDelete] = useState(false);

  useEffect(() => {
    if (profile) {
      setForm({ username: profile.username, first_name: profile.first_name, last_name: profile.last_name, avatar: profile.avatar });
    }
  }, [profile]);

  if (!profile) return <Spinner />;

  const dirty =
    form.username !== profile.username ||
    form.first_name !== profile.first_name ||
    form.last_name !== profile.last_name ||
    form.avatar !== profile.avatar;

  async function save(e: FormEvent) {
    e.preventDefault();
    const username = form.username.trim();
    if (!/^[\p{L}\p{N}_.-]{3,30}$/u.test(username)) {
      return setError('El nombre de usuario debe tener entre 3 y 30 letras, números, puntos o guiones.');
    }
    setSaving(true);
    setError(null);
    const { error } = await supabase
      .from('profiles')
      .update({ username, first_name: form.first_name.trim(), last_name: form.last_name.trim(), avatar: form.avatar })
      .eq('id', profile!.id);
    setSaving(false);
    if (error) return setError(friendlyError(error));
    await refreshProfile();
    toast('Perfil guardado');
  }

  return (
    <div className="page">
      <Backdrop src={BACKDROPS.profile} />
      <PageHead title="Mi perfil" />

      <form className="glass profile-card form" onSubmit={save}>
        <div className="profile-top">
          <img className="avatar avatar-xl" src={img.avatar(form.avatar)} alt="Tu foto de perfil" />
          <div>
            <p className="title-md" style={{ fontSize: '1.3rem' }}>{profile.username}</p>
            <p className="faint">{session?.user.email}</p>
          </div>
        </div>

        <fieldset className="avatar-picker">
          <legend>Foto de perfil</legend>
          {AVATARS.map((a) => (
            <label key={a} className={`avatar-option ${form.avatar === a ? 'selected' : ''}`}>
              <input type="radio" name="avatar" value={a} checked={form.avatar === a} onChange={() => setForm((f) => ({ ...f, avatar: a }))} className="sr-only" />
              <img className="avatar" src={img.avatar(a)} alt="" />
              {form.avatar === a && <Check size={16} className="avatar-check" />}
            </label>
          ))}
        </fieldset>

        <div className="field">
          <label htmlFor="username">Nombre de usuario</label>
          <input id="username" className="input" value={form.username} onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))} autoCapitalize="none" required />
        </div>
        <div className="grid-2" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))' }}>
          <div className="field">
            <label htmlFor="first">Nombre</label>
            <input id="first" className="input" value={form.first_name} onChange={(e) => setForm((f) => ({ ...f, first_name: e.target.value }))} />
          </div>
          <div className="field">
            <label htmlFor="last">Apellidos</label>
            <input id="last" className="input" value={form.last_name} onChange={(e) => setForm((f) => ({ ...f, last_name: e.target.value }))} />
          </div>
        </div>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <button className="btn btn-primary btn-block" disabled={!dirty || saving}>
          {saving ? 'Guardando…' : 'Guardar cambios'}
        </button>
      </form>

      <div className="stack">
        <InstallPrompt />
        <button type="button" className="btn btn-block" onClick={() => setShowPassword(true)}>
          <KeyRound size={20} /> Cambiar contraseña
        </button>
        <Link to="/creditos" className="btn btn-block btn-ghost">
          <Info size={20} /> Sobre el proyecto
        </Link>
        <button type="button" className="btn btn-block btn-ghost" onClick={() => setConfirmLogout(true)}>
          <LogOut size={20} /> Cerrar sesión
        </button>
        <button type="button" className="btn btn-block btn-danger" onClick={() => setShowDelete(true)}>
          <Trash2 size={20} /> Eliminar mi cuenta
        </button>
      </div>

      {showPassword && <ChangePassword onClose={() => setShowPassword(false)} />}
      {confirmLogout && (
        <ConfirmDialog
          title="¿Cerrar sesión?"
          message="Tus partidas quedan guardadas en tu cuenta."
          confirmLabel="Cerrar sesión"
          onCancel={() => setConfirmLogout(false)}
          onConfirm={async () => {
            await signOut();
            navigate('/', { replace: true });
          }}
        />
      )}
      {showDelete && <DeleteAccount email={session?.user.email ?? ''} onClose={() => setShowDelete(false)} />}
    </div>
  );
}

function ChangePassword({ onClose }: { onClose: () => void }) {
  const toast = useToast();
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
    toast('Contraseña actualizada');
    onClose();
  }

  return (
    <Modal title="Cambiar contraseña" onClose={onClose}>
      <form className="form" onSubmit={submit}>
        <div className="field">
          <label htmlFor="new-password">Nueva contraseña</label>
          <PasswordInput id="new-password" value={password} onChange={setPassword} autoComplete="new-password" />
        </div>
        <div className="field">
          <label htmlFor="confirm-password">Repite la contraseña</label>
          <PasswordInput id="confirm-password" value={confirm} onChange={setConfirm} autoComplete="new-password" />
        </div>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <div className="row" style={{ justifyContent: 'flex-end' }}>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Guardando…' : 'Guardar'}</button>
        </div>
      </form>
    </Modal>
  );
}

function DeleteAccount({ email, onClose }: { email: string; onClose: () => void }) {
  const { signOut } = useAuth();
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    // Se comprueba la contraseña antes de borrar nada
    const check = await supabase.auth.signInWithPassword({ email, password });
    if (check.error) {
      setBusy(false);
      return setError('La contraseña no es correcta.');
    }
    const { error } = await supabase.rpc('delete_my_account');
    if (error) {
      setBusy(false);
      return setError(friendlyError(error));
    }
    await signOut();
    navigate('/', { replace: true });
  }

  return (
    <Modal title="Eliminar mi cuenta" onClose={onClose}>
      <form className="form" onSubmit={submit}>
        <p className="muted">Se borrarán tu perfil, tus partidas y todas tus decisiones. No se puede deshacer.</p>
        <div className="field">
          <label htmlFor="delete-password">Escribe tu contraseña para confirmar</label>
          <PasswordInput id="delete-password" value={password} onChange={setPassword} autoComplete="current-password" />
        </div>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <div className="row" style={{ justifyContent: 'flex-end' }}>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancelar</button>
          <button className="btn btn-danger" disabled={busy || !password}>{busy ? 'Eliminando…' : 'Eliminar para siempre'}</button>
        </div>
      </form>
    </Modal>
  );
}
