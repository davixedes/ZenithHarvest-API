# SatelliteAnalysis

## Visão Geral

`SatelliteAnalysis` armazena o **resultado de uma análise de imagem satelital** de um talhão.
É produzida pelo `analise-svc` quando um sinistro é aberto ou em varreduras periódicas
preventivas. Combina métricas de vegetação (NDVI, EVI), cobertura de nuvens e uma classificação
da condição da lavoura via ML.

Persiste no PostgreSQL compartilhado (gerenciado pelo schema do `core-svc`) para que o
`core-svc` possa ler as análises ao construir laudos e históricos.

---

## Entidade Principal

**Arquivo:** `analise-svc/src/main/java/com/fiap/zenith/analise_svc/domain/entity/SatelliteAnalysis.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `claimId` | `UUID` | FK para `Claim` (nullable — análises preventivas não têm sinistro) |
| `plotId` | `UUID` | FK para `Plot` — talhão analisado |
| `satelliteSourceId` | `Integer` | FK para `SatelliteSource` (lookup) — satélite usado |
| `satelliteClassId` | `Integer` | FK para `SatelliteClass` (lookup) — classificação ML da condição |
| `imageDate` | `LocalDate` | Data de captura da imagem |
| `sceneId` | `String(100)` | ID da cena no catálogo do provedor satelital |
| `imageUrl` | `String(500)` | URL da imagem processada |
| `meanNdvi` | `BigDecimal(4,3)` | NDVI médio da área do talhão (Normalized Difference Vegetation Index) |
| `meanEvi` | `BigDecimal(4,3)` | EVI médio (Enhanced Vegetation Index — menos sensível a solo exposto) |
| `cloudCoveragePct` | `BigDecimal(5,2)` | Percentual de cobertura de nuvens na imagem |
| `affectedAreaM2` | `BigDecimal(15,2)` | Área afetada em metros quadrados (detectada por ML) |
| `mlConfidence` | `BigDecimal(5,2)` | Score de confiança do modelo (0–100) |
| `processedAt` | `OffsetDateTime` | Quando o `analise-svc` concluiu o processamento |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Lookup: `SatelliteSource`

| ID | Descrição | Operador | Resolução | Revisita (dias) |
|---|---|---|---|---|
| 1 | Sentinel-2 | ESA | 10m | 5 |
| 2 | Landsat-8 | NASA/USGS | 30m | 16 |
| 3 | MODIS | NASA | 250m | 1 |

**Sentinel-2** é o padrão do sistema: resolução de 10m e revisita de 5 dias são adequados
para monitoramento de talhões individuais.

---

## Lookup: `SatelliteClass`

| ID | Descrição | Severidade |
|---|---|---|
| 1 | Vegetação saudável | 0 |
| 2 | Estresse leve | 1 |
| 3 | Estresse moderado | 2 |
| 4 | Estresse severo | 3 |
| 5 | Solo exposto | 4 |

A `SatelliteClass` é determinada pelo modelo ML com base no NDVI e EVI. Severidade ≥ 3
dispara `PreventiveAlert` se não houver sinistro aberto.

---

## Como o NDVI é Interpretado

| NDVI | Interpretação |
|---|---|
| > 0.6 | Vegetação densa e saudável |
| 0.4 – 0.6 | Vegetação moderada |
| 0.2 – 0.4 | Vegetação esparsa ou estressada |
| < 0.2 | Solo exposto, seco ou urbano |

Valores de referência por cultura estão em `Crop.expectedNdviMin` e `Crop.expectedNdviMax`.

---

## Fluxo de Processamento

```
[Evento: sinistro.aberto no RabbitMQ]
         │
         ▼
analise-svc: SatelliteAnalysisService
  ├─ Consulta imagens Sentinel-2 para o polígono do Plot
  ├─ Filtra imagens com cloudCoveragePct < 20%
  ├─ Calcula meanNdvi e meanEvi para a área do talhão
  ├─ Classifica com modelo ML → SatelliteClass
  ├─ Calcula affectedAreaM2 por segmentação de imagem
  ├─ Persiste SatelliteAnalysis
  └─ Publica sinistro.analisado com os resultados

[Varredura periódica preventiva — job agendado]
  ├─ Para cada Plot com apólice Vigente:
  │    ├─ Busca imagem mais recente
  │    ├─ Calcula NDVI
  │    ├─ Se NDVI < Crop.expectedNdviMin → cria PreventiveAlert
  │    └─ Persiste SatelliteAnalysis (claimId = null)
```

---

## Armazenamento de Séries Temporais (MongoDB)

Além do registro em PostgreSQL, o `analise-svc` grava séries temporais de NDVI na
collection `historico_ndvi` do MongoDB:

```json
{
  "plotId": "<uuid>",
  "date": "2026-06-09",
  "meanNdvi": 0.712,
  "meanEvi": 0.645,
  "satelliteSource": "Sentinel-2",
  "cloudCoverage": 5.2,
  "satelliteClass": 1
}
```

Essa série alimenta os gráficos de evolução de NDVI no app mobile e o RAG do chatbot.

---

## Observações e Armadilhas

- **`cloudCoveragePct` alto**: imagens com > 20% de nuvens têm acurácia reduzida. O serviço
  deve aguardar a próxima passagem disponível antes de processar um sinistro.
- **`claimId` nullable**: análises preventivas não têm sinistro associado. Ao cruzar
  `SatelliteAnalysis` com `Claim`, sempre usar LEFT JOIN.
- **`affectedAreaM2` vs `Plot.areaHectares`**: a área afetada pode ser maior que a área do
  talhão se o polígono WKT estiver desatualizado — validar na camada de serviço.
- **Acesso cross-service ao banco**: o `analise-svc` persiste `SatelliteAnalysis` diretamente
  no PostgreSQL (schema compartilhado). Isso é uma decisão de simplicidade para o MVP —
  em produção, o ideal seria via API ou evento para manter independência dos serviços.
