-- El final 3 se funde con el 4.
--
-- El final (capítulo 9) depende del malestar de Bella (migración 004). Hasta ahora:
--   Estable -> 9.1, Algo inquieta -> 9.2, Ansiosa -> 9.3, Angustiada -> 9.4.
-- Ahora «Ansiosa» también lleva al 9.4, para que más jugadoras vean el desenlace más grave
-- y solo se salve quien reconoce las señales, pide ayuda y se cuida.
-- El capítulo 9.3 se queda en la base de datos, pero ya no se alcanza.

create or replace function branch_state(p_metric text, p_malestar integer, p_tension integer)
returns text
language sql
immutable
as $$
  select case
           when p_metric = 'malestar' then
             case psychological_state(p_malestar)
               when 'STABLE' then 'CONFIDENT'
               when 'SLIGHTLY_ANXIOUS' then 'LOW_TENSION'
               else 'CRITICAL' -- Ansiosa o Angustiada: final 9.4
             end
           else relationship_state(p_tension)
         end;
$$;
