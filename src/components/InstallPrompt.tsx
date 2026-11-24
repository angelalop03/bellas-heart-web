import { useEffect, useState } from 'react';
import { Download, Share, SquarePlus } from 'lucide-react';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

let deferred: BeforeInstallPromptEvent | null = null;
window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferred = e as BeforeInstallPromptEvent;
});

function isStandalone() {
  return (
    window.matchMedia('(display-mode: standalone)').matches ||
    (navigator as Navigator & { standalone?: boolean }).standalone === true
  );
}

const isIOS = () => /iphone|ipad|ipod/i.test(navigator.userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);

/** Botón para añadir el juego a la pantalla de inicio (Android) o instrucciones (iPhone). */
export function InstallPrompt() {
  const [canPrompt, setCanPrompt] = useState(Boolean(deferred));
  const [showIOS, setShowIOS] = useState(false);
  const [installed, setInstalled] = useState(isStandalone());

  useEffect(() => {
    const onPrompt = () => setCanPrompt(true);
    const onInstalled = () => setInstalled(true);
    window.addEventListener('beforeinstallprompt', onPrompt);
    window.addEventListener('appinstalled', onInstalled);
    return () => {
      window.removeEventListener('beforeinstallprompt', onPrompt);
      window.removeEventListener('appinstalled', onInstalled);
    };
  }, []);

  if (installed) return null;

  if (canPrompt && deferred) {
    return (
      <button
        type="button"
        className="btn btn-block"
        onClick={async () => {
          await deferred?.prompt();
          const choice = await deferred?.userChoice;
          if (choice?.outcome === 'accepted') setInstalled(true);
          deferred = null;
          setCanPrompt(false);
        }}
      >
        <Download size={20} /> Añadir a la pantalla de inicio
      </button>
    );
  }

  if (isIOS()) {
    return (
      <div className="stack">
        <button type="button" className="btn btn-block" onClick={() => setShowIOS((v) => !v)} aria-expanded={showIOS}>
          <SquarePlus size={20} /> Añadir a la pantalla de inicio
        </button>
        {showIOS && (
          <div className="alert alert-info">
            En Safari, toca <Share size={16} style={{ verticalAlign: '-3px' }} /> <strong>Compartir</strong> y después{' '}
            <strong>Añadir a pantalla de inicio</strong>. El juego se abrirá como una app.
          </div>
        )}
      </div>
    );
  }
  return null;
}
