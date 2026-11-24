import { Link } from 'react-router-dom';
import { BookHeart, BookOpen, Gamepad2, GraduationCap, LifeBuoy, Newspaper, Play, Plus } from 'lucide-react';
import { useAuth } from '../lib/auth';
import { supabase } from '../lib/supabase';
import { unwrap, useAsync } from '../lib/useAsync';
import { Backdrop } from '../components/ui';
import { StateMeters } from '../components/StateMeters';
import { BACKDROPS, img } from '../lib/assets';
import type { Chapter, Game, History, Scene } from '../lib/types';
import { chapterLabel } from '../lib/story';

interface ContinueInfo {
  game: Game;
  history: History | null;
  chapter: Chapter | null;
  scene: Scene | null;
}

async function loadContinue(userId: string | undefined): Promise<ContinueInfo | null> {
  if (!userId) return null;
  // Filtrar por usuaria: una cuenta admin puede ver todas las partidas
  const games = unwrap(
    await supabase.from('games').select('*').eq('user_id', userId).eq('state', 'IN_PROGRESS').order('updated_at', { ascending: false }).limit(1),
  ) as Game[];
  const game = games[0];
  if (!game) return null;
  const [history, chapter, scene] = await Promise.all([
    supabase.from('histories').select('*').eq('id', game.history_id).maybeSingle(),
    game.current_chapter_id ? supabase.from('chapters').select('*').eq('id', game.current_chapter_id).maybeSingle() : null,
    game.current_scene_id ? supabase.from('scenes').select('*').eq('id', game.current_scene_id).maybeSingle() : null,
  ]);
  return {
    game,
    history: (history.data as History | null) ?? null,
    chapter: (chapter?.data as Chapter | null) ?? null,
    scene: (scene?.data as Scene | null) ?? null,
  };
}

export function Home() {
  const { profile, session } = useAuth();
  const userId = session?.user.id;
  const { data: cont, loading } = useAsync(() => loadContinue(userId), [userId]);
  const name = profile?.first_name || profile?.username || '';

  return (
    <div className="page">
      <Backdrop src={BACKDROPS.home} />
      <section className="home-hello">
        <img className="avatar avatar-lg" src={img.avatar(profile?.avatar)} alt="" />
        <div>
          <p className="muted">Hola de nuevo,</p>
          <h1 className="title-lg">{name}</h1>
        </div>
      </section>

      {loading ? (
        <div className="card continue-card skeleton" aria-hidden="true" />
      ) : cont ? (
        <Link to={`/jugar/${cont.game.id}`} className="continue-card card-link" aria-label={`Continuar ${cont.game.name}`}>
          <div className="continue-bg" style={{ backgroundImage: `url("${img.scene(cont.scene?.background_image)}")` }} />
          <div className="continue-body">
            <span className="badge badge-pink">Continuar</span>
            <h2 className="title-md" style={{ fontSize: '1.4rem' }}>{cont.game.name}</h2>
            <p className="muted">
              {cont.history?.title}
              {cont.chapter ? ` · ${chapterLabel(cont.chapter)}` : ''}
            </p>
            <StateMeters pointsM={cont.game.points_m} pointsR={cont.game.points_r} compact />
            <span className="btn btn-primary continue-play">
              <Play size={20} fill="currentColor" /> Seguir jugando
            </span>
          </div>
        </Link>
      ) : (
        <Link to="/nueva" className="continue-card card-link">
          <div className="continue-bg" style={{ backgroundImage: `url("${img.ui('Door')}")` }} />
          <div className="continue-body">
            <span className="badge badge-pink">Empieza aquí</span>
            <h2 className="title-md" style={{ fontSize: '1.4rem' }}>Bella y David</h2>
            <p className="muted">Crea tu primera partida y descubre cómo evoluciona su relación.</p>
            <span className="btn btn-primary continue-play">
              <Plus size={20} /> Nueva partida
            </span>
          </div>
        </Link>
      )}

      <section className="quick-grid" aria-label="Accesos rápidos">
        <QuickLink to="/nueva" icon={<Plus />} title="Nueva partida" text="Elige una historia" />
        <QuickLink to="/partidas" icon={<Gamepad2 />} title="Mis partidas" text="Capítulos y progreso" />
        <QuickLink to="/tutorial" icon={<GraduationCap />} title="Tutorial" text="Aprende a jugar" />
        <QuickLink to="/historial" icon={<BookHeart />} title="Historial" text="Tus decisiones" />
        <QuickLink to="/foro" icon={<Newspaper />} title="Noticias" text="Actualidad y datos" />
        <QuickLink to="/recursos" icon={<LifeBuoy />} title="Recursos" text="016 y ayuda" highlight />
      </section>

      <p className="faint" style={{ textAlign: 'center' }}>
        <Link to="/creditos" className="faint">
          <BookOpen size={14} style={{ verticalAlign: '-2px' }} /> Sobre el proyecto
        </Link>
      </p>
    </div>
  );
}

function QuickLink({ to, icon, title, text, highlight }: { to: string; icon: React.ReactNode; title: string; text: string; highlight?: boolean }) {
  return (
    <Link to={to} className={`card card-link quick ${highlight ? 'quick-highlight' : ''}`}>
      <span className="quick-icon">{icon}</span>
      <span className="quick-title">{title}</span>
      <span className="faint">{text}</span>
    </Link>
  );
}
