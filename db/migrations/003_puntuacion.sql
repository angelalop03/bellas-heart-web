-- =====================================================================
-- 003 · Nueva puntuación de las decisiones (revisión aprobada en docs/revision-puntuacion.xlsx)
--
-- * Las opciones de las 55 decisiones con elección real se puntúan según la gravedad de lo que
--   dice o hace Bella (escala de 18 categorías), no según su estado emocional.
-- * Umbrales recalculados simulando todas las partidas posibles, para que las 16 ramas
--   (incluida la 6.4, antes inalcanzable) y los estados de Bella sean alcanzables.
-- * Malestar y tensión quedan siempre entre 0 y 100.
-- * Erratas corregidas.
-- =====================================================================

-- Puntos por categoría (malestar, tensión)
-- BUSCA APOYO
update options set points_m = -4, points_r = 0 where id in (1079);
-- RECONOCE SEÑAL
update options set points_m = -3, points_r = 1 where id in (240, 803, 838, 876, 1349, 1551);
-- MANTIENE RED
update options set points_m = -2, points_r = 0 where id in (946, 947);
-- AUTOCUIDADO, PROTECCIÓN
update options set points_m = -2, points_r = 1 where id in (178, 259, 381, 435, 436, 634, 635, 910, 911, 1239, 1240, 1380, 1413, 1444, 1445, 1446, 1498, 1520, 1521, 1550, 1607, 1608, 1641);
-- LÍMITE SERENO
update options set points_m = -1, points_r = 2 where id in (35, 48, 51, 71, 91, 113, 128, 148, 200, 221, 295, 312, 338, 358, 495, 589, 725, 726, 766, 804, 1018, 1147, 1212, 1280, 1281, 1320, 1321, 1382, 1383, 1416, 1499, 1578);
-- CONFRONTA
update options set points_m = -1, points_r = 3 where id in (767, 1181, 1211, 1319, 1381, 1414, 1579);
-- NEUTRA
update options set points_m = 0, points_r = 0 where id in (26, 27, 29, 30, 380, 494, 532, 533, 676, 677, 912, 913, 986, 987, 1051, 1052, 1053, 1114, 1115, 1180, 1241, 1316, 1522, 1609);
-- EVITA
update options set points_m = 1, points_r = 0 where id in (129, 359, 591, 636, 948, 988, 1017, 1080, 1146, 1184, 1317, 1348, 1497);
-- CONCESIÓN, MINIMIZA
update options set points_m = 2, points_r = 0 where id in (36, 47, 177, 337, 437, 534, 535, 768, 805, 837, 840, 875, 877, 1019, 1020, 1081, 1182, 1183, 1242, 1243, 1350, 1384, 1447, 1552);
-- ANGUSTIA
update options set points_m = 3, points_r = 0 where id in (839, 841, 878, 879, 1078, 1082);
-- ACEPTA CONTROL, DEPENDENCIA, JUSTIFICA
update options set points_m = 3, points_r = 1 where id in (50, 70, 90, 112, 220, 239, 258, 313, 382, 438, 496, 497, 588, 590, 637, 678, 679, 727, 728, 729, 914, 1054, 1116, 1213, 1215, 1352, 1415, 1417, 1448, 1580);
-- AUTOCULPA
update options set points_m = 4, points_r = 1 where id in (149, 383, 638, 806, 989, 990, 1021, 1117, 1118, 1148, 1149, 1150, 1283, 1322, 1351, 1500, 1501, 1554, 1582);
-- AISLAMIENTO
update options set points_m = 4, points_r = 2 where id in (199, 294, 769, 770, 807, 949, 950, 1055, 1214, 1282, 1284, 1581);
-- NORMALIZA AGRESIÓN
update options set points_m = 5, points_r = 2 where id in (1523, 1524, 1553, 1610, 1611, 1642, 1643, 1644);

-- Umbrales nuevos (también en src/lib/states.ts)
create or replace function psychological_state(p_points integer)
returns text language sql immutable
as $$
  select case
    when greatest(p_points, 0) < 12 then 'STABLE'
    when p_points < 25 then 'SLIGHTLY_ANXIOUS'
    when p_points < 38 then 'ANXIOUS'
    else 'DISTRESSED' end;
$$;

create or replace function relationship_state(p_points integer)
returns text language sql immutable
as $$
  select case
    when greatest(p_points, 0) < 19 then 'CONFIDENT'
    when p_points < 24 then 'LOW_TENSION'
    when p_points < 29 then 'HIGH_TENSION'
    else 'CRITICAL' end;
$$;

-- Malestar y tensión siempre entre 0 y 100 (así bajar puntos sirve para recuperarse)
create or replace function clamp_points(p integer)
returns integer language sql immutable
as $$ select least(greatest(p, 0), 100); $$;

update games set points_m = clamp_points(points_m), points_r = clamp_points(points_r);

-- Elegir opción: igual que antes, pero el malestar y la tensión no salen de 0–100
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
  v_rel text;
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
      v_rel := relationship_state(g.points_r);

      for v_cand in
        select id, chapter_number, required_relationship_state from chapters
         where history_id = g.history_id
           and chapter_sort_key(chapter_number) > chapter_sort_key(v_current_number)
         order by chapter_sort_key(chapter_number), id
      loop
        continue when split_part(v_cand.chapter_number, '.', 1) = v_current_group;
        if v_cand.required_relationship_state is null or v_cand.required_relationship_state = v_rel then
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

-- Erratas
update options set text = replace(text, 'Estoy tan feliz poe esto', 'Estoy tan feliz por esto') where id = 29;
update options set text = 'Si eso es lo que necesitas... lo borro. Solo me cuesta entenderlo' where id = 1282;
update options set text = rtrim(text, ')') where id = 1500;
update options set text = ltrim(text, ')') where id = 1610;
update decisions set dialogue = replace(dialogue, 'violencia muchista', 'violencia machista') where id = 1675;
