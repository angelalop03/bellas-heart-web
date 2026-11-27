import { firstDecisionOfScene, firstSceneOfChapter, type Story } from './story';
import { clampPoints, psychState } from './states';
import type { Option } from './types';

/** Posición dentro de un capítulo. Se usa para el tutorial, que no se guarda en la base de datos. */
export interface LocalProgress {
  chapterId: number;
  sceneId: number | null;
  decisionId: number | null;
  points_m: number;
  points_r: number;
  finished: boolean;
}

export function startChapter(story: Story, chapterId: number): LocalProgress {
  const scene = firstSceneOfChapter(story, chapterId);
  const decision = scene ? firstDecisionOfScene(story, scene.id) : undefined;
  return {
    chapterId,
    sceneId: scene?.id ?? null,
    decisionId: decision?.id ?? null,
    points_m: 0,
    points_r: 0,
    finished: !decision,
  };
}

/** Opciones visibles en la decisión actual según el estado psicológico de Bella. */
export function availableOptions(story: Story, decisionId: number | null, pointsM: number): Option[] {
  if (decisionId == null) return [];
  const state = psychState(pointsM);
  return (story.optionsByDecision.get(decisionId) ?? []).filter(
    (o) => !o.required_psychological_state || o.required_psychological_state === state,
  );
}

/** Misma lógica que la función SQL choose_option, limitada a un capítulo. */
export function chooseLocal(story: Story, progress: LocalProgress, option: Option): LocalProgress {
  const next: LocalProgress = {
    ...progress,
    points_m: clampPoints(progress.points_m + option.points_m),
    points_r: clampPoints(progress.points_r + option.points_r),
  };
  if (option.next_decision_id != null) {
    next.decisionId = option.next_decision_id;
    return next;
  }
  const scenes = story.scenesByChapter.get(progress.chapterId) ?? [];
  const current = progress.sceneId != null ? story.sceneById.get(progress.sceneId) : undefined;
  const following = current ? scenes.find((s) => s.scene_order === current.scene_order + 1) : undefined;
  if (following) {
    next.sceneId = following.id;
    next.decisionId = firstDecisionOfScene(story, following.id)?.id ?? null;
    return next;
  }
  next.finished = true;
  next.decisionId = null;
  return next;
}
