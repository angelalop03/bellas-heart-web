import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, ChevronRight, Home, LifeBuoy, Music, BookHeart, Volume2, VolumeX, Sparkles, Trophy } from 'lucide-react';
import { api, friendlyError } from '../lib/api';
import { useAuth } from '../lib/auth';
import { loadStory, chapterLabel, type Story } from '../lib/story';
import { availableOptions, chooseLocal, startChapter, type LocalProgress } from '../lib/engine';
import { img, preload, speakerName } from '../lib/assets';
import { getMusicPrefs, playMusic, setMusicMuted, setMusicVolume, stopMusic } from '../lib/music';
import { PSYCH_INFO, psychState, RELATION_INFO, relationState } from '../lib/states';
import { StateMeters } from '../components/StateMeters';
import { Spinner, useToast } from '../components/ui';
import type { Game, Option } from '../lib/types';

type Mode = 'game' | 'tutorial';

interface Interlude {
  kind: 'chapter' | 'finished' | 'tutorial';
  completedLabel: string;
  nextChapterId: number | null;
}

const CONTINUE_RE = /^\(?\s*continuar\s*\)?\.?$/i;

function progressFromGame(game: Game): LocalProgress {
  return {
    chapterId: game.current_chapter_id ?? 0,
    sceneId: game.current_scene_id,
    decisionId: game.current_decision_id,
    points_m: game.points_m,
    points_r: game.points_r,
    finished: game.state === 'FINISHED',
  };
}

/** Escribe el texto poco a poco; tocar lo completa de golpe. */
function useTypewriter(text: string, speed = 18) {
  const [shown, setShown] = useState(0);
  useEffect(() => {
    setShown(0);
    if (!text) return;
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduce) {
      setShown(text.length);
      return;
    }
    const id = window.setInterval(() => {
      setShown((n) => {
        if (n >= text.length) {
          window.clearInterval(id);
          return n;
        }
        return Math.min(text.length, n + 2);
      });
    }, speed);
    return () => window.clearInterval(id);
  }, [text, speed]);
  return { visible: text.slice(0, shown), done: shown >= text.length, finish: () => setShown(text.length) };
}

