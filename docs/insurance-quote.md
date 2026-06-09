# InsuranceQuote

## Visão Geral

`InsuranceQuote` é a **cotação de seguro** gerada para um talhão específico. Representa a
proposta financeira — valor segurado, prêmio total e mensal — calculada a partir do produto
(`Insurance`), da cultura (`Crop`), do bioma e do histórico do produtor. Tem validade
determinada; se expirar sem ser aceita, o produtor precisa solicitar nova cotação.

Uma cotação aceita origina uma `Policy` (apólice).

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/InsuranceQuote.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `userId` | `UUID` | FK para `User` — produtor que solicitou a cotação |
| `plotId` | `UUID` | FK para `Plot` — talhão a ser segurado |
| `insuranceId` | `UUID` | FK para `Insurance` — produto escolhido |
| `quoteSituationId` | `Integer` | FK para `InsuranceQuoteSituation` (lookup) |
| `insuredAmount` | `BigDecimal(15,2)` | Valor total segurado (R$) |
| `totalPremium` | `BigDecimal(15,2)` | Prêmio total do período (R$) |
| `monthlyPremium` | `BigDecimal(15,2)` | Parcela mensal do prêmio (R$) |
| `regionalFactor` | `BigDecimal(5,3)` | Fator regional aplicado (default: 1.0) |
| `historyFactor` | `BigDecimal(5,3)` | Fator de histórico do produtor (default: 1.0) |
| `validUntil` | `OffsetDateTime` | Prazo de validade da cotação |
| `acceptedAt` | `OffsetDateTime` | Quando o produtor aceitou (nullable — só preenchido em `Aceita`) |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Situação (`InsuranceQuoteSituation`)

| ID | Descrição | Terminal |
|---|---|---|
| 1 | Em aberto | Não |
| 2 | Aceita | **Sim** |
| 3 | Recusada | **Sim** |
| 4 | Expirada | **Sim** |

### Ciclo de vida

```
Em aberto ──► Aceita (terminal) ──► origina Policy
    │
    ├──► Recusada (terminal)
    │
    └──► Expirada (terminal, via job de expiração)
```

---

## Fluxo de Cotação e Aceitação

```
POST /cotacoes
  ├─ Valida: Plot.plotSituation não terminal, Insurance.allowsNewQuotes = true
  ├─ Calcula insuredAmount = Plot.areaHectares × Crop.averageValuePerHectare
  ├─ Calcula totalPremium = insuredAmount × baseRatePct × regionalFactor × historyFactor × biome.riskFactor
  ├─ Define validUntil = agora + 30 dias
  └─ Persiste InsuranceQuote (situação: Em aberto)

POST /cotacoes/{id}/aceitar
  ├─ Valida: situação = Em aberto, validUntil > agora
  ├─ Atualiza: situação → Aceita, acceptedAt = agora
  └─ Cria Policy a partir da cotação
```

---

## Observações e Armadilhas

- **`validUntil`**: o job de expiração verifica periodicamente cotações `Em aberto` com
  `validUntil < agora` e as marca como `Expirada`. Sem esse job, cotações obsoletas
  continuariam aceitáveis.
- **`historyFactor`**: atualmente defaulta para 1.0. A lógica de desconto por histórico
  do produtor (sem sinistros anteriores) é um enhancement futuro.
- **Cotação aceita não altera a apólice**: os valores de `insuredAmount`, `totalPremium` e
  `monthlyPremium` são **copiados** para a `Policy` no momento da aceitação. Alterações
  posteriores no produto de seguro não afetam cotações nem apólices já emitidas.
