import { Backdrop, PageHead } from '../components/ui';
import { BACKDROPS, img } from '../lib/assets';
import { useAuth } from '../lib/auth';

export function Credits() {
  const { user } = useAuth();
  return (
    <div className="page">
      <Backdrop src={BACKDROPS.night} />
      <PageHead title="Sobre el proyecto" back={user ? '/inicio' : '/'} />
      <article className="glass article" style={{ textAlign: 'center' }}>
        <img src={img.ui('logo')} alt="Bella's Heart" style={{ width: 'min(70%, 280px)', margin: '0 auto' }} />
        <p>
          <strong>Bella's Heart</strong> es una novela visual creada como Trabajo Fin de Grado para concienciar sobre la
          violencia de género: ayudar a reconocer las señales de una relación tóxica y dar a conocer los recursos de ayuda.
        </p>
        <p className="muted">
          Desarrollada por <strong>Ángela López Oliva</strong>
          <br />
          Trabajo Fin de Grado · Universidad de Sevilla
          <br />
          Grado en Ingeniería Informática – Ingeniería del Software
        </p>
        <p className="muted">
          Tutora: María del Carmen Romero Ternero
          <br />
          Cotutora: Rosa María Casado Mejía
        </p>
        <p className="faint">© 2025 Todos los derechos reservados.</p>
      </article>
    </div>
  );
}
