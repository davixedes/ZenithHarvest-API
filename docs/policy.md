# Policy

## Visão Geral

`Policy` é a **apólice de seguro paramétrico** — o contrato vigente entre o produtor e a
seguradora. Origina-se de uma `InsuranceQuote` aceita e define os termos definitivos:
período de vigência, franquia, cobertura máxima e as coberturas por tipo de evento
(`PolicyItem`).

Enquanto a apólice está `Vigente`, sinistros (`Claim`) podem ser abertos contra ela.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Policy.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `policyNumber` | `String(30)` | Número único da apólice (gerado no serviço) |
| `insuranceQuoteId` | `UUID` | FK para `InsuranceQuote` de origem (nullable — apólices manuais não têm cotação) |
| `plotId` | `UUID` | FK para `Plot` — talhão coberto |
| `insurerId` | `UUID` | FK para `Insurer` — seguradora emissora |
| `insuranceId` | `UUID` | FK para `Insurance` — produto contratado |
| `policySituationId` | `Integer` | FK para `PolicySituation` (lookup) |
| `insuredAmount` | `BigDecimal(15,2)` | Valor total segurado (R$) |
| `totalPremium` | `BigDecimal(15,2)` | Prêmio total do período (R$) |
| `monthlyPremium` | `BigDecimal(15,2)` | Parcela mensal do prêmio (R$) |
| `deductiblePct` | `BigDecimal(5,2)` | Franquia percentual aplicada nesta apólice |
| `maxCoverage` | `BigDecimal(15,2)` | Limite máximo de cobertura (R$) |
| `accumulatedPaid` | `BigDecimal(15,2)` | Total de indenizações pagas nesta apólice (default: 0.00) |
| `startDate` | `LocalDate` | Início da vigência |
| `endDate` | `LocalDate` | Fim da vigência |
| `contractedAt` | `OffsetDateTime` | Timestamp da contratação |
| `cancelledAt` | `OffsetDateTime` | Timestamp do cancelamento (nullable) |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Situação (`PolicySituation`)

| ID | Descrição | Permite Sinistro | Terminal |
|---|---|---|---|
| 1 | Vigente | **Sim** | Não |
| 2 | Aguardando pagamento | Não | Não |
| 3 | Cancelada | Não | **Sim** |
| 4 | Expirada | Não | **Sim** |

### Ciclo de vida

```
Aguardando pagamento ──► Vigente ──► Expirada (terminal, ao atingir endDate)
                             │
                             └──► Cancelada (terminal, por solicitação)

[Vigente] ──► permite Claim
```

---

## Entidade Relacionada: `PolicyItem`

`PolicyItem` detalha a cobertura por **tipo de evento climático**. Uma apólice cobre múltiplos
eventos com percentuais e limites individuais.

**Arquivo:** `core-svc/.../domain/entity/PolicyItem.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `policyId` | `UUID` | FK para `Policy` |
| `claimEventTypeId` | `Integer` | FK para `ClaimEventType` (lookup) — ex: Seca, Geada |
| `coveragePct` | `BigDecimal(5,2)` | Percentual da cobertura para este evento (default: 100%) |
| `maxCoverageAmount` | `BigDecimal(15,2)` | Limite máximo de indenização para este evento (nullable) |
| `notes` | `String(500)` | Observações sobre a cobertura específica (nullable) |

> Constraint única: `(policyId, claimEventTypeId)` — cada tipo de evento aparece no máximo
> uma vez por apólice.

### Lookup: `ClaimEventType`

| ID | Descrição | Requer Foto |
|---|---|---|
| 1 | Seca | Não |
| 2 | Geada | Não |
| 3 | Granizo | **Sim** |
| 4 | Excesso de chuva | Não |
| 5 | Praga | **Sim** |
| 6 | Incêndio | **Sim** |

---

## Fluxo de Emissão

```
POST /cotacoes/{id}/aceitar
  ├─ InsuranceQuote → situação: Aceita
  └─ Cria Policy:
       ├─ Copia valores financeiros da cotação
       ├─ Define startDate = hoje + graceDays (carência do produto)
       ├─ Define endDate = startDate + ciclo da cultura (em meses)
       ├─ Cria PolicyItem para cada ClaimEventType do produto
       └─ Situação: Aguardando pagamento

[Pagamento do primeiro prêmio confirmado]
  └─ Situação → Vigente
```

---

## Observações e Armadilhas

- **`accumulatedPaid`**: atualizado a cada `Payment` confirmado de sinistro. Quando
  `accumulatedPaid >= maxCoverage` a apólice deve ser expirada — cobertura esgotada.
- **Carência (`graceDays`)**: sinistros abertos dentro do período de carência
  (`createdAt < startDate`) devem ser rejeitados automaticamente na camada de serviço.
- **`policyNumber`**: gerado pelo serviço (não pelo banco) — padrão `ZH-{ano}-{code:06d}`.
  Deve ser único; colisões podem ocorrer se o serviço gerar o número sem lock adequado.
- **Cancelamento**: ao cancelar, registrar `cancelledAt` e trocar situação para `Cancelada`.
  Sinistros em aberto vinculados à apólice cancelada precisam ser tratados manualmente.
