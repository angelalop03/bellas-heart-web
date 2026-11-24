// Música de fondo del juego (la misma pista lo-fi del original).
// El volumen y el silencio se recuerdan en el dispositivo.

const KEY = 'bh-music';
let audio: HTMLAudioElement | null = null;

interface MusicPrefs {
  volume: number;
  muted: boolean;
}

export function getMusicPrefs(): MusicPrefs {
  try {
    const raw = localStorage.getItem(KEY);
    if (raw) return { volume: 0.4, muted: false, ...JSON.parse(raw) };
  } catch {
    /* ignorado */
  }
  return { volume: 0.4, muted: false };
}

function savePrefs(prefs: MusicPrefs) {
  try {
    localStorage.setItem(KEY, JSON.stringify(prefs));
  } catch {
    /* ignorado */
  }
}

function ensureAudio(): HTMLAudioElement {
  if (!audio) {
    audio = new Audio('/music/musica-lofi.mp3');
    audio.loop = true;
    audio.preload = 'auto';
  }
  const prefs = getMusicPrefs();
  audio.volume = prefs.volume;
  audio.muted = prefs.muted;
  return audio;
}

/** Debe llamarse tras un toque del usuario (los navegadores bloquean el autoplay). */
export function playMusic() {
  const a = ensureAudio();
  if (a.paused) a.play().catch(() => undefined);
}

export function stopMusic() {
  if (audio && !audio.paused) audio.pause();
}

export function setMusicVolume(volume: number) {
  const prefs = { ...getMusicPrefs(), volume };
  savePrefs(prefs);
  if (audio) audio.volume = volume;
}

export function setMusicMuted(muted: boolean) {
  const prefs = { ...getMusicPrefs(), muted };
  savePrefs(prefs);
  if (audio) audio.muted = muted;
}
