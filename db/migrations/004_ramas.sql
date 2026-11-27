-- =====================================================================
-- 004 · Ramas y finales coherentes con cómo juega Bella
--
-- Antes, la rama dependía solo de la tensión y estaba emparejada al revés: quien más
-- cedía llegaba a la rama en la que Bella va al viaje y sale con ayuda de Eva (9.1), y
-- quien más se protegía, a la rama en la que lo cancela y muere (9.4).
--
-- * Capítulos 6, 7 y 8: se siguen eligiendo por la tensión con David, pero intercambiados:
--   mucha tensión (Bella se planta) -> x.1 ; poca tensión (David controla sin resistencia) -> x.4.
-- * Final (9.x): se elige por el malestar de Bella (mismos umbrales que su estado: 12/25/38).
--   Poco malestar -> 9.1 (sale con ayuda de Eva) ; mucho malestar -> 9.4.
-- Comprobado simulando todas las partidas posibles: las 16 ramas son alcanzables.
-- =====================================================================

alter table chapters add column branch_metric text not null default 'tension'
  check (branch_metric in ('tension', 'malestar'));

update chapters set required_relationship_state = case required_relationship_state
    when 'CONFIDENT' then 'CRITICAL'
    when 'LOW_TENSION' then 'HIGH_TENSION'
    when 'HIGH_TENSION' then 'LOW_TENSION'
    when 'CRITICAL' then 'CONFIDENT' end
 where required_relationship_state is not null and split_part(chapter_number, '.', 1) in ('6', '7', '8');

update chapters set branch_metric = 'malestar' where split_part(chapter_number, '.', 1) = '9';

-- Nivel (con las etiquetas de required_relationship_state) según la medida de cada capítulo
create or replace function branch_state(p_metric text, p_malestar integer, p_tension integer)
returns text language sql immutable
as $$
  select case when p_metric = 'malestar' then
           case psychological_state(p_malestar)
             when 'STABLE' then 'CONFIDENT'
             when 'SLIGHTLY_ANXIOUS' then 'LOW_TENSION'
             when 'ANXIOUS' then 'HIGH_TENSION'
             else 'CRITICAL' end
         else relationship_state(p_tension) end;
$$;

create or replace function choose_option(p_user uuid, p_game_id bigint, p_option_id bigint)
returns jsonb language plpgsql
as $$
declare
  g games;
  o options;
  v_scene scenes;
  v_next_scene scenes;
  v_current_number text;
  v_current_group text;
  v_cand record;
  v_next_chapter bigint;
  v_event text;
begin
  select * into g from games where id = p_game_id for update;
  if g.id is null or g.user_id <> p_user then raise exception 'Partida no encontrada'; end if;
  if g.state <> 'IN_PROGRESS' then raise exception 'Esta partida ya ha terminado'; end if;

  select * into o from options where id = p_option_id;
  if o.id is null or o.decision_id is distinct from g.current_decision_id then
    raise exception 'Esa opción no corresponde a este momento de la historia';
  end if;
  if o.required_psychological_state is not null
     and o.required_psychological_state <> psychological_state(g.points_m) then
    raise exception 'Esa opción no está disponible ahora';
  end if;

  g.points_m := clamp_points(g.points_m + o.points_m);
  g.points_r := clamp_points(g.points_r + o.points_r);

  insert into game_choices (game_id, chapter_id, decision_id, option_id, points_m, points_r)
  values (g.id, g.current_chapter_id, o.decision_id, o.id, o.points_m, o.points_r);

  if o.next_decision_id is not null then
    -- Igual que el juego original: el fondo (escena) no cambia al saltar de decisión.
    g.current_decision_id := o.next_decision_id;
    v_event := 'decision';
  else
    select * into v_scene from scenes where id = g.current_scene_id;
    select * into v_next_scene from scenes
     where chapter_id = g.current_chapter_id and scene_order = v_scene.scene_order + 1
     order by id limit 1;

    if v_next_scene.id is not null then
      g.current_scene_id := v_next_scene.id;
      g.current_decision_id := first_decision_of_scene(v_next_scene.id);
      v_event := 'scene';
    else
      -- Fin de capítulo: se marca jugado y se desbloquea el siguiente según la relación
      v_event := 'chapter_end';
      update game_chapters set played_at = now()
       where game_id = g.id and chapter_id = g.current_chapter_id;

      select chapter_number into v_current_number from chapters where id = g.current_chapter_id;
      v_current_group := split_part(v_current_number, '.', 1);

      for v_cand in
        select id, chapter_number, required_relationship_state, branch_metric from chapters
         where history_id = g.history_id
           and chapter_sort_key(chapter_number) > chapter_sort_key(v_current_number)
         order by chapter_sort_key(chapter_number), id
      loop
        continue when split_part(v_cand.chapter_number, '.', 1) = v_current_group;
        if v_cand.required_relationship_state is null
           or v_cand.required_relationship_state = branch_state(v_cand.branch_metric, g.points_m, g.points_r) then
          v_next_chapter := v_cand.id;
          exit;
        end if;
      end loop;

      if v_next_chapter is not null then
        insert into game_chapters (game_id, chapter_id) values (g.id, v_next_chapter)
          on conflict do nothing;
        g.current_chapter_id := v_next_chapter;
        g.current_scene_id := first_scene_of_chapter(v_next_chapter);
        g.current_decision_id := first_decision_of_scene(g.current_scene_id);
      else
        g.state := 'FINISHED';
        g.current_decision_id := null;
      end if;
    end if;
  end if;

  update games set
    points_m = g.points_m, points_r = g.points_r,
    current_chapter_id = g.current_chapter_id, current_scene_id = g.current_scene_id,
    current_decision_id = g.current_decision_id, state = g.state, updated_at = now()
  where id = g.id;

  return jsonb_build_object(
    'event', v_event,
    'next_chapter_id', v_next_chapter,
    'finished', g.state = 'FINISHED',
    'game', to_jsonb(g)
  );
end;
$$;
