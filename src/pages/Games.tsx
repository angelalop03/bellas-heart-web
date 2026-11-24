import { useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Check, GitBranch, Lock, Play, Plus, Trash2, Trophy } from 'lucide-react';
import { supabase, friendlyError } from '../lib/supabase';
import { useAuth } from '../lib/auth';
import { unwrap, useAsync } from '../lib/useAsync';
import { Backdrop, ConfirmDialog, Empty, PageHead, Spinner, formatDate, useToast } from '../components/ui';
import { StateMeters } from '../components/StateMeters';
import { BACKDROPS, img } from '../lib/assets';
import { chapterLabel, chapterSortKey } from '../lib/story';
import type { Chapter, Game, GameChapter, History } from '../lib/types';

export function Games() {
  const { session } = useAuth();
  const toast = useToast();
  const userId = session?.user.id;
  const [toDelete, setToDelete] = useState<Game | null>(null);
  const [busy, setBusy] = useState(false);

  const { data, loading, error, reload } = useAsync(async () => {
    const games = unwrap(
      await supabase.from('games').select('*').eq('user_id', userId!).order('updated_at', { ascending: false }),
    ) as Game[];
    const histories = unwrap(await supabase.from('histories').select('id,title,image_path')) as History[];
    return { games, histories: new Map(histories.map((h) => [h.id, h])) };
  }, [userId]);

  async function remove() {
    if (!toDelete) return;
    setBusy(true);
    const { error } = await supabase.from('games').delete().eq('id', toDelete.id);
    setBusy(false);
    setToDelete(null);
    if (error) toast(friendlyError(error));
    else {
      toast('Partida borrada');
      reload();
    }
  }

  return (
    <div className="page">
      <Backdrop src={BACKDROPS.games} />
      <PageHead
        title="Mis partidas"
        action={
          <Link to="/nueva" className="btn btn-sm btn-primary">
            <Plus size={18} /> Nueva
          </Link>
        }
      />
      {loading && <Spinner />}
      {error && <div className="alert alert-error">{error}</div>}
      {data?.games.length === 0 && (
        <Empty icon={<Play size={36} color="var(--pink-400)" />} title="Aún no tienes partidas">
          <Link to="/nueva" className="btn btn-primary">Crear la primera</Link>
        </Empty>
      )}
      <div className="stack">
        {data?.games.map((game) => (
          <article key={game.id} className="card game-card">
            <Link to={`/partidas/${game.id}`} className="game-card-main">
              <div className="spread">
                <div>
                  <h2 className="title-md">{game.name}</h2>
                  <p className="faint">
                    {data.histories.get(game.history_id)?.title} · {formatDate(game.created_at)}
                  </p>
                </div>
                {game.state === 'FINISHED' ? (
                  <span className="badge badge-ok"><Trophy size={14} /> Terminada</span>
                ) : (
                  <span className="badge badge-pink">En curso</span>
                )}
              </div>
              <StateMeters pointsM={game.points_m} pointsR={game.points_r} compact />
            </Link>
            <div className="row" style={{ justifyContent: 'flex-end' }}>
              <button type="button" className="btn btn-sm btn-ghost" onClick={() => setToDelete(game)} aria-label={`Borrar ${game.name}`}>
                <Trash2 size={18} />
              </button>
              <Link to={`/partidas/${game.id}`} className="btn btn-sm">Capítulos</Link>
              {game.state === 'IN_PROGRESS' && (
                <Link to={`/jugar/${game.id}`} className="btn btn-sm btn-primary">
                  <Play size={16} fill="currentColor" /> Jugar
                </Link>
              )}
            </div>
          </article>
        ))}
      </div>
      {toDelete && (
        <ConfirmDialog
          title="¿Borrar la partida?"
          message={`Se perderá "${toDelete.name}" con todas sus decisiones. No se puede deshacer.`}
          confirmLabel="Borrar"
          danger
          busy={busy}
          onConfirm={remove}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
}

type ChapterStatus = 'played' | 'current' | 'locked' | 'skipped';

export function GameChapters() {
  const { gameId } = useParams();
  const id = Number(gameId);
  const navigate = useNavigate();

  const { data, loading, error } = useAsync(async () => {
    const game = unwrap(await supabase.from('games').select('*').eq('id', id).maybeSingle()) as Game | null;
    if (!game) throw new Error('No encontramos esta partida');
    const [chapters, progress] = await Promise.all([
      supabase.from('chapters').select('*').eq('history_id', game.history_id),
      supabase.from('game_chapters').select('*').eq('game_id', id),
    ]);
    return {
      game,
      chapters: (unwrap(chapters) as Chapter[]).sort((a, b) => chapterSortKey(a) - chapterSortKey(b)),
      progress: new Map((unwrap(progress) as GameChapter[]).map((p) => [p.chapter_id, p])),
    };
  }, [id]);

  // Agrupa los capítulos por número (las ramas 6.1–6.4 forman un solo paso del camino)
  const steps = useMemo(() => {
    if (!data) return [];
    const groups = new Map<string, Chapter[]>();
    for (const ch of data.chapters) {
      const key = ch.chapter_number.split('.')[0];
      groups.set(key, [...(groups.get(key) ?? []), ch]);
    }
    return [...groups.values()].map((list) => {
      const unlocked = list.find((c) => data.progress.has(c.id));
      const chapter = unlocked ?? list[0];
      const p = unlocked ? data.progress.get(unlocked.id) : undefined;
      let status: ChapterStatus = 'locked';
      if (p?.played_at) status = 'played';
      else if (unlocked && data.game.current_chapter_id === unlocked.id && data.game.state === 'IN_PROGRESS') status = 'current';
      else if (data.game.state === 'FINISHED') status = 'skipped';
      return { chapter, status, branches: list.length };
    });
  }, [data]);

  if (loading) return <Spinner />;
  if (error || !data) {
    return (
      <div className="page">
        <PageHead title="Capítulos" back="/partidas" />
        <div className="alert alert-error">{error}</div>
      </div>
    );
  }

  return (
    <div className="page">
      <Backdrop src={BACKDROPS.games} />
      <PageHead title={data.game.name} back="/partidas" />
      <div className="glass" style={{ padding: 16 }}>
        <StateMeters pointsM={data.game.points_m} pointsR={data.game.points_r} />
      </div>
      <ol className="chapter-path">
        {steps.map(({ chapter, status, branches }) => (
          <li key={chapter.id} className={`chapter-step is-${status}`}>
            <button
              type="button"
              className="chapter-node card"
              disabled={status !== 'current'}
              onClick={() => navigate(`/jugar/${data.game.id}`)}
            >
              <img src={img.chapter(chapter.image_path)} alt="" loading="lazy" />
              <div className="chapter-info">
                <span className="chapter-name">{status === 'locked' && branches > 1 ? `Capítulo ${chapter.chapter_number.split('.')[0]}` : chapterLabel(chapter)}</span>
                <span className="faint">
                  {status === 'played' && 'Completado'}
                  {status === 'current' && 'Toca para continuar'}
                  {status === 'locked' && (branches > 1 ? 'Su contenido depende de tus decisiones' : 'Bloqueado')}
                  {status === 'skipped' && 'No forma parte de tu camino'}
                </span>
              </div>
              <span className="chapter-icon" aria-hidden="true">
                {status === 'played' && <Check size={20} />}
                {status === 'current' && <Play size={20} fill="currentColor" />}
                {status === 'locked' && (branches > 1 ? <GitBranch size={20} /> : <Lock size={18} />)}
              </span>
            </button>
          </li>
        ))}
      </ol>
      {data.game.state === 'FINISHED' && (
        <div className="card stack" style={{ textAlign: 'center' }}>
          <Trophy size={34} color="var(--warn)" style={{ margin: '0 auto' }} />
          <p className="title-md">Has llegado al final de esta historia</p>
          <Link to={`/historial/${data.game.id}`} className="btn">Revisar mis decisiones</Link>
        </div>
      )}
    </div>
  );
}
