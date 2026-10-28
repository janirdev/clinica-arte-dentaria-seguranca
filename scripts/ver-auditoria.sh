#!/usr/bin/env bash
# Mostra o registo de auditoria (todas as alteracoes capturadas na BD).
# Uso de laboratorio.
docker exec postgres_db psql -U postgres -d clinica -c "
  SELECT id, to_char(momento,'HH24:MI:SS') AS hora, operacao, tabela, registo_id,
         utilizador_bd, ip_cliente,
         COALESCE(dados_novos->>'procedimento', dados_antigos->>'procedimento') AS procedimento,
         COALESCE(dados_novos->>'estado',       dados_antigos->>'estado')       AS estado
  FROM public.auditoria
  ORDER BY id;
"
