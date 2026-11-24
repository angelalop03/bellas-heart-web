import { useMemo, useState, type FormEvent } from 'react';
import { Link, NavLink, Route, Routes, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Newspaper, Pencil, Plus, Search, Trash2, Users } from 'lucide-react';
import { supabase, friendlyError } from '../lib/supabase';
import { useAuth } from '../lib/auth';
import { unwrap, useAsync } from '../lib/useAsync';
import { Backdrop, ConfirmDialog, Empty, Modal, PageHead, Spinner, formatDate, useToast } from '../components/ui';
import { StateMeters } from '../components/StateMeters';
import { BACKDROPS, img } from '../lib/assets';
import type { Game, News, Profile } from '../lib/types';

export default function Admin() {
  const { profile, loading } = useAuth();
  if (loading || (!profile && loading)) return <Spinner />;
  if (profile?.role !== 'admin') {
    return (
      <div className="page" style={{ paddingTop: 48 }}>
        <Backdrop src={BACKDROPS.admin} />
        <div className="alert alert-error">Esta zona es solo para administración.</div>
        <Link to="/inicio" className="btn">Volver al inicio</Link>
      </div>
    );
  }
  return (
    <div className="app-shell admin">
      <Backdrop src={BACKDROPS.admin} />
      <header className="topbar">
        <Link to="/inicio" className="btn btn-sm btn-ghost">
          <ArrowLeft size={18} /> Volver al juego
        </Link>
        <div className="topbar-spacer" />
        <span className="badge badge-pink">Administración</span>
      </header>
      <nav className="admin-tabs" aria-label="Secciones de administración">
        <NavLink to="/admin" end>
          <Newspaper size={18} /> Noticias
        </NavLink>
        <NavLink to="/admin/jugadores">
          <Users size={18} /> Jugadoras
        </NavLink>
      </nav>
      <Routes>
        <Route index element={<AdminNews />} />
        <Route path="jugadores" element={<AdminPlayers />} />
        <Route path="jugadores/:userId" element={<AdminPlayer />} />
      </Routes>
    </div>
  );
}

function AdminNews() {
  const toast = useToast();
  const [editing, setEditing] = useState<Partial<News> | null>(null);
  const [toDelete, setToDelete] = useState<News | null>(null);
  const { data: news, loading, error, reload } = useAsync(
    async () => unwrap(await supabase.from('news').select('*').order('created_at', { ascending: false }).order('id', { ascending: false })) as News[],
    [],
  );

  async function remove() {
    if (!toDelete) return;
    const { error } = await supabase.from('news').delete().eq('id', toDelete.id);
    setToDelete(null);
    toast(error ? friendlyError(error) : 'Noticia eliminada');
    reload();
  }

  return (
    <div className="page">
      <PageHead
        title="Noticias"
        action={
          <button type="button" className="btn btn-sm btn-primary" onClick={() => setEditing({ title: '', body: '' })}>
            <Plus size={18} /> Nueva
          </button>
        }
      />
      {loading && <Spinner />}
      {error && <div className="alert alert-error">{error}</div>}
      {news?.length === 0 && <Empty title="No hay noticias" />}
      <div className="stack">
        {news?.map((item) => (
          <article key={item.id} className="card stack" style={{ gap: 8 }}>
            <div className="spread">
              <span className="faint">{formatDate(item.created_at)}</span>
              <div className="row" style={{ gap: 4 }}>
                <button type="button" className="btn btn-sm btn-ghost" onClick={() => setEditing(item)} aria-label={`Editar ${item.title}`}>
                  <Pencil size={17} />
                </button>
                <button type="button" className="btn btn-sm btn-ghost" onClick={() => setToDelete(item)} aria-label={`Borrar ${item.title}`}>
                  <Trash2 size={17} />
                </button>
              </div>
            </div>
            <h2 className="title-md">{item.title}</h2>
            <p className="muted clamp-3">{item.body}</p>
          </article>
        ))}
      </div>
      {editing && (
        <NewsForm
          initial={editing}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            toast('Noticia guardada');
            reload();
          }}
        />
      )}
      {toDelete && (
        <ConfirmDialog
          title="¿Eliminar la noticia?"
          message={`"${toDelete.title}" dejará de verse en el foro.`}
          confirmLabel="Eliminar"
          danger
          onConfirm={remove}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
}

function NewsForm({ initial, onClose, onSaved }: { initial: Partial<News>; onClose: () => void; onSaved: () => void }) {
  const [title, setTitle] = useState(initial.title ?? '');
  const [body, setBody] = useState(initial.body ?? '');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const values = { title: title.trim(), body: body.trim() };
    const { error } = initial.id
      ? await supabase.from('news').update(values).eq('id', initial.id)
      : await supabase.from('news').insert(values);
    setBusy(false);
    if (error) return setError(friendlyError(error));
    onSaved();
  }

  return (
    <Modal title={initial.id ? 'Editar noticia' : 'Nueva noticia'} onClose={onClose}>
      <form className="form" onSubmit={submit}>
        <div className="field">
          <label htmlFor="news-title">Título</label>
          <input id="news-title" className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
        </div>
        <div className="field">
          <label htmlFor="news-body">Texto</label>
          <textarea id="news-body" className="textarea" value={body} onChange={(e) => setBody(e.target.value)} required />
        </div>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <div className="row" style={{ justifyContent: 'flex-end' }}>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" disabled={busy || !title.trim() || !body.trim()}>
            {busy ? 'Guardando…' : 'Guardar'}
          </button>
        </div>
      </form>
    </Modal>
  );
}

