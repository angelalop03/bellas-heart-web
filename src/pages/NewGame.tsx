import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { BookOpen, Play } from 'lucide-react';
import { supabase, friendlyError } from '../lib/supabase';
import { unwrap, useAsync } from '../lib/useAsync';
import { Backdrop, Empty, PageHead, Spinner } from '../components/ui';
import { BACKDROPS, img } from '../lib/assets';
import type { History } from '../lib/types';

export function Stories() {
  const { data: stories, loading, error } = useAsync(
    async () => unwrap(await supabase.from('histories').select('*').eq('published', true).order('id')) as History[],
    [],
  );

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
    const story = unwrap(await supabase.from('histories').select('*').eq('id', id).maybeSingle()) as History | null;
    const { count } = await supabase
      .from('chapters')
      .select('id', { count: 'exact', head: true })
      .eq('history_id', id);
    return { story, chapters: count ?? 0 };
  }, [id]);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const { data: gameId, error } = await supabase.rpc('create_game', { p_history_id: id, p_name: name.trim() });
    setBusy(false);
    if (error) return setError(friendlyError(error));
    navigate(`/jugar/${gameId}`, { replace: true });
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
            <BookOpen size={15} style={{ verticalAlign: '-3px' }} /> Introducción y capítulos con distintos caminos según cómo evolucione la relación.
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
