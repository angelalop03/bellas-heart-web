import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { BookOpen, Play } from 'lucide-react';
import { api, friendlyError } from '../lib/api';
import { useAsync } from '../lib/useAsync';
import { Backdrop, Empty, PageHead, Spinner } from '../components/ui';
import { BACKDROPS, img } from '../lib/assets';

export function Stories() {
  const { data: stories, loading, error } = useAsync(() => api.histories(), []);

  return (
    <div className="page">
      <Backdrop src={BACKDROPS.newGame} />
      <PageHead title="Elige una historia" back="/inicio" />
      {loading && <Spinner />}
      {error && <div className="alert alert-error">{error}</div>}
      {stories?.length === 0 && <Empty title="Todavía no hay historias publicadas" />}
      <div className="grid-2">
        {stories?.map((story) => (
          <Link key={story.id} to={`/nueva/${story.id}`} className="card card-link story-card">
            <img src={img.history(story.image_path)} alt="" loading="lazy" />
            <div className="stack" style={{ gap: 6 }}>
              <h2 className="title-md" style={{ fontSize: '1.3rem' }}>{story.title}</h2>
              <p className="muted clamp-3">{story.description}</p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

export function StoryDetail() {
  const { historyId } = useParams();
  const navigate = useNavigate();
  const id = Number(historyId);
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const { data, loading, error: loadError } = useAsync(async () => {
    const { history, chapters } = await api.history(id);
    return { story: history, chapters };
  }, [id]);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const { id: gameId } = await api.createGame(id, name.trim());
      navigate(`/jugar/${gameId}`, { replace: true });
    } catch (err) {
      setBusy(false);
      setError(friendlyError(err));
    }
  }

  if (loading) return <Spinner />;
  if (loadError || !data?.story) {
    return (
      <div className="page">
        <PageHead title="Historia" back="/nueva" />
        <div className="alert alert-error">{loadError ?? 'Esta historia no está disponible.'}</div>
      </div>
    );
  }
  const story = data.story;

  return (
    <div className="page">
      <Backdrop src={BACKDROPS.newGame} />
      <PageHead title={story.title} back="/nueva" />
      <div className="glass story-detail">
        <img src={img.history(story.image_path)} alt="" />
        <div className="stack">
          <p>{story.description}</p>
          <p className="faint">
            <BookOpen size={15} style={{ verticalAlign: '-3px' }} /> Introducción y capítulos con distintos caminos según tus decisiones.
          </p>
        </div>
        <form className="form" onSubmit={submit}>
          <div className="field">
            <label htmlFor="game-name">Nombre de la partida</label>
            <input
              id="game-name"
              className="input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Por ejemplo: Mi primera partida"
              maxLength={40}
              required
            />
          </div>
          {error && <div className="alert alert-error" role="alert">{error}</div>}
          <button className="btn btn-primary btn-block" disabled={busy || !name.trim()}>
            <Play size={20} fill="currentColor" /> {busy ? 'Preparando…' : 'Empezar la historia'}
          </button>
        </form>
      </div>
    </div>
  );
}