function AdminPlayers() {
  const [query, setQuery] = useState('');
  const { data, loading, error } = useAsync(async () => {
    const profiles = unwrap(await supabase.from('profiles').select('*').order('created_at', { ascending: false })) as Profile[];
    const games = unwrap(await supabase.from('games').select('user_id').range(0, 9999)) as Pick<Game, 'user_id'>[];
    const counts = new Map<string, number>();
    for (const g of games) counts.set(g.user_id, (counts.get(g.user_id) ?? 0) + 1);
    return { profiles, counts };
  }, []);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!data) return [];
    return data.profiles.filter((p) => !q || `${p.username} ${p.first_name} ${p.last_name}`.toLowerCase().includes(q));
  }, [data, query]);

  return (
    <div className="page">
      <PageHead title="Jugadoras" />
      <label className="search">
        <Search size={18} />
        <span className="sr-only">Buscar</span>
        <input className="input" placeholder="Buscar por nombre o usuario" value={query} onChange={(e) => setQuery(e.target.value)} />
      </label>
      {loading && <Spinner />}
      {error && <div className="alert alert-error">{error}</div>}
      <div className="stack">
        {filtered.map((p) => (
          <Link key={p.id} to={`/admin/jugadores/${p.id}`} className="card card-link row">
            <img className="avatar" src={img.avatar(p.avatar)} alt="" />
            <div style={{ flex: 1, minWidth: 0 }}>
              <p className="title-md">
                {p.username} {p.role === 'admin' && <span className="badge badge-pink">admin</span>}
              </p>
              <p className="faint">
                {[p.first_name, p.last_name].filter(Boolean).join(' ') || 'Sin nombre'} · desde {formatDate(p.created_at)}
              </p>
            </div>
            <span className="badge">{data?.counts.get(p.id) ?? 0} partidas</span>
          </Link>
        ))}
      </div>
    </div>
  );
}

function AdminPlayer() {
  const { userId } = useParams();
  const { profile: me } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [busy, setBusy] = useState(false);

  const { data, loading, error } = useAsync(async () => {
    const profile = unwrap(await supabase.from('profiles').select('*').eq('id', userId!).maybeSingle()) as Profile | null;
    if (!profile) throw new Error('No encontramos a esta jugadora');
    const games = unwrap(await supabase.from('games').select('*').eq('user_id', userId!).order('updated_at', { ascending: false })) as Game[];
    return { profile, games };
  }, [userId]);

  async function remove() {
    setBusy(true);
    const { error } = await supabase.rpc('admin_delete_user', { p_user_id: userId });
    setBusy(false);
    setConfirmDelete(false);
    if (error) return toast(friendlyError(error));
    toast('Cuenta eliminada');
    navigate('/admin/jugadores', { replace: true });
  }

  if (loading) return <Spinner />;
  if (error || !data) return <div className="page"><div className="alert alert-error">{error}</div></div>;
  const { profile, games } = data;

  return (
    <div className="page">
      <PageHead title={profile.username} back="/admin/jugadores" />
      <div className="glass profile-top" style={{ padding: 16 }}>
        <img className="avatar avatar-lg" src={img.avatar(profile.avatar)} alt="" />
        <div>
          <p className="title-md">{[profile.first_name, profile.last_name].filter(Boolean).join(' ') || 'Sin nombre'}</p>
          <p className="faint">Registrada el {formatDate(profile.created_at)} · {games.length} partidas</p>
        </div>
      </div>
      {games.length === 0 && <Empty title="Sin partidas" />}
      <div className="stack">
        {games.map((g) => (
          <Link key={g.id} to={`/historial/${g.id}`} className="card card-link stack">
            <div className="spread">
              <h2 className="title-md">{g.name}</h2>
              <span className={`badge ${g.state === 'FINISHED' ? 'badge-ok' : 'badge-pink'}`}>{g.state === 'FINISHED' ? 'Terminada' : 'En curso'}</span>
            </div>
            <StateMeters pointsM={g.points_m} pointsR={g.points_r} compact />
            <span className="faint">Ver decisiones · actualizada el {formatDate(g.updated_at)}</span>
          </Link>
        ))}
      </div>
      {profile.id !== me?.id && (
        <button type="button" className="btn btn-danger btn-block" onClick={() => setConfirmDelete(true)}>
          <Trash2 size={18} /> Eliminar cuenta
        </button>
      )}
      {confirmDelete && (
        <ConfirmDialog
          title="¿Eliminar esta cuenta?"
          message={`Se borrarán el perfil de ${profile.username}, sus partidas y decisiones. No se puede deshacer.`}
          confirmLabel="Eliminar"
          danger
          busy={busy}
          onConfirm={remove}
          onCancel={() => setConfirmDelete(false)}
        />
      )}
    </div>
  );
}
