# Plot

## Visão Geral

`Plot` é o **talhão** — a subdivisão da fazenda que o satélite monitora individualmente.
É a unidade operacional mais importante do sistema: cada análise NDVI, cotação, apólice,
sinistro e alerta preventivo está vinculado a um talhão específico.

Um talhão representa uma área homogênea de plantio de uma cultura (`Crop`) com data de
plantio, ciclo produtivo e polígono de delimitação.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Plot.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `farmId` | `UUID` | FK para `Farm` — fazenda pai |
| `cropId` | `UUID` | FK para `Crop` — cultura plantada neste ciclo |
| `plotSituationId` | `Integer` | FK para `PlotSituation` (lookup) — estágio do ciclo produtivo |
| `productionSystemId` | `Integer` | FK para `ProductionSystem` (lookup) — afeta desconto no prêmio |
| `identifier` | `String(40)` | Identificador textual livre (ex: "Talhão A3", "Norte") |
| `areaHectares` | `BigDecimal(10,2)` | Área do talhão em hectares |
| `plantingDate` | `LocalDate` | Data de plantio |
| `estimatedHarvestDate` | `LocalDate` | Data estimada de colheita |
| `cycleDays` | `Integer` | Duração real do ciclo (pode diferir do ciclo médio da cultura) |
| `seedVariety` | `String(100)` | Variedade de semente plantada |
| `polygonWkt` | `String` (TEXT) | Polígono de delimitação em WKT — usado para recorte de imagem satelital |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Situação (`PlotSituation`)

| ID | Descrição | Terminal |
|---|---|---|
| 1 | Em preparo | Não |
| 2 | Plantado | Não |
| 3 | Em desenvolvimento | Não |
| 4 | Colhido | **Sim** |
| 5 | Perda total | **Sim** |

Situações terminais bloqueiam novas cotações e sinistros para o talhão.

### Ciclo de vida

```
Em preparo ──► Plantado ──► Em desenvolvimento ──► Colhido (terminal)
                                    │
                                    └──► Perda total (terminal)
```

---

## Lookup: `ProductionSystem`

| ID | Descrição | Desconto de Prêmio |
|---|---|---|
| 1 | Sequeiro | 0% |
| 2 | Irrigado | 5% |
| 3 | Plantio direto | 3% |
| 4 | Plantio convencional | 0% |

O `PremiumDiscount` é aplicado como redução percentual no cálculo do prêmio na cotação.

---

## Relacionamentos

```
Farm (1) ──── (N) Plot ──── Crop (N:1)
                    │
                    ├── PlotSituation (lookup)
                    ├── ProductionSystem (lookup)
                    ├── (N) InsuranceQuote
                    ├── (N) Policy
                    ├── (N) SatelliteAnalysis
                    └── (N) PreventiveAlert
```

---

## Observações e Armadilhas

- **`polygonWkt`**: é o insumo principal para o `analise-svc` recortar as imagens do
  Sentinel-2. Sem polígono válido a análise NDVI não consegue delimitar a área.
- **`cropId` nullable no banco**: no momento do cadastro inicial o produtor pode não ter
  definido a cultura ainda (talhão "Em preparo"). Campos que dependem de `Crop` (ex: NDVI
  esperado) devem verificar se `cropId != null`.
- **Talhão não tem `farmId` único**: a mesma fazenda pode ter múltiplos talhões ativos
  simultaneamente — cada um com sua própria apólice e ciclo produtivo.
- **Situações terminais**: ao atingir `Colhido` ou `Perda total`, o sistema deve bloquear
  na camada de serviço qualquer abertura de nova cotação ou sinistro para este talhão.
