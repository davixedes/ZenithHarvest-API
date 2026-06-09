# Claim

## Visão Geral

`Claim` é o **sinistro** — o agregado central do produto Zenith Harvest. Representa o ciclo
completo de uma ocorrência de perda agrícola: do registro pelo produtor até a indenização
via PIX. É o evento que aciona o `analise-svc` via RabbitMQ e que, ao ser aprovado, gera
um `Payment`.

O diferencial do produto é a velocidade: satélite analisa, IA decide, PIX cai em até 48h.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Claim.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `claimNumber` | `String(30)` | Número único do sinistro (gerado no serviço) |
| `policyId` | `UUID` | FK para `Policy` — apólice que cobre este sinistro |
| `claimSituationId` | `Integer` | FK para `ClaimSituation` (lookup) |
| `categoryId` | `Integer` | FK para `ClaimCategory` (lookup) — Climático, Biológico, Operacional |
| `subCategoryId` | `Integer` | FK para `ClaimSubCategory` (lookup) — ex: Estiagem prolongada |
| `description` | `String` (TEXT) | Relato livre do produtor sobre o evento |
| `photoUrl` | `String(500)` | URL da foto de evidência (obrigatória para eventos com `requiresPhoto`) |
| `openingGpsLat` | `BigDecimal(10,7)` | Latitude do GPS no momento da abertura |
| `openingGpsLng` | `BigDecimal(10,7)` | Longitude do GPS no momento da abertura |
| `ndviBefore` | `BigDecimal(4,3)` | NDVI da lavoura antes do evento (preenchido pela análise) |
| `ndviAfter` | `BigDecimal(4,3)` | NDVI da lavoura após o evento (preenchido pela análise) |
| `totalLossPct` | `BigDecimal(5,2)` | Percentual total de perda calculado pela IA |
| `totalAffectedAreaHa` | `BigDecimal(10,2)` | Área afetada total em hectares |
| `calculatedAmount` | `BigDecimal(15,2)` | Valor de indenização calculado automaticamente |
| `approvedAmount` | `BigDecimal(15,2)` | Valor aprovado para pagamento (pode diferir do calculado) |
| `mlConfidenceScore` | `BigDecimal(5,2)` | Score de confiança do modelo de IA (0–100) |
| `fraudFlag` | `Boolean` | `true` = suspeita de fraude detectada pela IA |
| `rejectionReasonId` | `Integer` | FK para `RejectionReason` (lookup) — preenchido se rejeitado |
| `analystId` | `UUID` | ID do analista que revisou manualmente (nullable) |
| `approvedAt` | `OffsetDateTime` | Timestamp de aprovação (nullable) |
| `paidAt` | `OffsetDateTime` | Timestamp de pagamento confirmado (nullable) |
| `createdAt` | `OffsetDateTime` | Timestamp de abertura (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Situação (`ClaimSituation`)

| ID | Descrição | Permite Novo Sinistro | Terminal |
|---|---|---|---|
| 1 | Aberto | Sim | Não |
| 2 | Em análise | Não | Não |
| 3 | Aprovado | Não | Não |
| 4 | Rejeitado | Não | **Sim** |
| 5 | Pago | Não | **Sim** |

### Ciclo de vida

```
                ┌──────────────────────────────────────┐
                │             Aberto                    │
                │  (produtor registra via app)          │
                └──────────────────┬───────────────────┘
                                   │ evento RabbitMQ: sinistro.aberto
                                   ▼
                ┌──────────────────────────────────────┐
                │           Em análise                  │
                │  (analise-svc processa NDVI + IA)     │
                └──────────────────┬───────────────────┘
                                   │ evento RabbitMQ: sinistro.analisado
                         ┌─────────┴──────────┐
                         ▼                    ▼
              ┌────────────────┐   ┌──────────────────────┐
              │   Aprovado     │   │      Rejeitado        │
              │ (IA aprova ou  │   │  (fraude, carência,   │
              │  analista ok)  │   │   evento não coberto) │
              └───────┬────────┘   └──────────────────────┘
                      │ Payment confirmado
                      ▼
              ┌────────────────┐
              │      Pago      │ (terminal)
              └────────────────┘
```

---

## Entidade Relacionada: `ClaimItem`

Detalha o sinistro **por tipo de evento**. Um sinistro pode envolver múltiplos eventos
simultâneos (ex: seca + granizo).

**Arquivo:** `core-svc/.../domain/entity/ClaimItem.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `claimId` | `UUID` | FK para `Claim` |
| `claimEventTypeId` | `Integer` | FK para `ClaimEventType` (lookup) |
| `affectedAreaHa` | `BigDecimal(10,2)` | Área afetada por este evento em hectares |
| `lossPct` | `BigDecimal(5,2)` | Percentual de perda atribuído a este evento |
| `ndviBefore` | `BigDecimal(4,3)` | NDVI antes do evento (específico deste item) |
| `ndviAfter` | `BigDecimal(4,3)` | NDVI após o evento (específico deste item) |
| `itemAmount` | `BigDecimal(15,2)` | Valor de indenização calculado para este item |
| `description` | `String(500)` | Observações do analista sobre este item |

---

## Lookups

### `ClaimCategory`

| ID | Descrição |
|---|---|
| 1 | Climático |
| 2 | Biológico |
| 3 | Operacional |

### `ClaimSubCategory`

| ID | Descrição |
|---|---|
| 1 | Estiagem prolongada |
| 2 | Geada de radiação |
| 3 | Tempestade de granizo |
| 4 | Alagamento |
| 5 | Infestação de pragas |
| 6 | Queimada |

### `RejectionReason`

| ID | Descrição |
|---|---|
| 1 | Fora do período de vigência |
| 2 | Carência não cumprida |
| 3 | Evento não coberto pela apólice |
| 4 | Suspeita de fraude |
| 5 | Documentação insuficiente |
| 6 | Perda abaixo da franquia |

---

## Integração com analise-svc (RabbitMQ)

### Publicação: `sinistro.aberto`

Ao criar um sinistro, o `core-svc` publica no RabbitMQ:

```json
{
  "claimId": "<uuid>",
  "plotId": "<uuid>",
  "policyId": "<uuid>",
  "categoryId": 1,
  "openingGpsLat": -15.7801,
  "openingGpsLng": -47.9292,
  "createdAt": "2026-06-09T10:00:00Z"
}
```

### Consumo: `sinistro.analisado`

O `analise-svc` devolve o resultado da análise NDVI + IA:

```json
{
  "claimId": "<uuid>",
  "ndviBefore": 0.720,
  "ndviAfter": 0.310,
  "totalLossPct": 57.00,
  "totalAffectedAreaHa": 12.50,
  "calculatedAmount": 40625.00,
  "mlConfidenceScore": 94.00,
  "fraudFlag": false,
  "items": [...]
}
```

O handler em `core-svc` atualiza os campos e avança para `Aprovado` ou `Rejeitado`.

---

## Cálculo de Indenização

```
valor_bruto    = insuredAmount × (totalLossPct / 100)
franquia       = valor_bruto × (policy.deductiblePct / 100)
calculatedAmount = valor_bruto - franquia

-- Limite por cobertura do evento (PolicyItem)
calculatedAmount = min(calculatedAmount, policyItem.maxCoverageAmount)

-- Limite total da apólice
disponivel = policy.maxCoverage - policy.accumulatedPaid
approvedAmount = min(calculatedAmount, disponivel)
```

---

## Observações e Armadilhas

- **Carência**: a abertura deve verificar `policy.startDate <= claim.createdAt`. Violação
  → rejeição com `RejectionReason = 2`.
- **`fraudFlag = true`**: não rejeita automaticamente — sinaliza para revisão humana.
  O analista (`analystId`) pode aprovar mesmo com flag ativo.
- **`approvedAt` e `paidAt`** são nullable e preenchidos conforme o fluxo avança. Nunca
  marcar NOT NULL nesses campos.
- **`accumulatedPaid` da apólice**: ao confirmar `Payment`, o `core-svc` deve incrementar
  `Policy.accumulatedPaid`. Essa atualização deve ser atômica com a confirmação do pagamento.
- **Idempotência**: o consumer de `sinistro.analisado` deve verificar se o sinistro ainda
  está `Em análise` antes de processar — redeliveries do RabbitMQ podem duplicar a atualização.
