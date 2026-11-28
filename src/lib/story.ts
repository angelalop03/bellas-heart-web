import { api } from './api';
import type { Chapter, Character, Decision, Option, Scene, StoryData } from './types';

/** Historia ya indexada para consultas rápidas durante el juego. */
export interface Story {
  raw: StoryData;
  chapters: Chapter[];
  chapterById: Map<number, Chapter>;
  sceneById: Map<number, Scene>;
  scenesByChapter: Map<number, Scene[]>;
  decisionById: Map<number, Decision>;
  decisionsByScene: Map<number, Decision[]>;
  optionById: Map<number, Option>;
  optionsByDecision: Map<number, Option[]>;
  characterById: Map<number, Character>;
}

function group<T, K>(items: T[], key: (item: T) => K): Map<K, T[]> {
  const map = new Map<K, T[]>();
  for (const item of items) {
    const k = key(item);
    const list = map.get(k);
    if (list) list.push(item);
    else map.set(k, [item]);
  }
  return map;
}

export function indexStory(raw: StoryData): Story {
  const scenesByChapter = group(raw.scenes, (s) => s.chapter_id);
  scenesByChapter.forEach((list) => list.sort((a, b) => a.scene_order - b.scene_order || a.id - b.id));
  const decisionsByScene = group(raw.decisions, (d) => d.scene_id);
  decisionsByScene.forEach((list) => list.sort((a, b) => a.decision_order - b.decision_order || a.id - b.id));
  const optionsByDecision = group(raw.options, (o) => o.decision_id);
  optionsByDecision.forEach((list) => list.sort((a, b) => a.id - b.id));
  return {
    raw,
    chapters: raw.chapters,
    chapterById: new Map(raw.chapters.map((c) => [c.id, c])),
    sceneById: new Map(raw.scenes.map((s) => [s.id, s])),
    scenesByChapter,
    decisionById: new Map(raw.decisions.map((d) => [d.id, d])),
    decisionsByScene,
    optionById: new Map(raw.options.map((o) => [o.id, o])),
    optionsByDecision,
    characterById: new Map(raw.characters.map((c) => [c.id, c])),
  };
}

const memory = new Map<string, Promise<Story>>();
// v4: puntos de los primeros capítulos (migración 006)
const CACHE_PREFIX = 'bh-story-v4:';

/**
 * Descarga la historia completa (capítulos, escenas, diálogos y opciones) en una sola llamada.
 * Se guarda una copia en el dispositivo para abrirla al instante la próxima vez.
 * historyId null = capítulo tutorial.
 */
export function loadStory(historyId: number | null): Promise<Story> {
  const key = String(historyId ?? 'tutorial');
  const cached = memory.get(key);
  if (cached) return cached;

  const fetchFresh = async () => {
    const data = await api.story(historyId);
    try {
      localStorage.setItem(CACHE_PREFIX + key, JSON.stringify(data));
    } catch {
      /* sin espacio o modo privado: no pasa nada */
    }
    return indexStory(data as StoryData);
  };

  let promise: Promise<Story>;
  let stored: string | null = null;
  try {
    stored = localStorage.getItem(CACHE_PREFIX + key);
  } catch {
    stored = null;
  }
  if (stored) {
    // Copia local al instante; se actualiza en segundo plano para la próxima vez.
    promise = Promise.resolve(indexStory(JSON.parse(stored) as StoryData));
    fetchFresh()
      .then((fresh) => memory.set(key, Promise.resolve(fresh)))
      .catch(() => undefined);
  } else {
    promise = fetchFresh();
    promise.catch(() => memory.delete(key));
  }
  memory.set(key, promise);
  return promise;
}

export function firstDecisionOfScene(story: Story, sceneId: number): Decision | undefined {
  return story.decisionsByScene.get(sceneId)?.[0];
}

export function firstSceneOfChapter(story: Story, chapterId: number): Scene | undefined {
  return story.scenesByChapter.get(chapterId)?.[0];
}

export function chapterLabel(chapter: Chapter): string {
  if (chapter.chapter_number === '0') return chapter.name || 'Introducción';
  const base = chapter.chapter_number.split('.')[0];
  const name = chapter.name.trim();
  if (/^final$/i.test(name)) return 'Final';
  return `${name && !/^cap[ií]tulo$/i.test(name) ? name : 'Capítulo'} ${base}`;
}

/** Ordena "0", "1", ..., "6.1", "6.2" numéricamente. */
export function chapterSortKey(chapter: Chapter): number {
  const n = Number(chapter.chapter_number);
  return Number.isFinite(n) ? n : 999999;
}
