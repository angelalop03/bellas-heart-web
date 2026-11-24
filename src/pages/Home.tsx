import { Link } from 'react-router-dom';
import { BookHeart, BookOpen, Gamepad2, GraduationCap, LifeBuoy, Newspaper, Play, Plus } from 'lucide-react';
import { useAuth } from '../lib/auth';
import { api } from '../lib/api';
import { useAsync } from '../lib/useAsync';
import { Backdrop } from '../components/ui';
import { StateMeters } from '../components/StateMeters';
import { BACKDROPS, img } from '../lib/assets';
import { chapterLabel } from '../lib/story';
import type { Chapter } from '../lib/types';

export function Home() {
  const { user: profile } = useAuth();
  const { data: cont, loading } = useAsync(() => api.continueGame(), [profile?.id]);
  const name = profile?.first_name || profile?.username || '';
  const chapter =
    cont?.chapter_number != null ? ({ name: cont.chapter_name ?? '', chapter_number: cont.chapter_number } as Chapter) : null;

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
        <Link to={`/jugar/${cont.id}`} className="continue-card card-link" aria-label={`Continuar ${cont.name}`}>
          <div className="continue-bg" style={{ backgroundImage: `url("${img.scene(cont.background_image)}")` }} />
          <div className="continue-body">
            <span className="badge badge-pink">Continuar</span>
            <h2 className="title-md" style={{ fontSize: '1.4rem' }}>{cont.name}</h2>
            <p className="muted">
              {cont.history_title}
              {chapter ? ` · ${chapterLabel(chapter)}` : ''}
            </p>
            <StateMeters pointsM={cont.points_m} pointsR={cont.points_r} compact />
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
