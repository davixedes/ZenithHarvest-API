# 🌾 Zenith Harvest — API Java

> **Seguro paramétrico agrícola via satélite.** Monitora lavouras por NDVI (Sentinel-2),
> detecta perdas com visão computacional + IA generativa, e paga sinistros via PIX em até 48h.
> SaaS B2B para seguradoras (Brasilseg, Porto, Mapfre, Tokio Marine).

Projeto da **Global Solution 2026/1 — FIAP** (grupo Zenith), disciplina **Java Advanced**.

---

## 📑 Índice

- [O problema e a solução](#-o-problema-e-a-solução)
- [Arquitetura](#-arquitetura)
- [Stack técnica](#-stack-técnica)
- [Estrutura do monorepo](#-estrutura-do-monorepo)
- [Fluxo principal — sinistro paramétrico](#-fluxo-principal--sinistro-paramétrico)
- [Banco de dados](#-banco-de-dados)
- [Segurança (JWT assimétrico)](#-segurança-jwt-assimétrico)
- [Mensageria (RabbitMQ)](#-mensageria-rabbitmq)
- [Inteligência Artificial (Spring AI)](#-inteligência-artificial-spring-ai)
- [Integração de satélite (Sentinel Hub)](#-integração-de-satélite-sentinel-hub)
- [API — endpoints REST](#-api--endpoints-rest)
- [Como rodar](#-como-rodar)
- [Testes e CI](#-testes-e-ci)
- [Requisitos do edital atendidos](#-requisitos-do-edital-atendidos)
- [Convenções de código](#-convenções-de-código)

---

## 💡 O problema e a solução

O produtor rural espera **meses** por um sinistro agrícola tradicional: vistoria presencial,
perícia, burocracia. O **Zenith Harvest** inverte isso com **seguro paramétrico**:

1. **Satélite** observa a lavoura (NDVI do Sentinel-2).
2. **Visão computacional + IA** decidem se houve perda.
3. **PIX** cai na conta do produtor em até 48h.

O produto é vendido para seguradoras como SaaS B2B.

---

## 🏛 Arquitetura

Monorepo de **3 microsserviços Java (Spring Boot)** + infraestrutura (Postgres, MongoDB,
RabbitMQ, Ollama), tudo orquestrado via Docker Compose.

```
                              ┌───────────────────────────┐
        mobile / front  ───▶  │   gateway  (porta 8080)   │  Borda: roteamento + valida JWT
                              │  Spring Cloud Gateway MVC  │
                              └─────────────┬─────────────┘
                          /auth/**, /api/** │ /api/ndvi/**, /api/chatbot
                    ┌───────────────────────┴────────────────────────┐
                    ▼                                                  ▼
        ┌───────────────────────┐                       ┌───────────────────────────┐
        │  core-svc (8081)      │   OpenFeign (síncrono) │   analise-svc (8082)      │
        │  CRUD do domínio      │ ─────────────────────▶ │   Visão computacional     │
        │  Auth/JWT, financeiro │                        │   (NDVI/satélite) + IA    │
        │  Publica eventos      │                        │   (Spring AI / Ollama)    │
        └───────┬───────────────┘                        └────────────┬──────────────┘
                │                                                      ▲
                │  RabbitMQ — sinistro.aberto (assíncrono)             │
                └──────────────────────────────────────────────────────┘
                                  sinistro.analisado

   PostgreSQL (39 tabelas)        MongoDB (4 collections)        Ollama (LLM local)
```

### Por que 3 serviços? (justificativa exigida pelo edital)

| Serviço | Porta | Responsabilidade | Por que separado |
|---|---|---|---|
| **`gateway`** | 8080 | Borda única. Roteamento, validação de JWT, ponto de CORS. | Isola a borda do domínio; permite escalar e proteger a entrada sem tocar na regra de negócio. |
| **`core-svc`** | 8081 | Coração transacional. CRUD de todo o domínio, auth/JWT, financeiro. Publica eventos. | Concentra o estado consistente (Postgres + transações). É o serviço mais estável. |
| **`analise-svc`** | 8082 | Processamento pesado: visão computacional (NDVI) e IA generativa. Consome eventos. | Carga e dependências (LLM, satélite) muito diferentes do core — escala de forma independente e não derruba o CRUD se a IA travar. |

**Comunicação:**
- **Síncrona** — `core-svc` → `analise-svc` via **OpenFeign** (ex.: histórico NDVI de um talhão).
- **Assíncrona** — `core-svc` publica `sinistro.aberto` no **RabbitMQ**; `analise-svc` consome,
  processa e devolve `sinistro.analisado`.

> **Nota sobre o gateway:** apesar do nome "reativo" no domínio, a implementação usa
> **Spring Cloud Gateway Server WebMVC** (`spring-cloud-starter-gateway-server-webmvc`),
> com rotas definidas programaticamente em `GatewayRoutesConfig`.

---

## 🧰 Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | **Java 21** |
| Framework | **Spring Boot 3.5.14** |
| Build | **Maven** multi-módulo (parent POM centraliza versões) |
| Gateway | Spring Cloud Gateway Server WebMVC (Spring Cloud `2025.0.2`) |
| Persistência relacional | **PostgreSQL 16** + Spring Data JPA / Hibernate |
| Persistência NoSQL | **MongoDB 7** (séries temporais NDVI + auditoria) |
| Mensageria | **RabbitMQ 3.13** (Spring AMQP) |
| Segurança | **Spring Security** + OAuth2 Resource Server + **JWT RS256** |
| IA generativa | **Spring AI 1.1.7** + **Ollama** (LLM local) |
| Comunicação interserviços | **OpenFeign** |
| HATEOAS / Cache / Validation | Spring HATEOAS, Spring Cache, Bean Validation |
| Documentação | **SpringDoc OpenAPI 2.8.13** (Swagger UI) |
| Containers | Docker + Docker Compose |
| CI | GitHub Actions (`mvn verify`) |

---

## 📁 Estrutura do monorepo

```
Zenith-API/
 ├─ pom.xml                  # parent POM (Maven multi-módulo) — centraliza versões/BOMs
 ├─ docker-compose.yml       # Postgres + MongoDB + RabbitMQ + Ollama + 3 serviços
 ├─ Dockerfile               # imagem única parametrizada por --build-arg MODULE=<svc>
 ├─ CLAUDE.md                # guia para assistentes de IA
 ├─ .github/workflows/ci.yml # pipeline de build & test
 ├─ db/
 │   ├─ schema/zenith_harvest_postgres.sql   # DDL canônico (fonte de verdade — 39 tabelas)
 │   ├─ seed/zenith_harvest_seed.sql         # seed dos lookups (idempotente)
 │   └─ dbml/zenith_harvest.dbml             # modelo visual (dbdiagram.io)
 ├─ gateway/                 # com.fiap.zenith.gateway
 ├─ core-svc/                # com.fiap.zenith.core
 └─ analise-svc/             # com.fiap.zenith.analise_svc
```

GroupId único: **`com.fiap.zenith`**. Artifacts: `gateway`, `core-svc`, `analise-svc`.

### Organização interna (camadas) de cada serviço

```
src/main/java/com/fiap/zenith/<svc>/
 ├─ config/        # Security, OpenAPI, RabbitMQ, Feign, Cache
 ├─ domain/
 │   ├─ entity/    # @Entity JPA
 │   ├─ repository/# interfaces Spring Data
 │   ├─ enums/     # enums de situação
 │   └─ document/  # @Document MongoDB (analise-svc)
 ├─ application/
 │   ├─ service/   # regras de negócio
 │   ├─ dto/       # request/response (records) — nunca expõe @Entity
 │   ├─ mapper/    # entity ⇄ DTO
 │   └─ exception/ # exceções de domínio
 ├─ web/
 │   ├─ controller/# @RestController
 │   └─ handler/   # GlobalExceptionHandler
 └─ infra/
     ├─ feign/     # clients para outros serviços
     ├─ messaging/ # publishers/consumers RabbitMQ
     └─ satellite/ # integração Sentinel Hub (analise-svc)
```

---

## 🔄 Fluxo principal — sinistro paramétrico

```
1. Produtor/seguradora abre sinistro
        │  POST /api/claims  (core-svc)
        ▼
2. core-svc persiste o Claim (Postgres) e publica evento
        │  RabbitMQ → exchange "sinistros" (fanout) → fila "sinistro.aberto"
        ▼
3. analise-svc consome o evento (SinistroAbertoConsumer)
        │  • busca NDVI do talhão (MongoDB / Sentinel Hub)
        │  • roda análise satelital (SatelliteAnalysisService)
        │  • gera laudo em linguagem natural (LaudoService + Spring AI/Ollama)
        ▼
4. analise-svc publica o resultado
        │  RabbitMQ → exchange "analises" (fanout) → fila "sinistro.analisado"
        ▼
5. core-svc aprova/rejeita e dispara o pagamento PIX
        POST /api/claims/{id}/approve  →  Payment  →  PIX
```

A fila `sinistro.aberto` tem **DLQ** (`sinistro.aberto.dlq`) via dead-letter exchange
(`zenith.dlx`) para mensagens que falham no processamento.

---

## 🗄 Banco de dados

### PostgreSQL — 39 tabelas (relacional)

> **Fonte de verdade do schema:** [`db/schema/zenith_harvest_postgres.sql`](db/schema/zenith_harvest_postgres.sql).
> O app roda com `ddl-auto: validate` — **não cria nem altera** o schema; o DDL e o seed
> são aplicados pelo Postgres no primeiro boot (via `docker-entrypoint-initdb.d`).

Padrão de modelagem (estilo WMS corporativo) — **20 entidades/itens + 19 lookups**:

| Domínio | Tabelas |
|---|---|
| **Users (6)** | `User` · `Credential` · `Address` · `AccessLog` · `AccessLogAction`(L) |
| **Farm (6)** | `Crop` · `Farm` · `Plot` · `Biome`(L) · `ProductionSystem`(L) · `PlotSituation`(L) |
| **Insurance Catalog (6)** | `Insurer` · `Insurance` · `InsuranceQuote` · `InsurerSituation`(L) · `InsuranceSituation`(L) · `InsuranceQuoteSituation`(L) |
| **Operation (17)** | `Policy` · `PolicyItem` · `Claim` · `ClaimItem` · `SatelliteAnalysis` · `PreventiveAlert` + 11 lookups (`PolicySituation`, `ClaimSituation`, `ClaimEventType`, `ClaimCategory`, `ClaimSubCategory`, `SatelliteSource`, `SatelliteClass`, `AlertType`, `AlertSeverity`, `AlertSituation`, `RejectionReason`) |
| **Financial (4)** | `Payment` · `PaymentInvoice` · `PaymentType`(L) · `PaymentSituation`(L) |
| **Audit (1)** | `AuditLog` |

`(L)` = lookup (tabela de domínio que substitui ENUM).

**Convenções de modelagem (seguidas à risca):**
- Nomes de tabela/coluna em **PascalCase entre aspas duplas** (`"Claim"`, `"ClaimNumber"`) →
  `hibernate.globally_quoted_identifiers: true`.
- **PK de entidade** = `UUID` (`gen_random_uuid()`); **PK de lookup** = `INTEGER IDENTITY`.
- **`Code`** = inteiro incremental (1 em 1) só nas entidades operacionais (número "bonito" do front).
- Lookups de estado chamam-se **`*Situation`** (nunca `*Status`).
- **Soft delete triplo:** `CreatedAt` / `EditedAt` / `DeletedAt` (TIMESTAMPTZ).
- **Dinheiro** = `NUMERIC(15,2)` (nunca float) → `BigDecimal` no Java. **NDVI** = `NUMERIC(4,3)`.
- **NOT NULL conservador:** só no que existe na criação da linha; campo operacional é nullable.

### MongoDB — 4 collections (NoSQL)

- `historico_ndvi` — séries temporais de NDVI por talhão.
- `claim_audit_events` · `policy_audit_events` · `payment_audit_events` — trilhas de auditoria detalhadas.

**Auditoria híbrida:** `AuditLog` no Postgres (operações cross-cutting LGPD: LOGIN/LOGOUT/
ANONYMIZATION/BLOCK) + eventos de negócio detalhados no MongoDB.

---

## 🔐 Segurança (JWT assimétrico)

- **Login/registro** em `core-svc` (`/auth/register`, `/auth/login`), senha com **BCrypt**.
- JWT assinado em **RS256** com chave privada gerada em memória pelo `core-svc` (`JwtConfig`).
- A chave **pública** é publicada em **`/oauth2/jwks`** (formato JWKS).
- `gateway` e `analise-svc` validam o token consultando o JWKS do core (`jwk-set-uri`) —
  **não há segredo compartilhado** entre os serviços.
- Stateless: Spring Security OAuth2 Resource Server em todos os serviços.
- Expiração padrão do token: **8h** (`JWT_EXPIRATION_SECONDS=28800`).
- Role padrão na criação: `ROLE_USER`.

**Endpoints públicos:** `/auth/**`, `/oauth2/jwks`, `/api/lookups/**`, Swagger
(`/swagger-ui/**`, `/v3/api-docs/**`) e `/actuator/health`. Todo o resto exige JWT válido.

---

## 📨 Mensageria (RabbitMQ)

Topologia (espelhada entre `core-svc` e `analise-svc`):

| Recurso | Nome | Tipo |
|---|---|---|
| Exchange | `sinistros` | fanout |
| Exchange | `analises` | fanout |
| Exchange | `zenith.dlx` | direct (dead-letter) |
| Fila | `sinistro.aberto` | durable (+ DLQ) |
| Fila | `sinistro.analisado` | durable |
| Fila | `sinistro.aberto.dlq` | dead-letter |

Mensagens serializadas em **JSON** (`Jackson2JsonMessageConverter`). Painel de administração
em **http://localhost:15672** (user/pass: `zenith`/`zenith`).

---

## 🤖 Inteligência Artificial (Spring AI)

Provider padrão: **Ollama** (local, sem custo, sem API key) — garante demo sem depender de
crédito/internet. O código é abstraído via Spring AI: trocar para OpenAI/Gemini é mudar o
starter + `application.yaml`, sem tocar na lógica.

Usos de IA no `analise-svc`:
- **Geração de laudo** de sinistro em linguagem natural (`LaudoService`).
- **Chatbot** de suporte ao produtor (`ChatbotController`, `POST /api/chatbot`).
- (Roadmap) **RAG** sobre cartilhas SUSEP/Embrapa.

Modelo no Docker Compose: `qwen2:0.5b` (leve, baixado automaticamente pelo serviço
`ollama-pull` no boot). Default em dev local: `llama3.2`. Temperatura `0.3`.

---

## 🛰 Integração de satélite (Sentinel Hub)

O `analise-svc` integra com o **Sentinel Hub Statistics API** (coleção `sentinel-2-l2a`)
para obter o NDVI real dos talhões. É **desligado por padrão** (`SENTINEL_HUB_ENABLED=false`)
para a demo funcionar offline; quando habilitado, autentica via OAuth2 client-credentials.

Variáveis: `SENTINEL_HUB_CLIENT_ID`, `SENTINEL_HUB_CLIENT_SECRET`, `SENTINEL_HUB_DAYS_BACK`,
`SENTINEL_HUB_RESOLUTION_METERS` (ver `analise-svc/application.yaml`).

---

## 🌐 API — endpoints REST

Tudo passa pelo **gateway (8080)**. O gateway encaminha sem reescrever o path:
`/auth/**` e `/api/**` → core-svc; `/api/ndvi/**` e `/api/chatbot` → analise-svc.

### Autenticação (`core-svc`, público)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/register` | Cria conta (usuário + endereço, senha BCrypt) |
| `POST` | `/auth/login` | Autentica e devolve o JWT |
| `GET` | `/oauth2/jwks` | Chave pública RSA (JWKS) p/ validação do token |

### core-svc — domínio (`/api/**`, exige JWT)

| Recurso | Base path | Operações |
|---|---|---|
| Usuários | `/api/users` | `GET /me`, CRUD (`GET /{id}`, `GET`, `PUT /{id}`, `DELETE /{id}`) |
| Culturas | `/api/crops` | CRUD + soft delete |
| Fazendas | `/api/farms` | CRUD |
| Talhões | `/api/plots` | CRUD; `GET /api/farms/{farmId}/plots`; `GET /{id}/ndvi-historico` (Feign → analise-svc) |
| Sinistros | `/api/claims` | `POST` (publica evento RabbitMQ), `GET`, `POST /{id}/approve`, `POST /{id}/reject`, `DELETE /{id}` |
| Itens de sinistro | `/api/claim-items` | CRUD (filtro opcional por claim) |
| Seguradoras | `/api/insurers` | CRUD |
| Produtos de seguro | `/api/insurances` | CRUD (cacheado) |
| Cotações | `/api/quotes` | `POST`, `GET`, `POST /{id}/accept`, `DELETE` |
| Apólices | `/api/policies` | `POST`, `GET`, `POST /{id}/cancel`, `DELETE` |
| Itens de apólice | `/api/policy-items` | CRUD (filtro opcional por policy) |
| Pagamentos | `/api/payments` | `POST`, `GET`, `POST /{id}/confirm` (PIX), `DELETE` |
| Faturas | `/api/payment-invoices` | CRUD, `POST /{id}/pay` |
| Alertas preventivos | `/api/preventive-alerts` | `POST`, `GET`, `POST /{id}/view`, `PUT /{id}/situation` |
| Lookups | `/api/lookups/*` | **público + cacheado** — 19 tabelas de domínio (biomes, claim-situations, alert-severities, payment-types, …) |
| Auditoria | `/api/audit-logs` | `GET /{id}`, `GET` (filtro opcional por usuário) |

### analise-svc

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| `POST` | `/api/chatbot` | público | Conversa com o assistente IA (Spring AI/Ollama) |
| `GET` | `/api/ndvi/{plotId}/historico` | JWT | Série temporal NDVI do talhão (MongoDB) |
| `GET` | `/api/ndvi/{plotId}/analises` | JWT | Análises satelitais do talhão (Postgres) |

**Documentação interativa (Swagger UI):**
- core-svc → http://localhost:8081/swagger-ui.html
- analise-svc → http://localhost:8082/swagger-ui.html

---

## 🚀 Como rodar

### Pré-requisitos
- **Java 21** e **Maven** (para build/dev local).
- **Docker** + **Docker Compose** (para o ambiente completo).

### Opção 1 — ambiente completo via Docker Compose (recomendado)

```bash
# Sobe Postgres + MongoDB + RabbitMQ + Ollama + os 3 serviços Java.
# Na 1ª execução o Postgres aplica o schema e o seed automaticamente,
# e o Ollama baixa o modelo qwen2:0.5b.
docker compose up -d

# acompanhar logs
docker compose logs -f core-svc analise-svc gateway
```

Portas expostas:

| Serviço | URL |
|---|---|
| Gateway | http://localhost:8080 |
| core-svc | http://localhost:8081 |
| analise-svc | http://localhost:8082 |
| PostgreSQL | `localhost:5432` (zenith/zenith) |
| MongoDB | `localhost:27017` (zenith/zenith) |
| RabbitMQ (AMQP) | `localhost:5672` |
| RabbitMQ (admin) | http://localhost:15672 (zenith/zenith) |
| Ollama | http://localhost:11434 |

### Opção 2 — build do monorepo + serviço isolado em dev

```bash
# build de tudo a partir da raiz (parent POM)
mvn clean install

# subir só a infra via compose e rodar um serviço local
docker compose up -d postgres mongo rabbitmq ollama
cd core-svc && mvn spring-boot:run
```

As `application.yaml` usam defaults `localhost` para todas as conexões, então um serviço
local conversa com a infra do Compose sem configuração extra.

### Build de uma imagem específica

O `Dockerfile` é único e parametrizado:

```bash
docker build --build-arg MODULE=core-svc    -t zenith-core-svc .
docker build --build-arg MODULE=analise-svc -t zenith-analise-svc .
docker build --build-arg MODULE=gateway     -t zenith-gateway .
```

### Smoke test rápido

```bash
# 1. registrar e logar
curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d '{...}'
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{...}' | jq -r .token)

# 2. consumir um endpoint protegido
curl http://localhost:8080/api/lookups/biomes               # público
curl http://localhost:8080/api/farms -H "Authorization: Bearer $TOKEN"
```

---

## ✅ Testes e CI

```bash
mvn verify        # roda todos os testes dos 3 módulos
```

Testes presentes (JUnit + Spring Boot Test; core-svc usa **H2** em teste):
- `core-svc` — services (`ClaimItemServiceTest`, `PolicyItemServiceTest`),
  controller (`PolicyItemControllerTest`) e segurança (`PolicyItemControllerSecurityTest`).
- `analise-svc` — `LaudoServiceTest`, `SatelliteAnalysisServiceTest`, `SentinelHubStatsRequestFactoryTest`.
- `gateway` — context load.

**CI (GitHub Actions — `.github/workflows/ci.yml`):** em todo push na `main`, PR ou disparo
manual, configura JDK 21 (cache Maven) e roda `mvn -B verify`.

---

## 📋 Requisitos do edital atendidos (Java Advanced)

| Requisito | Onde |
|---|---|
| **Microsserviços + justificativa** | 3 serviços (gateway/core/analise) — ver [tabela acima](#por-que-3-serviços-justificativa-exigida-pelo-edital) |
| **Mensageria** | RabbitMQ — evento `sinistro.aberto`/`sinistro.analisado` com DLQ |
| **Spring Security + JWT** | JWT RS256 assimétrico + JWKS; OAuth2 Resource Server nos 3 serviços |
| **HATEOAS** | Spring HATEOAS nas respostas REST do core-svc |
| **Cache** | Spring Cache (`@Cacheable`) em lookups e catálogo de seguros |
| **CORS** | `GatewayCorsConfig` (mobile React Native consome a API) |
| **Swagger / OpenAPI** | SpringDoc em core-svc e analise-svc (`/swagger-ui.html`) |
| **Spring AI** | Ollama no analise-svc — laudo + chatbot |
| **Banco relacional + NoSQL** | PostgreSQL (39 tabelas) + MongoDB (4 collections) |
| **Feign** | core-svc → analise-svc (síncrono) |
| **DevOps** | Dockerfile, Docker Compose, GitHub Actions CI |

---

## 📐 Convenções de código

- **DTOs sempre** na fronteira da API — nunca serializar `@Entity` direto no controller.
- **`record`** para DTOs e eventos (imutável). **Não usar Lombok.**
- **Entidades JPA:** construtor sem-args `protected` + getters/setters explícitos.
- **Injeção por construtor** explícito nos services.
- **`@Valid` + Bean Validation** em todo request de entrada.
- **`GlobalExceptionHandler`** (`@RestControllerAdvice`) — respostas de erro padronizadas.
- **`ddl-auto: validate`** — o schema é gerido pelo SQL, nunca por `update`.
- Mensagens, nomes e comentários podem ser em **português** (domínio é BR).

> Há agentes e skills em `.claude/` (code-reviewer, feature-planner, solid-reviewer,
> jpa-entity, rest-endpoint, rabbitmq-event) que automatizam revisão e scaffolding no padrão
> do projeto. Detalhes em [`CLAUDE.md`](CLAUDE.md).

---

## 🌎 Contexto da Global Solution

Este repositório cobre **Java Advanced**, **Database** (Postgres + MongoDB) e parte de
**DevOps**. A trilha de IA escolhida é **Visão Computacional + IA Generativa**.

Entregas em **outros repositórios** (não fazem parte desta API): **API .NET** (Clean
Architecture, SOLID, Oracle, xUnit), **app mobile React Native** e modelagem
**TOGAF/Archimate**.

---

<sub>Global Solution 2026/1 — FIAP · Grupo Zenith · Seguro paramétrico agrícola via satélite.</sub>
