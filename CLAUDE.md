# Zenith Harvest

> Seguro paramétrico agrícola via satélite. Monitora lavouras por NDVI (Sentinel-2),
> detecta perdas com visão computacional + IA, e paga sinistros via PIX em até 48h.
> SaaS B2B para seguradoras. Projeto da Global Solution 2026/1 — FIAP (grupo Zenith).

Este arquivo orienta assistentes de IA (Claude Code etc.) a trabalhar neste repositório.
Leia antes de gerar ou alterar código.

---

## Visão geral

Zenith Harvest é um **monorepo de microsserviços Java (Spring Boot)**. O produto resolve a
dor do produtor rural que espera meses por um sinistro agrícola: satélite vê a lavoura,
IA decide, PIX cai. Vende-se para seguradoras (Brasilseg, Porto, Mapfre, Tokio Marine).

**Stack principal:** Java 21 + Spring Boot 3.5.14 + PostgreSQL + MongoDB + RabbitMQ + Spring AI (Ollama).

---

## Arquitetura — 3 serviços Java

| Serviço | Porta | Responsabilidade |
|---|---|---|
| `gateway` | 8080 | Borda. Roteamento, validação de JWT, rate limit. Spring Cloud Gateway (reativo/WebFlux). |
| `core-svc` | 8081 | Coração. CRUD de todo o domínio + auth/JWT + financeiro. Publica eventos no RabbitMQ. |
| `analise-svc` | 8082 | Visão computacional (NDVI/satélite) + IA generativa (Spring AI/Ollama: RAG, laudo, chatbot). Consome eventos. |

**Comunicação:**
- Síncrona: `core-svc` → `analise-svc` via **OpenFeign**.
- Assíncrona: `core-svc` publica `sinistro.aberto` no **RabbitMQ** → `analise-svc` consome → devolve `sinistro.analisado`.

**Fora deste repo (entregas separadas da GS, NÃO mexer aqui):**
- **API .NET** — entrega isolada da disciplina .NET (não faz parte do produto). O edital
  EXIGE nela: **Clean Architecture, SOLID, Injeção de Dependência**, tratamento global de
  exceções, JWT, Swagger/OpenAPI, persistência Oracle e testes xUnit (padrão AAA).
  Clean Architecture é requisito DESTA API, não do Java.
- **App mobile React Native** — consome as APIs Java.

---

## Estrutura do monorepo

```
zenith/
 ├─ pom.xml              # parent POM (Maven multi-módulo) — centraliza versões
 ├─ docker-compose.yml   # Postgres + MongoDB + RabbitMQ + Ollama + 3 serviços
 ├─ CLAUDE.md            # este arquivo
 ├─ gateway/             # com.fiap.zenith.gateway
 ├─ core-svc/            # com.fiap.zenith.core
 └─ analise-svc/         # com.fiap.zenith.analise
```

GroupId único nos 3: **`com.fiap.zenith`**. Artifacts: `gateway`, `core-svc`, `analise-svc`.

### Estrutura interna de cada serviço (organização em camadas)

> Nota: "Clean Architecture" NÃO é exigida no Java pelo edital — é requisito da API .NET
> (repo separado). Aqui usamos organização em camadas como boa prática; a banca avalia
> "qualidade da arquitetura" na apresentação, mas não cobra o termo Clean Architecture.

```
src/main/java/com/fiap/zenith/<svc>/
 ├─ config/        # Security, OpenAPI, RabbitMQ, Feign
 ├─ domain/
 │   ├─ entity/    # @Entity JPA
 │   ├─ repository/# interfaces Spring Data
 │   └─ enums/
 ├─ application/
 │   ├─ service/   # regras de negócio
 │   └─ dto/       # request/response — NUNCA expor entity na API
 ├─ web/
 │   ├─ controller/# @RestController
 │   └─ handler/   # GlobalExceptionHandler
 └─ infra/
     ├─ feign/     # clients para outros serviços
     └─ messaging/ # publishers/consumers RabbitMQ
```

---

## Banco de dados

### PostgreSQL (relacional) — 39 tabelas

> **DDL canônico (fonte de verdade do schema): `db/schema/zenith_harvest_postgres.sql`**
> SEMPRE consulte esse arquivo antes de criar/alterar entidades JPA. Use os nomes e tipos
> EXATOS das colunas de lá. O modelo visual está em `db/dbml/zenith_harvest.dbml` (dbdiagram.io).

Padrão de modelagem (estilo WMS corporativo):
- **Entity** (PK UUID): substantivos de negócio. Algumas têm `Code` (Integer, 1 em 1, p/ front).
- **Item** (PK UUID): composição (`PolicyItem`, `ClaimItem`).
- **Lookup** (PK Integer): tabelas de domínio que substituem ENUMs. Sem `Code`.

**As 39 tabelas por domínio:**

**1. Users (6):** `User` · `Credential` · `Address` · `AccessLog` · `AccessLogAction`(L) — (User tem Code; Address sem Code).

