# Crop

## Visão Geral

`Crop` é o **catálogo global de culturas agrícolas** da plataforma. Apesar de tecnicamente
ser uma entidade (PK UUID + `Code`), funciona como referência curada centralmente: parâmetros
agronômicos (NDVI esperado, valor por hectare, vulnerabilidades a seca e geada) que o sistema
usa para calibrar análises e calcular prêmios.

Não é editada pelo produtor — é mantida pela equipe Zenith com base em dados
agronômicos oficiais (Embrapa, CONAB).

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Crop.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `name` | `String(80)` | Nome comum da cultura (ex: "Soja", "Milho") |
| `scientificName` | `String(120)` | Nome científico (nullable) |
| `averageCycleDays` | `Integer` | Duração média do ciclo produtivo em dias |
| `expectedNdviMin` | `BigDecimal(4,3)` | NDVI mínimo esperado para lavoura saudável |
| `expectedNdviMax` | `BigDecimal(4,3)` | NDVI máximo esperado para lavoura saudável |
| `averageValuePerHectare` | `BigDecimal(15,2)` | Valor médio de mercado por hectare (R$) |
| `droughtVulnerability` | `BigDecimal(3,1)` | Score 0–10 de vulnerabilidade à seca |
| `frostVulnerability` | `BigDecimal(3,1)` | Score 0–10 de vulnerabilidade à geada |
| `status` | `Boolean` | `true` = cultura disponível para cotação |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Dados de Seed

| Cultura | Ciclo (dias) | NDVI Min | NDVI Max | Valor/ha (R$) | Seca | Geada |
|---|---|---|---|---|---|---|
| Soja | 120 | 0.650 | 0.850 | 6.500,00 | 7.5 | 4.0 |
| Milho | 150 | 0.600 | 0.880 | 5.200,00 | 8.0 | 6.0 |
| Algodão | 180 | 0.550 | 0.800 | 9.800,00 | 6.5 | 3.0 |
| Café | 365 | 0.700 | 0.900 | 18.500,00 | 5.0 | 9.0 |
| Trigo | 130 | 0.550 | 0.820 | 4.100,00 | 7.0 | 8.5 |
| Cana-de-açúcar | 360 | 0.650 | 0.900 | 7.200,00 | 6.0 | 5.0 |
| Arroz | 130 | 0.600 | 0.880 | 5.600,00 | 9.0 | 5.5 |
| Feijão | 90 | 0.550 | 0.820 | 4.800,00 | 8.5 | 6.5 |
| Sorgo | 120 | 0.500 | 0.780 | 3.500,00 | 4.0 | 5.0 |

---

## Uso no Sistema

- **Motor de detecção** (`analise-svc`): compara `meanNdvi` da `SatelliteAnalysis` com
  `expectedNdviMin` para identificar estresse na lavoura.
- **Motor de cotação**: usa `averageValuePerHectare` como base do valor segurado e os scores
  de vulnerabilidade para ajustar a taxa base do prêmio.
- **Alertas preventivos**: o `analise-svc` dispara `PreventiveAlert` quando o NDVI cai abaixo
  de `expectedNdviMin`.

---

## Observações

- Apesar de ter PK UUID + `Code` como entidade operacional, `Crop` **não** deve ser criada
  livremente pelo usuário. Novos cultivos exigem validação agronômica.
- O `status = false` desativa a cultura para novas cotações sem remover talhões existentes
  que já usam essa cultura.