export default function Play({ mode }: { mode: Mode }) {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { user } = useAuth();

  const [story, setStory] = useState<Story | null>(null);
  const [game, setGame] = useState<Game | null>(null);
  const [progress, setProgress] = useState<LocalProgress | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [interlude, setInterlude] = useState<Interlude | null>(null);
  const [showSettings, setShowSettings] = useState(false);
  const [music, setMusic] = useState(getMusicPrefs());

  // ---------- Carga ----------
  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        if (mode === 'tutorial') {
          const s = await loadStory(null);
          const chapter = s.chapters[0];
          if (!chapter) throw new Error('El tutorial no está disponible');
          if (!cancelled) {
            setStory(s);
            setProgress(startChapter(s, chapter.id));
          }
        } else {
          const { game: g } = await api.game(Number(gameId));
          const s = await loadStory(g.history_id);
          if (!cancelled) {
            setStory(s);
            setGame(g);
            setProgress(progressFromGame(g));
          }
        }
      } catch (e) {
        if (!cancelled) setLoadError(friendlyError(e));
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [mode, gameId]);

  // ---------- Música ----------
  useEffect(() => {
    playMusic();
    const resume = () => playMusic();
    window.addEventListener('pointerdown', resume, { once: true });
    return () => {
      window.removeEventListener('pointerdown', resume);
      stopMusic();
    };
  }, []);

  // ---------- Datos de la pantalla actual ----------
  const decision = story && progress?.decisionId != null ? story.decisionById.get(progress.decisionId) : undefined;
  const scene = story && progress?.sceneId != null ? story.sceneById.get(progress.sceneId) : undefined;
  const chapter = story && progress ? story.chapterById.get(progress.chapterId) : undefined;
  const character = decision?.character_id != null ? story?.characterById.get(decision.character_id) : undefined;
  const options = useMemo(
    () => (story && progress ? availableOptions(story, progress.decisionId, progress.points_m) : []),
    [story, progress],
  );
  const dialogue = decision?.dialogue ?? '';
  const typer = useTypewriter(dialogue);
  const isNarration = !character;
  const bgUrl = img.scene(scene?.background_image);
  const charUrl = character ? img.character(character.image_path) : null;

  // Precarga lo que puede venir a continuación (siguiente diálogo y siguiente escena)
  useEffect(() => {
    if (!story || !progress) return;
    const urls: (string | null)[] = [];
    for (const o of options) {
      const next = o.next_decision_id != null ? story.decisionById.get(o.next_decision_id) : undefined;
      const c = next?.character_id != null ? story.characterById.get(next.character_id) : undefined;
      if (c) urls.push(img.character(c.image_path));
    }
    const scenes = story.scenesByChapter.get(progress.chapterId) ?? [];
    const idx = scenes.findIndex((s) => s.id === progress.sceneId);
    if (idx >= 0 && scenes[idx + 1]) urls.push(img.scene(scenes[idx + 1].background_image));
    preload(urls);
  }, [story, progress, options]);

  // ---------- Elegir opción ----------
  const choose = useCallback(
    async (option: Option) => {
      if (!story || !progress || busy) return;
      playMusic();
      if (mode === 'tutorial') {
        const next = chooseLocal(story, progress, option);
        setProgress(next);
        if (next.finished) setInterlude({ kind: 'tutorial', completedLabel: 'Tutorial', nextChapterId: null });
        return;
      }
      if (!game) return;
      const previous = progress;
      const optimistic = chooseLocal(story, progress, option);
      // Avance inmediato dentro del capítulo; el servidor confirma (y decide el siguiente capítulo).
      if (!optimistic.finished) setProgress(optimistic);
      setBusy(true);
      let result;
      try {
        result = await api.choose(game.id, option.id);
      } catch (err) {
        setProgress(previous);
        toast(friendlyError(err));
        return;
      } finally {
        setBusy(false);
      }
      const completed = story.chapterById.get(previous.chapterId);
      setGame(result.game);
      setProgress(progressFromGame(result.game));
      if (result.event === 'chapter_end') {
        setInterlude({
          kind: result.finished ? 'finished' : 'chapter',
          completedLabel: completed ? chapterLabel(completed) : 'Capítulo',
          nextChapterId: result.next_chapter_id,
        });
      }
    },
    [story, progress, busy, mode, game, toast],
  );

  // Teclado: Intro/espacio para avanzar, 1–4 para elegir
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (interlude || showSettings) return;
      if ((e.key === 'Enter' || e.key === ' ') && !typer.done) {
        e.preventDefault();
        typer.finish();
        return;
      }
      if (!typer.done) return;
      if ((e.key === 'Enter' || e.key === ' ') && options.length === 1) {
        e.preventDefault();
        choose(options[0]);
      }
      const n = Number(e.key);
      if (n >= 1 && n <= options.length) choose(options[n - 1]);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [options, typer, choose, interlude, showSettings]);

  const exitTo = mode === 'tutorial' ? (user ? '/inicio' : '/') : game ? `/partidas/${game.id}` : '/partidas';

  // ---------- Render ----------
  if (loadError) {
    return (
      <div className="page" style={{ paddingTop: 48 }}>
        <div className="alert alert-error">{loadError}</div>
        <Link to={exitTo} className="btn">Volver</Link>
      </div>
    );
  }
  if (!story || !progress) return <Spinner label="Preparando la escena…" />;

  const finishedGame = mode === 'game' && game?.state === 'FINISHED' && !interlude;
  const singleContinue = options.length === 1 && CONTINUE_RE.test(options[0].text.trim());

  return (
    <div className={`play ${isNarration ? 'is-narration' : ''}`}>
      <div className="play-bg" key={bgUrl} style={{ backgroundImage: `url("${bgUrl}")` }} />
      <div className="play-shade" />
      {charUrl && <img className="play-character" key={charUrl} src={charUrl} alt={speakerName(character?.name) ?? ''} />}

      <header className="play-hud">
        <button type="button" className="btn btn-icon hud-btn" aria-label="Salir de la partida" onClick={() => navigate(exitTo)}>
          <ArrowLeft size={22} />
        </button>
        <div className="hud-chapter">
          <span className="hud-kicker">{mode === 'tutorial' ? 'Tutorial' : game?.name}</span>
          <span className="hud-title">{chapter ? chapterLabel(chapter) : ''}</span>
        </div>
        <div className="hud-meters">
          <StateMeters pointsM={progress.points_m} pointsR={progress.points_r} compact />
        </div>
        <button
          type="button"
          className="btn btn-icon hud-btn"
          aria-label="Música"
          aria-expanded={showSettings}
          onClick={() => setShowSettings((v) => !v)}
        >
          {music.muted ? <VolumeX size={20} /> : <Music size={20} />}
        </button>
      </header>

      {showSettings && (
        <div className="glass play-settings">
          <div className="spread">
            <strong>Música</strong>
            <button
              type="button"
              className="btn btn-sm"
              onClick={() => {
                const muted = !music.muted;
                setMusicMuted(muted);
                setMusic((m) => ({ ...m, muted }));
                if (!muted) playMusic();
              }}
            >
              {music.muted ? <><Volume2 size={16} /> Activar</> : <><VolumeX size={16} /> Silenciar</>}
            </button>
          </div>
          <label className="field">
            <span className="faint">Volumen {Math.round(music.volume * 100)}%</span>
            <input
              type="range"
              min={0}
              max={1}
              step={0.05}
              value={music.volume}
              onChange={(e) => {
                const volume = Number(e.target.value);
                setMusicVolume(volume);
                setMusic((m) => ({ ...m, volume }));
              }}
            />
          </label>
          <div className="play-meters-full">
            <StateMeters pointsM={progress.points_m} pointsR={progress.points_r} />
          </div>
          <Link to="/recursos" className="btn btn-sm btn-ghost">
            <LifeBuoy size={16} /> Recursos de ayuda
          </Link>
        </div>
      )}

      {finishedGame ? (
        <EndCard game={game!} />
      ) : decision ? (
        <section className="play-panel" aria-live="polite">
          <div className="glass dialogue" onClick={() => !typer.done && typer.finish()}>
            {!isNarration && <span className="speaker">{speakerName(character?.name)}</span>}
            <p className={`dialogue-text ${isNarration ? 'narration' : ''}`}>
              {typer.visible}
              {!typer.done && <span className="caret" aria-hidden="true" />}
            </p>
            {!typer.done && <span className="tap-hint">Toca para ver todo</span>}
          </div>

          <div className={`choices ${typer.done ? 'is-ready' : ''}`}>
            {typer.done &&
              (singleContinue ? (
                <button type="button" className="choice choice-continue" onClick={() => choose(options[0])} disabled={busy}>
                  Continuar <ChevronRight size={20} />
                </button>
              ) : (
                options.map((option, i) => {
                  const text = option.text.trim();
                  const isAction = text.startsWith('(');
                  return (
                    <button
                      type="button"
                      key={option.id}
                      className={`choice ${isAction ? 'choice-action' : ''}`}
                      style={{ animationDelay: `${i * 60}ms` }}
                      onClick={() => choose(option)}
                      disabled={busy}
                    >
                      {CONTINUE_RE.test(text) ? 'Continuar' : text}
                    </button>
                  );
                })
              ))}
            {typer.done && options.length === 0 && (
              <div className="alert alert-error">Esta parte de la historia no tiene opciones disponibles.</div>
            )}
          </div>
        </section>
      ) : (
        !interlude && (
          <section className="play-panel">
            <div className="glass dialogue">
              <p>No hay nada que jugar ahora mismo en esta partida.</p>
              <Link to={exitTo} className="btn">Volver</Link>
            </div>
          </section>
        )
      )}

      {interlude && (
        <InterludeCard
          interlude={interlude}
          story={story}
          game={game}
          loggedIn={Boolean(user)}
          onContinue={() => setInterlude(null)}
          onExit={() => navigate(exitTo)}
        />
      )}
    </div>
  );
}