**2. Farm (6):** `Crop` · `Farm` · `Plot` · `Biome`(L) · `ProductionSystem`(L) · `PlotSituation`(L).

**3. Insurance Catalog (6):** `Insurer` · `Insurance` · `InsuranceQuote` · `InsurerSituation`(L) · `InsuranceSituation`(L) · `InsuranceQuoteSituation`(L).

**4. Operation (17):** `Policy` · `PolicyItem` · `Claim` · `ClaimItem` · `SatelliteAnalysis` · `PreventiveAlert` · `PolicySituation`(L) · `ClaimSituation`(L) · `ClaimEventType`(L) · `ClaimCategory`(L) · `ClaimSubCategory`(L) · `SatelliteSource`(L) · `SatelliteClass`(L) · `AlertType`(L) · `AlertSeverity`(L) · `AlertSituation`(L) · `RejectionReason`(L).

**5. Financial (4):** `Payment` · `PaymentInvoice` · `PaymentType`(L) · `PaymentSituation`(L).

**6. Audit (1):** `AuditLog`.

`(L)` = lookup. Total: 20 entidades/itens + 19 lookups = 39.

Entidades com `Code` (Integer, incremento 1, exibido/buscado no front):
`User`, `Crop`, `Farm`, `Plot`, `Insurer`, `Insurance`, `InsuranceQuote`, `Policy`, `Claim`, `Payment`, `PaymentInvoice`.

Detalhes importantes do schema:
- `Payment` tem CHECK XOR: ou `ClaimId` ou `PolicyId`, nunca os dois.
- `SatelliteSource` é lookup puro (operador é coluna texto, não FK).
- `PreventiveAlert.AlertSituationId` é FK (não string).
- `Claim.CalculatedAmount` e `ApprovedAmount` são NUMERIC(15,2) (não timestamp).
- `AuditLog.DescriptionJson` é JSONB; `TargetEntityId` é VARCHAR (polimórfico).

### MongoDB (NoSQL) — 4 collections
`historico_ndvi` (séries temporais de NDVI por talhão) + `claim_audit_events` +
`policy_audit_events` + `payment_audit_events` (trilhas de auditoria detalhadas).

Arquitetura de auditoria é **híbrida**: `AuditLog` no Postgres (operações cross-cutting:
LOGIN/LOGOUT/ANONYMIZATION/BLOCK — garantia LGPD) + eventos de negócio detalhados no MongoDB.

---

## Convenções de modelagem (IMPORTANTE — seguir à risca)

- **Nomes de tabela/coluna em PascalCase entre aspas duplas** (`"Claim"`, `"ClaimNumber"`).
  Consequência: TODA query/JPA precisa de aspas. No `application.yml`:
  ```yaml
  spring.jpa.properties.hibernate.globally_quoted_identifiers: true
  ```
- **PK de entidade** = `UUID` (DEFAULT `gen_random_uuid()` no banco / `GenerationType.UUID` no JPA).
- **PK de lookup** = `INTEGER GENERATED BY DEFAULT AS IDENTITY`.
- **`Code`** = `INTEGER` incremental **de 1 em 1**, presente APENAS nas entidades operacionais
  (User, Crop, Farm, Plot, Insurer, Insurance, InsuranceQuote, Policy, Claim, Payment, PaymentInvoice).
  É o número "bonito" para exibir no front e buscar uma operação. Lookups NÃO têm Code.
- **Lookups de estado** chamam-se `*Situation` (não `*Status`): `PlotSituation`, `ClaimSituation`,
  `PolicySituation`, `InsurerSituation`, `InsuranceSituation`, `InsuranceQuoteSituation`, `PaymentSituation`, `AlertSituation`.
- **Soft delete triplo** de timestamps: `CreatedAt` / `EditedAt` / `DeletedAt` (TIMESTAMPTZ).
- **Dinheiro** = `NUMERIC(15,2)` (NUNCA float). **NDVI** = `NUMERIC(4,3)`. **Percentual** = `NUMERIC(5,2)`.
- **Booleano** = `BOOLEAN` nativo do Postgres. **JSON** = `JSONB`. **Texto longo/WKT** = `TEXT`.

### Filosofia de NOT NULL (CRÍTICO)
NOT NULL **somente** no que existe no momento da criação da linha (PKs, FKs estruturais,
identificadores naturais, Code, Description de lookup). **Todo campo operacional é NULLABLE**
e preenchido conforme o fluxo roda (resultados de análise satelital, valores calculados,
timestamps de transição como `ApprovedAt`/`PaidAt`/`CancelledAt`). Nunca trave a operação
com um NOT NULL em campo que só existe numa etapa futura do fluxo.

---

## Requisitos OBRIGATÓRIOS do edital — disciplina Java Advanced

A banca de Java cobra explicitamente (não esquecer nenhum):

- **Microsserviços + justificativa** — a escolha por 3 serviços (gateway/core/analise)
  precisa estar JUSTIFICADA no README/apresentação (por que separar, trade-offs).
