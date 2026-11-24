import { Link } from 'react-router-dom';
import { Backdrop } from '../components/ui';
import { BACKDROPS } from '../lib/assets';

export function NotFound() {
  return (
    <div className="page center-screen">
      <Backdrop src={BACKDROPS.night} />
      <div className="glass modal" style={{ textAlign: 'center' }}>
        <h1 className="title-lg">Página no encontrada</h1>
        <p className="muted">Puede que el enlace haya cambiado.</p>
        <Link to="/" className="btn btn-primary">Ir al inicio</Link>
      </div>
    </div>
  );
}
