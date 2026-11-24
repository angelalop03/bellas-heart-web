// Las imágenes de la base de datos se guardan con su nombre original ("Park.png");
// en la web se sirven convertidas a WebP con el mismo nombre ("/img/scenes/Park.webp").
function webp(name: string): string {
  return encodeURIComponent(name.replace(/\.(png|jpe?g|webp)$/i, '')) + '.webp';
}

export const img = {
  scene: (name: string | null | undefined) => (name ? `/img/scenes/${webp(name)}` : '/img/scenes/Black.webp'),
  character: (name: string | null | undefined) => (name ? `/img/characters/${webp(name)}` : null),
  chapter: (name: string | null | undefined) => `/img/chapters/${webp(name || 'Default_Chapter.png')}`,
  history: (name: string | null | undefined) => `/img/histories/${webp(name || 'Default_History.png')}`,
  avatar: (name: string | null | undefined) => `/img/avatars/${webp(name || 'default_image')}`,
  ui: (name: string) => `/img/ui/${webp(name)}`,
};

export const AVATARS = ['default_image', 'Perfil_2', 'Perfil_3', 'Perfil_4'];

/** Fondo ilustrado para cada sección (son las ilustraciones del juego original). */
export const BACKDROPS = {
  welcome: img.ui('Fondo'),
  home: img.ui('Fondo'),
  newGame: img.ui('Door'),
  games: img.ui('FondoPartidas'),
  history: img.ui('Bloc'),
  forum: img.ui('Newspaper2'),
  resources: img.ui('StrongBox'),
  profile: img.ui('Wardrobe'),
  admin: img.ui('Book3'),
  night: img.ui('FondoN'),
};

/** Nombre que se muestra para cada personaje del juego. */
export function speakerName(characterName: string | undefined | null): string | null {
  if (!characterName) return null;
  const map: Record<string, string> = {
    Angela: 'Ángela',
    David_Phone: 'David · mensaje',
    Eva_Call: 'Eva · videollamada',
    Eva_Phone: 'Mensaje a Eva',
    Note: 'Nota',
  };
  return map[characterName] ?? characterName;
}

/** Precarga imágenes para que las transiciones entre escenas sean instantáneas. */
export function preload(urls: (string | null | undefined)[]) {
  for (const url of urls) {
    if (!url) continue;
    const image = new Image();
    image.decoding = 'async';
    image.src = url;
  }
}