- **Mensageria** — usar RabbitMQ em ao menos uma parte (evento `sinistro.aberto`).
- **Spring Security + JWT** — controle de acesso com tokens JWT.
- **HATEOAS** — links de navegação nas respostas REST (Spring HATEOAS no core-svc).
- **Cache** — cacheamento (Spring Cache; ex: catálogo de seguros, lookups).
- **CORS** — configuração de CORS (o mobile React Native vai consumir as APIs).
- **Swagger / OpenAPI** — documentação via SpringDoc.
- **Spring AI** — RAG/tooling/chatbot (trilha de IA Generativa) no analise-svc.

## Convenções de código Java

- **DTOs sempre** na fronteira da API — nunca serializar `@Entity` direto no controller.
- **`record` para DTOs e eventos** (Java puro, imutável, gera getter/equals/toString). NÃO usar Lombok.
- **Entidades JPA**: construtor sem-args (`protected`) + getters/setters explícitos (a IDE gera).
- **Injeção por construtor explícito** nos services (Spring injeta sozinho quando há 1 construtor).
- **Validation** (`@Valid` + Bean Validation) em todo request de entrada.
- **GlobalExceptionHandler** (`@RestControllerAdvice`) — respostas de erro padronizadas.
- **Swagger/OpenAPI** via SpringDoc em core-svc e analise-svc (`/swagger-ui.html`).
- **HATEOAS** nas respostas de recursos REST do core-svc.
- **Cache** (`@Cacheable`) em consultas de baixa volatilidade (lookups, catálogo).
- **CORS** configurado para o app mobile consumir.
- Mensagens, nomes de variáveis e comentários podem ser em **português** (domínio é BR).
- O `gateway` é **reativo** (WebFlux) — NÃO adicionar Spring Web (MVC) nele.

---

## Build & Run

```bash
# build de tudo (a partir da raiz, com parent POM)
mvn clean install

# subir o ambiente completo (bancos + mensageria + IA + serviços)
docker compose up -d

# subir um serviço isolado em dev
cd core-svc && mvn spring-boot:run
```

Infra local via Docker Compose: PostgreSQL, MongoDB, RabbitMQ, Ollama.

---

## Spring AI (analise-svc)

Provider padrão: **Ollama** (local, sem custo, sem API key) — garante demo sem depender de
crédito/internet. Código abstraído via Spring AI: trocar para OpenAI/Gemini é mudar starter +
`application.yml`, sem tocar na lógica.

Usos de IA no produto:
- **RAG** sobre cartilhas SUSEP/Embrapa (contexto para laudo).
- **Geração de laudo** de sinistro em linguagem natural.
- **Chatbot** de suporte ao produtor.

---

## Contexto da Global Solution (disciplinas atendidas)

Este repo cobre **Java Advanced** (microsserviços justificados, Feign, mensageria RabbitMQ,
Spring Security/JWT, HATEOAS, Cache, CORS, Swagger, Spring AI), **Database** (Postgres
relacional + MongoDB NoSQL) e parte de **DevOps** (Docker, pipeline). A trilha de IA
escolhida é **Visão Computacional + IA Generativa** (não IoT).

Entregas em OUTROS repositórios/disciplinas: API .NET, mobile React Native, TOGAF/Archimate.

---

## Agentes e skills disponíveis (`.claude/`)

Use proativamente — não espere o usuário pedir.

| Ferramenta | Tipo | Quando acionar |
|---|---|---|
| `code-reviewer` | agent | Após escrever ou alterar qualquer classe Java (entity, controller, service, config); antes de commitar |
| `feature-planner` | agent | Antes de implementar uma feature nova — produz plano técnico (entidades, endpoints, fluxo, eventos) |
| `solid-reviewer` | agent | Quando o usuário pedir revisão SOLID, ou após escrever services/controllers com muitas dependências |
| `jpa-entity` | skill | Ao criar ou alterar um `@Entity` — garante PK, tipos, NOT NULL conservador e nomes PascalCase corretos |
| `rest-endpoint` | skill | Ao adicionar qualquer rota nova — garante DTO, `@Valid`, HATEOAS, `@Transactional` e camadas corretas |
| `rabbitmq-event` | skill | Ao criar publisher ou consumer RabbitMQ — garante naming de exchange/queue/DLQ e idempotência |

---

## O que NÃO fazer

- Não expor entidades JPA direto na API (use DTO).
- Não usar `float`/`double` para dinheiro (use `NUMERIC`/`BigDecimal`).
- Não colocar Spring Web (MVC) no gateway (ele é reativo).
- Não criar `Code` em lookups, nem usar saltos (é 1 em 1, só em entidades operacionais).
- Não marcar NOT NULL em campo operacional preenchido em etapa futura do fluxo.
- Não usar `ddl-auto: update` — o schema é gerido pelo script SQL; use `validate`.
- Não renomear lookups de estado para `*Status` (o padrão do projeto é `*Situation`).