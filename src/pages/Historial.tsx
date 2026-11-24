import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { BookHeart, Brain, HeartCrack, MessageCircle } from 'lucide-react';
import { supabase } from '../lib/supabase';
import { useAuth } from '../lib/auth';
import { unwrap, useAsync } from '../lib/useAsync';
import { Backdrop, Empty, PageHead, Spinner, formatDate } from '../components/ui';
import { StateMeters } from '../components/StateMeters';
import { BACKDROPS, img, speakerName } from '../lib/assets';
import { chapterLabel, loadStory } from '../lib/story';
import type { Game, GameChoice } from '../lib/types';

export function HistoryList() {
  const { session } = useAuth();
  const userId = session?.user.id;
  const { data: games, loading, error } = useAsync(
    async () =>
      unwrap(await supabase.from('games').select('*').eq('user_id', userId!).order('updated_at', { ascending: false })) as Game[],
    [userId],
  );

  return (
    <div className="page">
      <Backdrop src={BACKDROPS.history} />
      <PageHead title="Historial" />
      <p className="muted">
        Revisa las decisiones de cada partida y cuáles hicieron crecer el malestar de Bella o la tensión en la relación.
      </p>
      {loading && <Spinner />}
      {error && <div className="alert alert-error">{error}</div>}
      {games?.length === 0 && (
        <Empty icon={<BookHeart size={36} color="var(--pink-400)" />} title="Todavía no hay decisiones">
          <Link to="/nueva" className="btn btn-primary">Empezar una partida</Link>
        </Empty>
      )}
      <div className="stack">
        {games?.map((game) => (
          <Link key={game.id} to={`/historial/${game.id}`} className="card card-link stack">
            <div className="spread">
              <h2 className="title-md">{game.name}</h2>
              <span className="faint">{formatDate(game.updated_at)}</span>
            </div>
            <StateMeters pointsM={game.points_m} pointsR={game.points_r} compact />
          </Link>
        ))}
      </div>
    </div>
  );
}

export function HistoryDetail() {
  const { gameId } = useParams();
  const id = Number(gameId);
  const [onlySignals, setOnlySignals] = useState(true);

  const { data, loading, error } = useAsync(async () => {
    const game = unwrap(await supabase.from('games').select('*').eq('id', id).maybeSingle()) as Game | null;
    if (!game) throw new Error('No encontramos esta partida');
    const [choices, story] = await Promise.all([
      supabase.from('game_choices').select('*').eq('game_id', id).order('id').range(0, 4999),
      loadStory(game.history_id),
    ]);
    return { game, choices: unwrap(choices) as GameChoice[], story };
  }, [id]);

  const groups = useMemo(() => {
    if (!data) return [];
    const { story, choices } = data;
    const filtered = onlySignals ? choices.filter((c) => c.points_m > 0 || c.points_r > 0) : choices;
    const result: { chapterId: number | null; label: string; items: GameChoice[] }[] = [];
    for (const choice of filtered) {
      const last = result[result.length - 1];
      if (last && last.chapterId === choice.chapter_id) last.items.push(choice);
      else {
        const ch = choice.chapter_id != null ? story.chapterById.get(choice.chapter_id) : undefined;
        result.push({ chapterId: choice.chapter_id, label: ch ? chapterLabel(ch) : 'Capítulo', items: [choice] });
      }
    }
    return result;
  }, [data, onlySignals]);

  if (loading) return <Spinner />;
  if (error || !data) {
    return (
      <div className="page">
        <PageHead title="Historial" back="/historial" />
        <div className="alert alert-error">{error}</div>
      </div>
    );
  }
  const { story, game } = data;

  return (
    <div className="page">
      <Backdrop src={BACKDROPS.history} />
      <PageHead title={game.name} back="/historial" />
      <div className="glass" style={{ padding: 16 }}>
        <StateMeters pointsM={game.points_m} pointsR={game.points_r} />
      </div>
      <div className="segmented" role="tablist" aria-label="Filtrar decisiones">
        <button type="button" role="tab" aria-selected={onlySignals} className={onlySignals ? 'active' : ''} onClick={() => setOnlySignals(true)}>
          Señales de alerta
        </button>
        <button type="button" role="tab" aria-selected={!onlySignals} className={!onlySignals ? 'active' : ''} onClick={() => setOnlySignals(false)}>
          Todas ({data.choices.length})
        </button>
      </div>
      {groups.length === 0 && (
        <Empty title={onlySignals ? 'Ninguna decisión ha aumentado el malestar ni la tensión' : 'Aún no has tomado decisiones'} />
      )}
      {groups.map((group, gi) => {
        const chapter = group.chapterId != null ? story.chapterById.get(group.chapterId) : undefined;
        return (
          <section key={`${group.chapterId}-${gi}`} className="stack">
            <div className="row">
              <img className="chapter-thumb" src={img.chapter(chapter?.image_path)} alt="" />
              <h2 className="title-md">{group.label}</h2>
            </div>
            {group.items.map((choice) => {
              const decision = choice.decision_id != null ? story.decisionById.get(choice.decision_id) : undefined;
              const option = choice.option_id != null ? story.optionById.get(choice.option_id) : undefined;
              const character = decision?.character_id != null ? story.characterById.get(decision.character_id) : undefined;
              return (
                <article key={choice.id} className="card choice-log">
                  {decision?.dialogue && (
                    <p className="choice-context">
                      <MessageCircle size={15} />
                      <span>
                        {character && <strong>{speakerName(character.name)}: </strong>}
                        <span className="clamp-3">{decision.dialogue}</span>
                      </span>
                    </p>
                  )}
                  <p className="choice-picked">{option?.text ?? 'Opción eliminada'}</p>
                  {(choice.points_m > 0 || choice.points_r > 0) && (
                    <div className="row-wrap">
                      {choice.points_m > 0 && <span className="badge"><Brain size={13} /> Malestar de Bella +{choice.points_m}</span>}
                      {choice.points_r > 0 && <span className="badge"><HeartCrack size={13} /> Tensión +{choice.points_r}</span>}
                    </div>
                  )}
                </article>
              );
            })}
          </section>
        );
      })}
    </div>
  );
}
