# Zenith Harvest — API Java

> **Seguro paramétrico agrícola via satélite.** Monitora lavouras por NDVI (Sentinel-2),
> detecta perdas com visão computacional + IA generativa, e paga sinistros via PIX em até 48h.
> SaaS B2B para seguradoras (Brasilseg, Porto, Mapfre, Tokio Marine).

Projeto da **Global Solution 2026/1 — FIAP** (grupo Zenith), disciplina **Java Advanced**.

---

## Índice

- [O problema e a solução](#o-problema-e-a-solução)
- [Arquitetura](#arquitetura)
- [Stack técnica](#stack-técnica)
- [Como rodar](#como-rodar)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Autenticação](#autenticação)
- [Endpoints — Autenticação](#endpoints--autenticação)
- [Endpoints — Usuários](#endpoints--usuários)
- [Endpoints — Propriedade Rural](#endpoints--propriedade-rural)
- [Endpoints — Catálogo de Seguros](#endpoints--catálogo-de-seguros)
- [Endpoints — Operação (Apólices e Sinistros)](#endpoints--operação-apólices-e-sinistros)
- [Endpoints — Financeiro](#endpoints--financeiro)
- [Endpoints — Alertas Preventivos](#endpoints--alertas-preventivos)
- [Endpoints — Análise e IA](#endpoints--análise-e-ia)
- [Endpoints — Lookups](#endpoints--lookups)
- [Endpoints — Auditoria](#endpoints--auditoria)
- [Fluxo completo — sinistro paramétrico](#fluxo-completo--sinistro-paramétrico)
- [Fluxo — alerta preventivo](#fluxo--alerta-preventivo)
- [Banco de dados](#banco-de-dados)
- [Mensageria (RabbitMQ)](#mensageria-rabbitmq)
- [Inteligência Artificial](#inteligência-artificial)
- [Testes e CI](#testes-e-ci)
- [Requisitos do edital atendidos](#requisitos-do-edital-atendidos)

---

## O problema e a solução

O produtor rural espera **meses** por um sinistro agrícola tradicional: vistoria presencial,
perícia, burocracia. O **Zenith Harvest** inverte isso com **seguro paramétrico**:

1. **Satélite** observa a lavoura (NDVI via Sentinel-2 — Agência Espacial Europeia).
2. **Visão computacional + IA** decidem se houve perda e geram o laudo.
3. **PIX** cai na conta do produtor em até 48h.

O produto é vendido para seguradoras como SaaS B2B.

---

## Arquitetura

Monorepo de **3 microsserviços Java (Spring Boot)** + infraestrutura (Postgres, MongoDB,
RabbitMQ, Ollama), tudo orquestrado via Docker Compose.

```
                          ┌────────────────────────────────────┐
    mobile / front  ───▶  │      gateway  (porta 8080)         │  borda: roteamento + valida JWT
                          │   Spring Cloud Gateway WebMVC      │
                          └──────────────┬─────────────────────┘
                      /auth/**, /api/**  │  /api/ndvi/**, /api/chatbot, /api/varredura/**
                  ┌───────────────────── ┴ ──────────────────────────┐
                  ▼                                                    ▼
    ┌───────────────────────────┐              ┌──────────────────────────────────┐
    │     core-svc (8081)       │  OpenFeign   │       analise-svc (8082)         │
    │  CRUD do domínio          │ ──────────▶  │  Visão computacional (NDVI)      │
    │  Auth / JWT RS256         │              │  IA generativa (Spring AI/Ollama) │
    │  Financeiro / PIX         │              │  Chatbot de suporte              │
    │  Publica eventos          │              │  Varredura preventiva            │
    └───────────┬───────────────┘              └──────────────┬───────────────────┘
                │                                              ▲
                │      RabbitMQ: sinistro.aberto               │
                └──────────────────────────────────────────────┘
                               sinistro.analisado

  PostgreSQL (39 tabelas)   MongoDB (4 collections)   Ollama (LLM local)
```

### Por que 3 serviços?

| Serviço | Porta | Por que separado |
|---|---|---|
| **`gateway`** | 8080 | Isola a borda do domínio. Escala e protege a entrada sem tocar na regra de negócio. Único ponto de CORS. |
| **`core-svc`** | 8081 | Concentra o estado consistente (Postgres + transações ACID). O serviço mais estável — falhas na IA não o derrubam. |
| **`analise-svc`** | 8082 | Carga e dependências (LLM, satélite) muito diferentes do core. Escala de forma independente e pode reiniciar sem afetar o CRUD. |

**Comunicação inter-serviços:**
- **Síncrona:** `core-svc` → `analise-svc` via **OpenFeign** (histórico NDVI, criação de alertas).
- **Assíncrona:** `core-svc` publica `sinistro.aberto` no **RabbitMQ**; `analise-svc` consome, processa e devolve `sinistro.analisado`.

---

## Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build | Maven multi-módulo (parent POM centraliza versões) |
| Gateway | Spring Cloud Gateway Server WebMVC |
| Persistência relacional | PostgreSQL 16 + Spring Data JPA / Hibernate |
| Persistência NoSQL | MongoDB 7 (séries temporais NDVI + auditoria) |
| Mensageria | RabbitMQ 3.13 (Spring AMQP) |
| Segurança | Spring Security + OAuth2 Resource Server + JWT RS256 |
| IA generativa | Spring AI 1.1.7 + Ollama (LLM local) |
| Comunicação inter-serviços | OpenFeign |
| HATEOAS / Cache / Validation | Spring HATEOAS, Spring Cache, Bean Validation |
| Documentação | SpringDoc OpenAPI 2.8.x (Swagger UI) |
| Containers | Docker + Docker Compose |
| CI | GitHub Actions (`mvn verify`) |

---

## Como rodar

### Pré-requisitos

- Docker + Docker Compose

### Ambiente completo (recomendado)

```bash
docker compose up -d
```

Na primeira execução o Postgres aplica o schema (39 tabelas) e o seed (lookups) automaticamente.
O Ollama baixa o modelo `qwen2:0.5b`.

```bash
# acompanhar logs
docker compose logs -f core-svc analise-svc gateway

# verificar saúde
docker compose ps
```

**Portas expostas:**

| Serviço | URL | Credenciais |
|---|---|---|
| Gateway | http://localhost:8080 | — |
| core-svc | http://localhost:8081 | — |
| analise-svc | http://localhost:8082 | — |
| Swagger core-svc | http://localhost:8081/swagger-ui.html | — |
| Swagger analise-svc | http://localhost:8082/swagger-ui.html | — |
| PostgreSQL | `localhost:5432` | `zenith` / `zenith` |
| MongoDB | `localhost:27017` | `zenith` / `zenith` |
| RabbitMQ admin | http://localhost:15672 | `zenith` / `zenith` |
| Ollama | http://localhost:11434 | — |

### Infra apenas (desenvolvimento local)

```bash
# sobe só Postgres, MongoDB, RabbitMQ e Ollama
docker compose up -d postgres mongo rabbitmq ollama

# roda os serviços via Maven (os application.yaml usam localhost por padrão)
cd core-svc    && mvn spring-boot:run
cd analise-svc && mvn spring-boot:run
cd gateway     && mvn spring-boot:run
```

### Resetar banco

```bash
docker compose down -v   # apaga volumes
docker compose up -d     # recria tudo do zero
```

---

## Variáveis de ambiente

Todas têm defaults funcionais para desenvolvimento local.

### core-svc

| Variável | Default | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/zenith` | URL do PostgreSQL |
| `DB_USERNAME` | `zenith` | Usuário do banco |
| `DB_PASSWORD` | `zenith` | Senha do banco |
| `RABBITMQ_HOST` | `localhost` | Host do RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Porta AMQP |
| `RABBITMQ_USERNAME` | `zenith` | Usuário RabbitMQ |
| `RABBITMQ_PASSWORD` | `zenith` | Senha RabbitMQ |
| `ANALISE_SVC_URI` | `http://localhost:8082` | URL do analise-svc (Feign) |
| `JWT_EXPIRATION_SECONDS` | `28800` | Expiração do JWT (8h) |

### analise-svc

| Variável | Default | Descrição |
|---|---|---|
| `POSTGRES_HOST` | `localhost` | Host do PostgreSQL |
| `POSTGRES_USERNAME` | `zenith` | Usuário do banco |
| `POSTGRES_PASSWORD` | `zenith` | Senha do banco |
| `MONGO_HOST` | `localhost` | Host do MongoDB |
| `MONGO_USERNAME` | `zenith` | Usuário MongoDB |
| `MONGO_PASSWORD` | `zenith` | Senha MongoDB |
| `RABBITMQ_HOST` | `localhost` | Host do RabbitMQ |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | URL da API do Ollama |
| `OLLAMA_MODEL` | `llama3.2` | Modelo LLM (Docker: `qwen2:0.5b`) |
| `JWK_SET_URI` | `http://localhost:8081/oauth2/jwks` | JWKS do core-svc |
| `CORE_SVC_URL` | `http://localhost:8081` | URL do core-svc (Feign) |
| `ALERTA_VARREDURA_CRON` | `0 0 6 * * *` | Cron da varredura automática de alertas |
| `SENTINEL_HUB_ENABLED` | `false` | Habilita integração com Sentinel Hub |
| `SENTINEL_HUB_CLIENT_ID` | — | Client ID do Sentinel Hub |
| `SENTINEL_HUB_CLIENT_SECRET` | — | Client Secret do Sentinel Hub |

---

## Autenticação

Todos os endpoints (exceto os listados como **público**) exigem o header:

```
Authorization: Bearer <token>
```

O token é obtido via `POST /auth/login`. JWT assimétrico **RS256** — a chave pública é publicada
em `/oauth2/jwks` e validada automaticamente por todos os serviços. Expiração: **8 horas**.

---

## Endpoints — Autenticação

Base: `http://localhost:8080` (via gateway) ou `http://localhost:8081` (direto)

### `POST /auth/register` — público

Cria uma nova conta de produtor rural.

**Request:**
```json
{
  "cpf": "123.456.789-00",
  "name": "João",
  "lastName": "Silva",
  "email": "joao@email.com",
  "phone": "(11) 98765-4321",
  "password": "senhaSegura123",
  "street": "Rua das Flores",
  "number": 100,
  "neighboor": "Centro",
  "city": "Ribeirão Preto",
  "postalCode": "14010-001",
  "uf": "SP",
  "country": "Brasil"
}
```

**Response `201`:**
```json
{
  "id": "276edb95-bc64-4775-a4ec-0773efcb0f2c",
  "code": 1,
  "name": "João",
  "lastName": "Silva",
  "email": "joao@email.com"
}
```

---

### `POST /auth/login` — público

**Request:**
```json
{
  "email": "joao@email.com",
  "password": "senhaSegura123"
}
```

**Response `200`:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiJ9...",
  "expiresIn": 28800
}
```

---

## Endpoints — Usuários

Base: `/api/users`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users/me` | Dados do usuário autenticado |
| `GET` | `/api/users/{id}` | Busca usuário por ID |
| `GET` | `/api/users` | Lista usuários (paginado) |
| `PUT` | `/api/users/{id}` | Atualiza nome e telefone |
| `DELETE` | `/api/users/{id}` | Soft delete |

**`PUT /api/users/{id}` — Request:**
```json
{
  "name": "João",
  "lastName": "Santos",
  "phone": "(11) 99999-0000"
}
```

---

## Endpoints — Propriedade Rural

### Culturas — `/api/crops`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/crops` | Cadastra cultura |
| `GET` | `/api/crops` | Lista culturas (cacheado) |
| `GET` | `/api/crops/{id}` | Busca por ID |
| `PUT` | `/api/crops/{id}` | Atualiza parâmetros agronômicos |
| `DELETE` | `/api/crops/{id}` | Soft delete |

**`POST /api/crops` — Request:**
```json
{
  "name": "Soja",
  "scientificName": "Glycine max",
  "averageCycleDays": 120,
  "expectedNdviMin": 0.650,
  "expectedNdviMax": 0.850,
  "averageValuePerHectare": 6500.00,
  "droughtVulnerability": 7.5,
  "frostVulnerability": 4.0
}
```

---

### Fazendas — `/api/farms`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/farms` | Cadastra fazenda |
| `GET` | `/api/farms` | Lista fazendas (paginado) |
| `GET` | `/api/farms/{id}` | Busca por ID |
| `PUT` | `/api/farms/{id}` | Atualiza dados da fazenda |
| `DELETE` | `/api/farms/{id}` | Soft delete |

**`POST /api/farms` — Request:**
```json
{
  "userId": "276edb95-bc64-4775-a4ec-0773efcb0f2c",
  "name": "Fazenda São João",
  "carRegistration": "SP-1234567-8901234567-8901234567-89",
  "nirf": "12345678",
  "latitude": -21.1767,
  "longitude": -47.8208,
  "totalAreaHectares": 250.50,
  "state": "SP",
  "biomeId": 2,
  "propertyType": "Rural",
  "polygonWkt": "POLYGON((-47.82 -21.17, -47.81 -21.17, -47.81 -21.18, -47.82 -21.18, -47.82 -21.17))"
}
```

---

### Talhões — `/api/plots`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/plots` | Cadastra talhão |
| `GET` | `/api/plots` | Lista todos (paginado) |
| `GET` | `/api/plots/{id}` | Busca por ID |
| `GET` | `/api/farms/{farmId}/plots` | Lista talhões de uma fazenda |
| `GET` | `/api/plots/{id}/ndvi-historico` | Histórico NDVI via Feign → analise-svc |
| `PUT` | `/api/plots/{id}` | Atualiza dados do talhão |
| `DELETE` | `/api/plots/{id}` | Soft delete |

**`POST /api/plots` — Request:**
```json
{
  "farmId": "7f2cc962-aeac-4863-9715-a49eed56f99c",
  "cropId": "35d41be8-467a-4ebb-afd9-09e08773fdcc",
  "plotSituationId": 2,
  "productionSystemId": 1,
  "identifier": "Talhão A1",
  "areaHectares": 45.00,
  "plantingDate": "2026-01-15",
  "estimatedHarvestDate": "2026-05-15",
  "cycleDays": 120,
  "seedVariety": "M7739IPRO",
  "polygonWkt": "POLYGON(...)"
}
```

---

## Endpoints — Catálogo de Seguros

### Seguradoras — `/api/insurers`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/insurers` | Cadastra seguradora |
| `GET` | `/api/insurers` | Lista seguradoras |
| `GET` | `/api/insurers/{id}` | Busca por ID |
| `PUT` | `/api/insurers/{id}` | Atualiza dados |
| `DELETE` | `/api/insurers/{id}` | Soft delete |

**`POST /api/insurers` — Request:**
```json
{
  "corporateName": "Brasilseg Seguros S.A.",
  "tradeName": "Brasilseg",
  "cnpj": "01.234.567/0001-89",
  "susepCode": "0123-4",
  "commercialEmail": "parceiros@brasilseg.com.br",
  "phone": "(11) 3000-0000",
  "adminFeePct": 5.00,
  "takeRatePct": 8.00,
  "insurerSituationId": 1,
  "accreditedAt": "2026-01-01"
}
```

---

### Produtos de seguro — `/api/insurances`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/insurances` | Cria produto |
| `GET` | `/api/insurances` | Lista produtos (cacheado) |
| `GET` | `/api/insurances/{id}` | Busca por ID |
| `PUT` | `/api/insurances/{id}` | Atualiza condições |
| `DELETE` | `/api/insurances/{id}` | Soft delete |

**`POST /api/insurances` — Request:**
```json
{
  "insurerId": "50124578-a7c1-4d63-9b5a-dc097c3f729c",
  "name": "Proteção Soja Premium",
  "description": "Cobertura paramétrica para soja no Cerrado",
  "deductiblePct": 10.00,
  "graceDays": 7,
  "maxCoveragePerHectare": 8000.00,
  "baseRatePct": 0.850,
  "availableStates": "SP,MT,GO,MS,MG",
  "insuranceSituationId": 1
}
```

---

### Cotações — `/api/quotes`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/quotes` | Gera cotação |
| `GET` | `/api/quotes` | Lista cotações |
| `GET` | `/api/quotes/{id}` | Busca por ID |
| `POST` | `/api/quotes/{id}/accept` | Aceita cotação → gera apólice |
| `DELETE` | `/api/quotes/{id}` | Remove cotação |

**`POST /api/quotes` — Request:**
```json
{
  "userId": "276edb95-bc64-4775-a4ec-0773efcb0f2c",
  "plotId": "63abc434-52b8-4c43-bf46-617b511e5aa5",
  "insuranceId": "131156a3-903b-4e89-8ef2-621fbc97f89c",
  "quoteSituationId": 1,
  "insuredAmount": 292500.00,
  "totalPremium": 2486.25,
  "monthlyPremium": 207.19,
  "regionalFactor": 1.10,
  "historyFactor": 1.00,
  "validUntil": "2026-07-09T23:59:59Z"
}
```

---

## Endpoints — Operação (Apólices e Sinistros)

### Apólices — `/api/policies`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/policies` | Emite apólice |
| `GET` | `/api/policies` | Lista apólices |
| `GET` | `/api/policies/{id}` | Busca por ID |
| `POST` | `/api/policies/{id}/cancel` | Cancela apólice |
| `DELETE` | `/api/policies/{id}` | Soft delete |

**`POST /api/policies` — Request:** (o `policyNumber`/protocolo é gerado pelo servidor)
```json
{
  "insuranceQuoteId": "2dcf0d14-bbe1-4b06-a331-fb0b9ac3339c",
  "plotId": "63abc434-52b8-4c43-bf46-617b511e5aa5",
  "insurerId": "50124578-a7c1-4d63-9b5a-dc097c3f729c",
  "insuranceId": "131156a3-903b-4e89-8ef2-621fbc97f89c",
  "policySituationId": 1,
  "insuredAmount": 292500.00,
  "totalPremium": 2486.25,
  "monthlyPremium": 207.19,
  "deductiblePct": 10.00,
  "maxCoverage": 292500.00,
  "startDate": "2026-01-22",
  "endDate": "2026-06-30"
}
```

---

### Itens de apólice — `/api/policy-items`

Define quais eventos climáticos a apólice cobre e com qual percentual.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/policy-items` | Adiciona cobertura de evento |
| `GET` | `/api/policy-items?policyId={id}` | Lista itens (filtro opcional por apólice) |
| `GET` | `/api/policy-items/{id}` | Busca por ID |
| `PUT` | `/api/policy-items/{id}` | Atualiza percentual/limite |
| `DELETE` | `/api/policy-items/{id}` | Remove cobertura |

**`POST /api/policy-items` — Request:**
```json
{
  "policyId": "ff6b203e-5ece-41a5-b992-854757f287ed",
  "claimEventTypeId": 1,
  "coveragePct": 100.00,
  "maxCoverageAmount": 292500.00,
  "notes": "Cobertura total para eventos de seca"
}
```

---

### Sinistros — `/api/claims`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/claims` | Abre sinistro → publica `sinistro.aberto` no RabbitMQ |
| `GET` | `/api/claims` | Lista sinistros |
| `GET` | `/api/claims/{id}` | Busca por ID |
| `POST` | `/api/claims/{id}/approve` | Aprova sinistro (analista) |
| `POST` | `/api/claims/{id}/reject` | Rejeita sinistro |
| `DELETE` | `/api/claims/{id}` | Soft delete |

**`POST /api/claims` — Request:** (o `claimNumber`/protocolo é gerado pelo servidor)
```json
{
  "policyId": "ff6b203e-5ece-41a5-b992-854757f287ed",
  "claimSituationId": 1,
  "categoryId": 1,
  "subCategoryId": 1,
  "description": "Estiagem prolongada desde meados de março. Plantas com murchamento generalizado.",
  "photoUrl": "https://storage.zenith.com.br/sinistros/foto-001.jpg",
  "openingGpsLat": -21.1767,
  "openingGpsLng": -47.8208,
  "ndviBefore": 0.720
}
```

**Response `201`:** sinistro criado com `situationId = 1` (Aberto). O evento `sinistro.aberto`
é publicado no RabbitMQ e o `analise-svc` inicia a análise automaticamente.

---

### Itens de sinistro — `/api/claim-items`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/claim-items` | Adiciona item ao sinistro |
| `GET` | `/api/claim-items?claimId={id}` | Lista itens (filtro opcional por sinistro) |
| `GET` | `/api/claim-items/{id}` | Busca por ID |
| `PUT` | `/api/claim-items/{id}` | Atualiza área e percentual |
| `DELETE` | `/api/claim-items/{id}` | Remove item |

**`POST /api/claim-items` — Request:**
```json
{
  "claimId": "44be5168-f7ca-41f5-b55d-1b6dc4ac199e",
  "claimEventTypeId": 1,
  "affectedAreaHa": 32.50,
  "lossPct": 57.00,
  "ndviBefore": 0.720,
  "ndviAfter": 0.310,
  "itemAmount": 119925.00,
  "description": "Seca severa na área norte do talhão"
}
```

---

## Endpoints — Financeiro

### Pagamentos — `/api/payments`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/payments` | Cria registro de pagamento PIX |
| `GET` | `/api/payments` | Lista pagamentos |
| `GET` | `/api/payments/{id}` | Busca por ID |
| `POST` | `/api/payments/{id}/confirm` | Confirma recebimento do PIX |
| `DELETE` | `/api/payments/{id}` | Soft delete |

**`POST /api/payments` — Request:**
```json
{
  "paymentTypeId": 1,
  "paymentSituationId": 1,
  "claimId": "44be5168-f7ca-41f5-b55d-1b6dc4ac199e",
  "amount": 107932.50,
  "pixKey": "123.456.789-00"
}
```

> `claimId` e `policyId` são mutuamente exclusivos (constraint XOR). Exatamente um dos dois deve ser informado.

---

### Faturas — `/api/payment-invoices`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/payment-invoices` | Emite fatura de cobrança de prêmio |
| `GET` | `/api/payment-invoices?userId={id}` | Lista faturas (filtro opcional por usuário) |
| `GET` | `/api/payment-invoices/{id}` | Busca por ID |
| `PUT` | `/api/payment-invoices/{id}` | Atualiza dados da fatura |
| `POST` | `/api/payment-invoices/{id}/pay` | Marca fatura como paga |
| `DELETE` | `/api/payment-invoices/{id}` | Desativa fatura |

**`POST /api/payment-invoices` — Request:** (o `invoiceNumber` é gerado pelo servidor)
```json
{
  "userId": "276edb95-bc64-4775-a4ec-0773efcb0f2c",
  "insurerId": "50124578-a7c1-4d63-9b5a-dc097c3f729c",
  "totalAmount": 207.19,
  "dueDate": "2026-07-10"
}
```

---

## Endpoints — Alertas Preventivos

O sistema emite alertas **antes** da perda total — um dos diferenciais do produto.
O `analise-svc` monitora o NDVI de todos os talhões com apólice vigente e cria alertas
automaticamente quando detecta queda anormal.

### Via `core-svc` — `/api/alertas`

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| `GET` | `/api/alertas` | JWT | Lista alertas (todos ou filtrado por `?plotId=`) |
| `GET` | `/api/alertas/{id}` | JWT | Busca alerta por ID |
| `PUT` | `/api/alertas/{id}/visualizar` | JWT | Marca como visualizado |
| `PUT` | `/api/alertas/{id}/resolver` | JWT | Marca como resolvido (terminal) |
| `PUT` | `/api/alertas/{id}/descartar` | JWT | Descarta alerta (terminal) |
| `POST` | `/api/alertas/interno` | público | Chamada interna do analise-svc (Feign) |

**Response com HATEOAS — `GET /api/alertas/{id}`:**
```json
{
  "id": "...",
  "plotId": "...",
  "alertTypeId": 1,
  "alertSeverityId": 3,
  "alertSituationId": 1,
  "message": "Alerta Crítico na lavoura de Soja. NDVI atual: 0.310 (esperado mínimo: 0.650 — queda de 52.3%). Recomendamos irrigação de emergência e abertura de sinistro pelo aplicativo.",
  "observedNdvi": 0.310,
  "expectedNdvi": 0.650,
  "dropPct": 52.31,
  "issuedAt": "2026-06-09T06:00:00Z",
  "viewedAt": null,
  "createdAt": "2026-06-09T06:00:00Z",
  "_links": {
    "self": { "href": "/api/alertas/..." },
    "alertas": { "href": "/api/alertas" },
    "visualizar": { "href": "/api/alertas/.../visualizar" },
    "resolver": { "href": "/api/alertas/.../resolver" },
    "descartar": { "href": "/api/alertas/.../descartar" }
  }
}
```

> Links `visualizar`, `resolver` e `descartar` só aparecem enquanto o alerta não for terminal.
> Alertas `Resolvido` ou `Descartado` retornam apenas `self` e `alertas`.

### Via `analise-svc` — gatilho manual (demo)

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| `POST` | `/api/varredura/alertas` | público | Dispara a varredura de NDVI imediatamente |

**Response `200`:**
```
Varredura de alertas concluída.
```

Útil na apresentação para demonstrar o fluxo sem esperar o cron das 6h.

---

## Endpoints — Análise e IA

Base: `http://localhost:8082` (direto) ou pelo gateway.

### Histórico NDVI — `/api/ndvi`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/ndvi/{plotId}/historico` | Série temporal de NDVI do talhão (MongoDB) |
| `GET` | `/api/ndvi/{plotId}/analises` | Análises satelitais do talhão (Postgres) |

**Response `/api/ndvi/{plotId}/historico`:**
```json
[
  {
    "plotId": "...",
    "date": "2026-06-04",
    "meanNdvi": 0.712,
    "meanEvi": 0.645,
    "satelliteSource": "Sentinel-2",
    "cloudCoverage": 5.2,
    "satelliteClass": 1
  }
]
```

---

### Chatbot — `/api/chatbot` — público

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/chatbot` | Envia mensagem ao assistente IA (Spring AI + Ollama) |

**Request:**
```json
{
  "mensagem": "O que é NDVI e como ele é usado no seguro?"
}
```

**Response `200`:**
```json
{
  "resposta": "O NDVI (Normalized Difference Vegetation Index) é um índice satelital que mede a saúde da vegetação..."
}
```

---

## Endpoints — Lookups

Base: `/api/lookups` — **público + cacheado em memória**

Todas as tabelas de domínio estão disponíveis via GET. Útil para popular selects no front.

| Endpoint | Descrição |
|---|---|
| `GET /api/lookups/biomes` | Biomas brasileiros com fator de risco regional |
| `GET /api/lookups/production-systems` | Sistemas de produção (sequeiro, irrigado…) |
| `GET /api/lookups/plot-situations` | Situações do ciclo do talhão |
| `GET /api/lookups/insurer-situations` | Situações de credenciamento da seguradora |
| `GET /api/lookups/insurance-situations` | Situações de disponibilidade do produto |
| `GET /api/lookups/insurance-quote-situations` | Situações do ciclo da cotação |
| `GET /api/lookups/policy-situations` | Situações da apólice |
| `GET /api/lookups/claim-situations` | Situações do sinistro |
| `GET /api/lookups/claim-event-types` | Tipos de evento climático |
| `GET /api/lookups/claim-categories` | Categorias de sinistro |
| `GET /api/lookups/claim-subcategories` | Subcategorias de sinistro |
| `GET /api/lookups/satellite-sources` | Fontes satelitais (Sentinel-2, Landsat-8…) |
| `GET /api/lookups/satellite-classes` | Classes de análise (saudável → solo exposto) |
| `GET /api/lookups/alert-types` | Tipos de alerta preventivo |
| `GET /api/lookups/alert-severities` | Severidades (Informativo / Atenção / Crítico) |
| `GET /api/lookups/alert-situations` | Situações do alerta |
| `GET /api/lookups/payment-types` | Tipos de pagamento (PIX, prêmio, estorno…) |
| `GET /api/lookups/payment-situations` | Situações do pagamento |
| `GET /api/lookups/rejection-reasons` | Motivos de rejeição de sinistro |

---

## Endpoints — Auditoria

Base: `/api/audit-logs`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/audit-logs` | Lista logs de auditoria |
| `GET` | `/api/audit-logs?userId={id}` | Filtra por usuário |
| `GET` | `/api/audit-logs/{id}` | Busca por ID |

Registra operações LGPD: LOGIN, LOGOUT, PASSWORD_CHANGE, ANONYMIZATION, BLOCK.

---

## Fluxo completo — sinistro paramétrico

```
1. Produtor abre sinistro
   POST /api/claims  →  Claim persiste (Postgres)
                     →  publica "sinistro.aberto" no RabbitMQ

2. analise-svc consome o evento
   SinistroAbertoConsumer
     → busca NDVI via Sentinel Hub (ou fallback simulado)
     → calcula percentual de perda
     → classifica SatelliteClass (saudável → solo exposto)
     → gera laudo em linguagem natural (Spring AI / Ollama)
     → persiste SatelliteAnalysis (Postgres)
     → salva série temporal no MongoDB (historico_ndvi)
     → publica "sinistro.analisado" no RabbitMQ

3. core-svc consome o resultado
   ClaimAnalisadoConsumer
     → atualiza Claim com ndviAfter, calculatedAmount, mlConfidenceScore
     → muda situação para "Aprovado" ou "Rejeitado"

4. Analista confirma (ou sistema aprova automaticamente)
   POST /api/claims/{id}/approve

5. Pagamento PIX
   POST /api/payments  (paymentTypeId=1, claimId=...)
   POST /api/payments/{id}/confirm
     → accumulatedPaid da apólice é incrementado
     → Claim muda para "Pago"
```

---

## Fluxo — alerta preventivo

```
Automático (diário às 6h) OU manual via POST /api/varredura/alertas

analise-svc: AlertaDeteccaoService.executarVarredura()
  1. Busca todos os plotIds com apólice Vigente e dentro da data de vigência
  2. Para cada plot:
     a. Busca SatelliteAnalysis mais recente (últimos 7 dias, nuvens ≤ 80%)
     b. Compara NDVI observado com Crop.expectedNdviMin
     c. Calcula dropPct = (esperado - observado) / esperado × 100
     d. Se dropPct < 15% → ignora (ruído tolerável)
     e. 15–30% → severidade Informativo (1)
        30–50% → severidade Atenção (2)
        ≥ 50%  → severidade Crítico (3)
     f. Gera mensagem via Spring AI (Ollama, fallback textual)
     g. Chama POST /api/alertas/interno no core-svc (Feign)
     h. core-svc verifica anti-duplicidade e persiste PreventiveAlert

Produtor recebe push notification (severidade ≥ 2) ou SMS (severidade 3)
  GET /api/alertas?plotId={id}
  PUT /api/alertas/{id}/resolver   ← produtor tomou ação
```

---

## Banco de dados

### PostgreSQL — 39 tabelas

> **Fonte de verdade:** [`db/schema/zenith_harvest_postgres.sql`](db/schema/zenith_harvest_postgres.sql).
> O app usa `ddl-auto: validate` — nunca altera o schema; o DDL é aplicado na 1ª inicialização pelo Docker.

| Domínio | Tabelas |
|---|---|
| Users (6) | `User` · `Credential` · `Address` · `AccessLog` · `AccessLogAction`(L) |
| Farm (6) | `Crop` · `Farm` · `Plot` · `Biome`(L) · `ProductionSystem`(L) · `PlotSituation`(L) |
| Insurance Catalog (6) | `Insurer` · `Insurance` · `InsuranceQuote` · `InsurerSituation`(L) · `InsuranceSituation`(L) · `InsuranceQuoteSituation`(L) |
| Operation (17) | `Policy` · `PolicyItem` · `Claim` · `ClaimItem` · `SatelliteAnalysis` · `PreventiveAlert` + 11 lookups |
| Financial (4) | `Payment` · `PaymentInvoice` · `PaymentType`(L) · `PaymentSituation`(L) |
| Audit (1) | `AuditLog` |

`(L)` = lookup (substitui ENUM).

**Convenções:**
- Nomes PascalCase entre aspas duplas — `hibernate.globally_quoted_identifiers: true`
- PK entidade = `UUID`; PK lookup = `INTEGER IDENTITY`
- `Code` = inteiro incremental (apenas em entidades operacionais)
- Lookups de estado = sufixo `*Situation` (nunca `*Status`)
- Soft delete: `CreatedAt` / `EditedAt` / `DeletedAt`
- Dinheiro = `NUMERIC(15,2)` → `BigDecimal`; NDVI = `NUMERIC(4,3)`

### MongoDB — 4 collections

| Collection | Conteúdo |
|---|---|
| `historico_ndvi` | Série temporal de NDVI por talhão (alimentada pelo analise-svc) |
| `claim_audit_events` | Trilha de todas as transições de um sinistro |
| `policy_audit_events` | Trilha de todas as transições de uma apólice |
| `payment_audit_events` | Trilha de todas as transições de um pagamento |

---

## Mensageria (RabbitMQ)

| Recurso | Nome | Tipo |
|---|---|---|
| Exchange | `sinistros` | fanout |
| Exchange | `analises` | fanout |
| Exchange | `zenith.dlx` | direct (dead-letter) |
| Fila | `sinistro.aberto` | durable + DLQ |
| Fila | `sinistro.analisado` | durable |
| Fila | `sinistro.aberto.dlq` | dead-letter |

Mensagens em **JSON** (`Jackson2JsonMessageConverter`). Painel: http://localhost:15672

---

## Inteligência Artificial

Provider padrão: **Ollama** (local, sem custo, sem API key). Trocar para OpenAI/Gemini = mudar
starter + `application.yaml`, sem tocar na lógica.

| Feature | Service | Descrição |
|---|---|---|
| Laudo de sinistro | `LaudoService` | Texto técnico em linguagem natural para o produtor |
| Alerta preventivo | `AlertaMensagemService` | Mensagem com recomendação de ação; fallback textual se Ollama indisponível |
| Chatbot de suporte | `ChatbotController` | `POST /api/chatbot` — responde dúvidas do produtor |

Modelo no Docker Compose: `qwen2:0.5b` (leve). Default dev local: `llama3.2`. Temperatura: `0.3`.

---

## Testes e CI

```bash
mvn verify   # roda todos os testes dos 3 módulos
```

| Módulo | Testes |
|---|---|
| `core-svc` | `ClaimItemServiceTest`, `PolicyItemServiceTest`, `PolicyItemControllerTest`, `PolicyItemControllerSecurityTest` |
| `analise-svc` | `LaudoServiceTest`, `SatelliteAnalysisServiceTest`, `SentinelHubStatsRequestFactoryTest` |
| `gateway` | context load |

**CI:** GitHub Actions — em todo push na `main` ou PR executa `mvn -B verify` com JDK 21.

---

## Requisitos do edital atendidos

| Requisito (Java Advanced) | Onde |
|---|---|
| Microsserviços + justificativa | 3 serviços com justificativa de separação na seção [Arquitetura](#arquitetura) |
| Mensageria | RabbitMQ — `sinistro.aberto` / `sinistro.analisado` com DLQ |
| Spring Security + JWT | JWT RS256 assimétrico + JWKS; OAuth2 Resource Server nos 3 serviços |
| HATEOAS | Spring HATEOAS em todos os controllers do core-svc; links condicionais nos alertas |
| Cache | `@Cacheable` em lookups e catálogo de seguros |
| CORS | `GatewayCorsConfig` no gateway (mobile React Native consome a API) |
| Swagger / OpenAPI | SpringDoc em core-svc (`:8081/swagger-ui.html`) e analise-svc (`:8082/swagger-ui.html`) |
| Spring AI | Ollama — laudo de sinistro + alerta preventivo + chatbot |
| Banco relacional + NoSQL | PostgreSQL (39 tabelas) + MongoDB (4 collections) |
| Feign | core-svc → analise-svc (NDVI) + analise-svc → core-svc (alertas) |
| DevOps | Dockerfile multi-módulo, Docker Compose completo, GitHub Actions CI |

---

<sub>Global Solution 2026/1 — FIAP · Grupo Zenith · Seguro paramétrico agrícola via satélite.</sub>
