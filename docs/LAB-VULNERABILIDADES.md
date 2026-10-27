# Vulnerabilidades do laboratório

Lista das falhas **intencionais**, com localização no código, impacto e correção.
Mapeadas ao OWASP Top 10 quando aplicável.

> Uso exclusivamente didático, contra o alvo local. Não aplicar estas técnicas a
> sistemas de terceiros sem autorização por escrito — é a diferença entre uma aula
> e um crime.

---

## 1. APIs sem autenticação (Broken Authentication)
**OWASP A07 / A01**
- **Onde:** `backend/.../controller/ConsultaController.java`, `PacienteController.java` — nenhum endpoint exige token.
- **Impacto:** qualquer pessoa chama a API diretamente (ex. `curl http://localhost:8080/api/consultas`) e lê/altera tudo.
- **Correção:** Spring Security + filtro que exige um JWT assinado e verificado em todos os endpoints protegidos.

## 2. Consumo da API feito no cliente + "segredos" no browser
**OWASP A02 / má arquitetura**
- **Onde:** `frontend/lib/api.js` (`API_URL`, `API_KEY_DEMO`) e todas as páginas `"use client"`.
- **Impacto:** a URL da API e a "chave" chegam ao browser e são visíveis em DevTools → Sources/Network. Nada do que vai para o cliente é segredo.
- **Correção:** chamadas sensíveis a partir do servidor (Route Handlers / BFF); segredos só no servidor (variáveis de ambiente), nunca com prefixo público.

## 3. IDOR — acesso ao histórico de outro paciente
**OWASP A01 (Broken Access Control)**
- **Onde:** `ConsultaController.porPaciente(...)` + `frontend/app/historico/page.js` (campo "ID do paciente").
- **Impacto:** trocar o `id` devolve consultas e observações clínicas de outra pessoa.
- **Correção:** derivar o dono do token no servidor; recusar (403) acesso a recursos de outro utilizador.

## 4. Controlo de acesso só no cliente
**OWASP A01**
- **Onde:** `frontend/app/admin/page.js` decide o acesso com `localStorage.perfil === "admin"`.
- **Impacto:** o utilizador edita o `localStorage` e entra no painel; e a API devolve tudo na mesma.
- **Correção:** autorização **no servidor** por papel (role); o cliente nunca é fonte de verdade.

## 5. Exposição de dados sensíveis (senhas em texto simples)
**OWASP A02**
- **Onde:** `Usuario.senha` sem hash; `GET /api/pacientes` devolve a senha.
- **Impacto:** fuga imediata de credenciais.
- **Correção:** guardar apenas o hash (BCrypt/Argon2); nunca serializar a senha (DTO sem o campo).

## 6. Mass Assignment / escalada de privilégios no registo
**OWASP A08 / A01**
- **Onde:** `PacienteController.criar(...)` aceita o campo `perfil` vindo do cliente.
- **Impacto:** `POST /api/pacientes` com `"perfil":"admin"` cria um administrador.
- **Correção:** ignorar `perfil` no corpo; forçar `"paciente"` no servidor; usar DTO de entrada restrito.

## 7. SQL Injection
**OWASP A03 (Injection)**
- **Onde:** `ConsultaController.buscar(...)` concatena o termo na query.
- **Impacto:** `?termo=' OR '1'='1` devolve tudo; variantes permitem ler/alterar mais dados.
- **Correção:** consultas parametrizadas (PreparedStatement / query do JPA com parâmetros).

## 8. CORS totalmente aberto
**OWASP A05 (Misconfiguration)**
- **Onde:** `config/CorsConfig.java` (`allowedOrigins("*")`).
- **Impacto:** qualquer site aberto no browser da vítima chama a API; junto com a falta de auth, agrava tudo.
- **Correção:** permitir só as origens conhecidas; nunca `*` com credenciais.

## 9. Falta de verificação de dono em PUT/DELETE → corrupção de dados
**OWASP A01**
- **Onde:** `ConsultaController.atualizar(...)` e `apagar(...)`.
- **Impacto:** alterar ou apagar consultas de qualquer paciente (ver cenários de corrupção em `EXERCICIOS-ATAQUE.md`).
- **Correção:** verificar o dono no servidor antes de gravar/apagar; registar auditoria.

## 10. Credenciais da base de dados em texto simples na configuração
**OWASP A05**
- **Onde:** `application.properties` — utilizador/senha do PostgreSQL (`postgres`/`postgres`) escritos no ficheiro e versionados.
- **Impacto:** quem lê o repositório obtém as credenciais da base de dados.
- **Correção:** usar variáveis de ambiente / gestor de segredos; nunca versionar credenciais; utilizador de base de dados com privilégios mínimos.

## 11. "Token" fraco e enumeração de utilizadores
**OWASP A07**
- **Onde:** `AuthController.login(...)` — token previsível `token-<id>-<perfil>` e mensagens que distinguem email/senha.
- **Impacto:** token forjável; descoberta de emails válidos.
- **Correção:** JWT assinado e com expiração; mensagem de erro genérica ("credenciais inválidas").

## 12. Sem limitação de pedidos — abuso de recursos / DoS
**OWASP A04 (Insecure Design) / API4 (Unrestricted Resource Consumption)**
- **Onde:** o backend vulnerável (8080) não limita o número de pedidos por cliente.
- **Impacto:** um único cliente pode inundar a API com pedidos (ex.: força bruta no login, raspagem de dados, exaustão de recursos), degradando o serviço para todos. É a base de um ataque de negação de serviço.
- **Correção (já aplicada no modo seguro, 8082):** *rate limiting* por IP em `RateLimitFilter` — acima de 60 pedidos/10s por IP, responde `429 Too Many Requests` com cabeçalho `Retry-After`.
- **Nota:** rate limiting na aplicação trava excesso de **uma** origem. Um DDoS real (muitas origens) mitiga-se mais acima (CDN/WAF, limites de infraestrutura, autoescala), não só no código.
