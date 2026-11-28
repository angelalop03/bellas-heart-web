-- Primeros capítulos más sensibles y reparto de finales 20 % / 20 % / 60 %.
--
-- * Capítulos 1 y 2: las respuestas que suman malestar valen el triple, así basta con
--   un par de malas decisiones para que Bella pase de «Estable» a «Algo inquieta».
-- * Capítulos 1 a 3: las respuestas que lo reducen restan al menos 4, para que se pueda
--   recuperar si se reacciona a tiempo.
--
-- Simulando 20.000 partidas eligiendo al azar: final 9.1 ≈ 20 %, 9.2 ≈ 19 %, 9.4 ≈ 61 %
-- (antes 28 % / 31 % / 40 %). Quien cede siempre sigue llegando al 9.4 y quien se protege, al 9.1.
-- Solo afecta a la historia principal, no al tutorial.

update options o
   set points_m = o.points_m * 3
  from decisions d
  join scenes s on s.id = d.scene_id
  join chapters c on c.id = s.chapter_id
 where o.decision_id = d.id
   and c.history_id is not null
   and c.chapter_number in ('1', '2')
   and o.points_m > 0;

update options o
   set points_m = least(o.points_m, -4)
  from decisions d
  join scenes s on s.id = d.scene_id
  join chapters c on c.id = s.chapter_id
 where o.decision_id = d.id
   and c.history_id is not null
   and c.chapter_number in ('1', '2', '3')
   and o.points_m < 0;
