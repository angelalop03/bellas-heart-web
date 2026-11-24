import { Link } from 'react-router-dom';
import { LifeBuoy, Sparkles } from 'lucide-react';
import { Backdrop } from '../components/ui';
import { InstallPrompt } from '../components/InstallPrompt';
import { BACKDROPS, img } from '../lib/assets';

export function Welcome() {
  return (
    <div className="welcome">
      <Backdrop src={BACKDROPS.welcome} />
      <div className="welcome-inner">
        <img className="welcome-logo" src={img.ui('logo')} alt="Bella's Heart" width={420} height={264} />
        <p className="welcome-tagline">
          Bella y David acaban de mudarse juntos. Tus decisiones irán dando forma a su relación… ¿sabrás ver las señales?
        </p>
        <div className="glass welcome-card">
          <Link to="/registro" className="btn btn-primary btn-block">
            <Sparkles size={20} /> Crear cuenta y jugar
          </Link>
          <Link to="/entrar" className="btn btn-block">
            Ya tengo cuenta
          </Link>
          <Link to="/tutorial" className="btn btn-ghost btn-block">
            Probar el tutorial sin cuenta
          </Link>
          <InstallPrompt />
        </div>
        <Link to="/recursos" className="help-chip welcome-help">
          <LifeBuoy size={16} /> ¿Necesitas ayuda? Llama al 016
        </Link>
      </div>
    </div>
  );
}