function InterludeCard({
  interlude,
  story,
  game,
  loggedIn,
  onContinue,
  onExit,
}: {
  interlude: Interlude;
  story: Story;
  game: Game | null;
  loggedIn: boolean;
  onContinue: () => void;
  onExit: () => void;
}) {
  const next = interlude.nextChapterId != null ? story.chapterById.get(interlude.nextChapterId) : undefined;

  if (interlude.kind === 'tutorial') {
    return (
      <div className="modal-backdrop">
        <div className="glass modal interlude">
          <Sparkles size={40} color="var(--pink-400)" style={{ margin: '0 auto' }} />
          <h2 className="title-lg" style={{ textAlign: 'center' }}>¡Tutorial completado!</h2>
          <p className="muted" style={{ textAlign: 'center' }}>Ya sabes cómo funciona. Ahora empieza la historia de Bella y David.</p>
          <Link to={loggedIn ? '/nueva' : '/registro'} className="btn btn-primary btn-block">
            {loggedIn ? 'Crear una partida' : 'Crear cuenta y jugar'}
          </Link>
          <button type="button" className="btn btn-ghost btn-block" onClick={onExit}>Volver</button>
        </div>
      </div>
    );
  }

  if (interlude.kind === 'finished' && game) return <div className="modal-backdrop"><EndCard game={game} /></div>;

  return (
    <div className="modal-backdrop">
      <div className="glass modal interlude">
        <span className="badge badge-ok" style={{ justifySelf: 'center' }}>Completado</span>
        <h2 className="title-lg" style={{ textAlign: 'center' }}>{interlude.completedLabel}</h2>
        {game && <StateMeters pointsM={game.points_m} pointsR={game.points_r} />}
        {next && (
          <div className="next-chapter">
            <img src={img.chapter(next.image_path)} alt="" />
            <div>
              <span className="faint">Siguiente</span>
              <p className="title-md">{chapterLabel(next)}</p>
            </div>
          </div>
        )}
        <button type="button" className="btn btn-primary btn-block" onClick={onContinue}>
          Continuar <ChevronRight size={20} />
        </button>
        <button type="button" className="btn btn-ghost btn-block" onClick={onExit}>Guardar y salir</button>
      </div>
    </div>
  );
}

