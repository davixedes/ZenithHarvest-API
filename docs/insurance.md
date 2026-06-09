# Insurance

## Visão Geral

`Insurance` é o **produto de seguro paramétrico** disponibilizado por uma `Insurer` na
plataforma. Define as condições gerais: franquia, carência, cobertura máxima, taxa base e
estados onde é comercializado. Produtores geram `InsuranceQuote` a partir de um produto
ativo.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Insurance.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `insurerId` | `UUID` | FK para `Insurer` — seguradora responsável pelo produto |
| `name` | `String(150)` | Nome comercial do produto |
| `description` | `String(500)` | Descrição das condições gerais (nullable) |
| `deductiblePct` | `BigDecimal(5,2)` | Franquia percentual (default: 10.00%) |
| `graceDays` | `Integer` | Carência em dias após contratação (default: 7) |
| `maxCoveragePerHectare` | `BigDecimal(15,2)` | Limite máximo de cobertura por hectare (R$) |
| `baseRatePct` | `BigDecimal(5,3)` | Taxa base do prêmio sobre o valor segurado |
| `availableStates` | `String(100)` | Lista de UFs separadas por vírgula (ex: `SP,MT,GO`) |
| `insuranceSituationId` | `Integer` | FK para `InsuranceSituation` (lookup) |
| `active` | `Boolean` | Flag operacional — `false` pausa sem descontinuar |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Situação (`InsuranceSituation`)

| ID | Descrição | Permite Novas Cotações |
|---|---|---|
| 1 | Disponível | Sim |
| 2 | Pausada | Não |
| 3 | Descontinuada | Não |

---

## Fórmula de Prêmio (simplificada)

```
prêmio_base = valor_segurado × baseRatePct
fator_bioma  = Biome.regionalRiskFactor
fator_sistema = 1 - (ProductionSystem.premiumDiscount / 100)
total_premium = prêmio_base × fator_bioma × fator_sistema
```

Os fatores `regionalFactor` e `historyFactor` da `InsuranceQuote` podem ajustar ainda mais.

---

## Relacionamentos

```
Insurer (1) ──── (N) Insurance (1) ──── (N) InsuranceQuote
                              (1) ──── (N) Policy
```

---

## Observações

- **`availableStates`**: campo texto simples — não é FK. A validação de que o talhão está
  em um estado coberto deve ser feita na camada de serviço antes de gerar a cotação.
- **Descontinuação**: ao descontinuar um produto (`InsuranceSituation = 3`), apólices ativas
  continuam vigentes — apenas novas cotações são bloqueadas.
- **`baseRatePct` precisão 5,3**: permite taxas como `0.850%` (três casas decimais), comum
  em seguros paramétricos agrícolas com altas coberturas.
