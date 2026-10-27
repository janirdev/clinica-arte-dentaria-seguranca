# Clínica Arte Dentária — Laboratório de Segurança

Aplicação de **ensino** composta por um site (Next.js) para marcação de consultas e
consulta de histórico, e uma API (Spring Boot). A aplicação é **propositadamente
vulnerável** e serve para demonstrar, numa aula, falhas comuns em sistemas web.

> ⚠️ **Aviso**
> Este projeto é inseguro de propósito. Use-o **apenas em `localhost` ou numa rede de
> laboratório isolada**, nunca exposto à internet, e **nunca com dados reais de pessoas**.
> É material didático no espírito do OWASP Juice Shop / DVWA / WebGoat.

## Arquitetura

```
clinica-arte-dentaria/
├── backend/     API REST em Spring Boot + PostgreSQL
├── frontend/    Site em Next.js (App Router) que consome a API no cliente
└── docs/        Material da aula: lista de vulnerabilidades e exercícios
```

## Como correr

### 0. Base de dados PostgreSQL (porta 5433)
O backend liga-se a `jdbc:postgresql://localhost:5433/clinica` (utilizador `postgres`,
senha `postgres`). Se não tiver um PostgreSQL nessa porta, pode arrancar um com Docker:
```bash
docker run --name pg-clinica -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=clinica -p 5433:5432 -d postgres:16
```
> Ajuste utilizador/senha/base em `backend/src/main/resources/application.properties` se o seu PostgreSQL for diferente.
> As tabelas e os dados são criados por **migrations Flyway** (`backend/src/main/resources/db/migration/`), que correm automaticamente no arranque.
> Para recomeçar do zero, recrie a base de dados (ou apague o contentor Docker) antes de arrancar o backend.

### 1. Backend VULNERÁVEL (API) — porta 8080
Requisitos: Java 17+ e Maven.
```bash
cd backend
mvn spring-boot:run
```
- API: http://localhost:8080/api

### 1b. Backend SEGURO (versão corrigida) — porta 8082
Versão com as falhas fechadas, para comparar na aula. Usa a mesma base de dados
num schema próprio (`seguro`), por isso não precisa de configuração extra.
```bash
cd backend-seguro
mvn spring-boot:run
```
- API: http://localhost:8082/api

> Podes correr os dois ao mesmo tempo (portas 8080 e 8082) e alternar no site.
> (8081 ficou de fora por já estar ocupada pelo contentor mongo-express.)

### 2. Frontend (site) — porta 3000
Requisitos: Node.js 18+.
```bash
cd frontend
npm install
npm run dev
```
Abrir http://localhost:3000

## Contas de teste (fictícias)

| Email             | Senha     | Perfil   |
|-------------------|-----------|----------|
| ana@exemplo.pt    | ana123    | paciente |
| bruno@exemplo.pt  | bruno123  | paciente |
| admin@exemplo.pt  | admin123  | admin    |

## Para a aula

- **`docs/LAB-VULNERABILIDADES.md`** — cada falha intencional, onde está no código e como corrigir.
- **`docs/EXERCICIOS-ATAQUE.md`** — roteiro para a equipa que explora as falhas (equipa vermelha) e a que as corrige (equipa azul), tudo contra este alvo local.



## RUN UI & API
1. cd frontend && npm install && npm run dev (UI)
2. cd backend && mvn spring-boot:run (API)


"# clinica-arte-dentaria-seguranca" 