function EndCard({ game }: { game: Game }) {
  const psych = PSYCH_INFO[psychState(game.points_m)];
  const rel = RELATION_INFO[relationState(game.points_r)];
  return (
    <section className="play-panel play-end">
      <div className="glass modal interlude" style={{ width: 'min(100%, 520px)' }}>
        <Trophy size={40} color="var(--warn)" style={{ margin: '0 auto' }} />
        <h2 className="title-lg" style={{ textAlign: 'center' }}>Fin de la historia</h2>
        <p className="muted" style={{ textAlign: 'center' }}>
          Bella termina <strong style={{ color: psych.color }}>{psych.label.toLowerCase()}</strong> y la relación en estado{' '}
          <strong style={{ color: rel.color }}>{rel.label.toLowerCase()}</strong>. Cada decisión cuenta: en la vida real, reconocer
          las señales a tiempo también.
        </p>
        <StateMeters pointsM={game.points_m} pointsR={game.points_r} />
        <Link to={`/historial/${game.id}`} className="btn btn-primary btn-block">
          <BookHeart size={20} /> Revisar mis decisiones
        </Link>
        <Link to="/recursos" className="btn btn-block">
          <LifeBuoy size={20} /> Recursos de ayuda
        </Link>
        <Link to="/inicio" className="btn btn-ghost btn-block">
          <Home size={20} /> Inicio
        </Link>
      </div>
    </section>
  );
}
