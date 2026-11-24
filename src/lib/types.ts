export type PsychState = 'STABLE' | 'SLIGHTLY_ANXIOUS' | 'ANXIOUS' | 'DISTRESSED';
export type RelationState = 'CONFIDENT' | 'LOW_TENSION' | 'HIGH_TENSION' | 'CRITICAL';

export interface History {
  id: number;
  title: string;
  description: string;
  image_path: string;
  published: boolean;
}

export interface Chapter {
  id: number;
  history_id: number | null;
  name: string;
  chapter_number: string;
  image_path: string | null;
  required_relationship_state: RelationState | null;
}

export interface Scene {
  id: number;
  chapter_id: number;
  background_image: string | null;
  scene_order: number;
}

export interface Decision {
  id: number;
  scene_id: number;
  decision_order: number;
  character_id: number | null;
  dialogue: string;
}

export interface Option {
  id: number;
  decision_id: number;
  text: string;
  points_m: number;
  points_r: number;
  next_decision_id: number | null;
  required_psychological_state: PsychState | null;
}

export interface Character {
  id: number;
  name: string;
  image_path: string | null;
}

export interface StoryData {
  chapters: Chapter[];
  scenes: Scene[];
  decisions: Decision[];
  options: Option[];
  characters: Character[];
}

export interface Profile {
  id: string;
  email?: string;
  username: string;
  first_name: string;
  last_name: string;
  avatar: string;
  role: 'player' | 'admin';
  created_at: string;
}

export interface Game {
  id: number;
  user_id: string;
  history_id: number;
  name: string;
  current_chapter_id: number | null;
  current_scene_id: number | null;
  current_decision_id: number | null;
  points_m: number;
  points_r: number;
  state: 'IN_PROGRESS' | 'FINISHED';
  created_at: string;
  updated_at: string;
}

export interface GameChapter {
  game_id: number;
  chapter_id: number;
  unlocked_at: string;
  played_at: string | null;
}

export interface GameChoice {
  id: number;
  game_id: number;
  chapter_id: number | null;
  decision_id: number | null;
  option_id: number | null;
  points_m: number;
  points_r: number;
  created_at: string;
}

export interface News {
  id: number;
  title: string;
  body: string;
  created_at: string;
}

export interface ChooseResult {
  event: 'decision' | 'scene' | 'chapter_end';
  next_chapter_id: number | null;
  finished: boolean;
  game: Game;
}
