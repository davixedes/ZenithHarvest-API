# PreventiveAlert

## Visão Geral

`PreventiveAlert` é o **alerta preventivo** — um dos diferenciais competitivos do produto.
O sistema avisa o produtor de uma queda anormal de NDVI **antes** que vire perda total,
dando tempo de acionar irrigação, aplicar defensivos ou abrir um sinistro precocemente.

É gerado pelo `analise-svc` em varreduras periódicas e entregue via push notification
(severidade ≥ 2) ou SMS (severidade 3).

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/PreventiveAlert.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `plotId` | `UUID` | FK para `Plot` — talhão com anomalia detectada |
| `alertTypeId` | `Integer` | FK para `AlertType` (lookup) — tipo de risco detectado |
| `alertSeverityId` | `Integer` | FK para `AlertSeverity` (lookup) — nível de urgência |
| `alertSituationId` | `Integer` | FK para `AlertSituation` (lookup) — ciclo de vida do alerta |
| `message` | `String` (TEXT) | Mensagem gerada pela IA descrevendo o problema e recomendando ação |
| `observedNdvi` | `BigDecimal(4,3)` | NDVI observado na análise que gerou o alerta |
| `expectedNdvi` | `BigDecimal(4,3)` | NDVI esperado para a cultura nesta fase do ciclo |
| `dropPct` | `BigDecimal(5,2)` | Percentual de queda do NDVI em relação ao esperado |
| `issuedAt` | `OffsetDateTime` | Quando o alerta foi emitido (imutável) |
| `viewedAt` | `OffsetDateTime` | Quando o produtor visualizou (nullable) |
| `createdAt` | `OffsetDateTime` | Timestamp de persistência (imutável) |

---

## Lookup: `AlertType`

| ID | Descrição |
|---|---|
| 1 | Queda de NDVI |
| 2 | Risco de seca |
| 3 | Risco de geada |
| 4 | Anomalia detectada |

---

## Lookup: `AlertSeverity`

| ID | Descrição | Nível | Cor | Push | SMS |
|---|---|---|---|---|---|
| 1 | Informativo | 1 | `#2E7D32` (verde) | Não | Não |
| 2 | Atenção | 2 | `#F9A825` (amarelo) | **Sim** | Não |
| 3 | Crítico | 3 | `#C62828` (vermelho) | **Sim** | **Sim** |

---

## Situação (`AlertSituation`)

| ID | Descrição | Terminal |
|---|---|---|
| 1 | Aberto | Não |
| 2 | Visualizado | Não |
| 3 | Resolvido | **Sim** |
| 4 | Descartado | **Sim** |

### Ciclo de vida

```
Aberto ──► Visualizado (produtor abre o app)
    │             │
    │             ├──► Resolvido (produtor toma ação + confirma)
    │             └──► Descartado (falso positivo ou irrelevante)
    │
    └──► Descartado (automático, se sinistro foi aberto para o mesmo evento)
```

---

## Lógica de Geração (analise-svc)

```python
# Pseudo-código da lógica de detecção
para cada Plot com Policy vigente:
    analise = ultima_satellite_analysis(plot)
    se analise.cloudCoveragePct > 20%: pular
    
    ndvi_esperado = (crop.expectedNdviMin + crop.expectedNdviMax) / 2
    queda_pct = (ndvi_esperado - analise.meanNdvi) / ndvi_esperado * 100
    
    se queda_pct >= 50%: severidade = CRÍTICO
    se queda_pct >= 30%: severidade = ATENÇÃO
    se queda_pct >= 15%: severidade = INFORMATIVO
    caso contrário: não emitir alerta
    
    mensagem = IA.gerarLaudoPreventivo(plot, analise, severidade)  # Spring AI / Ollama
    
    persistir PreventiveAlert
    notificar produtor (push / SMS conforme severidade)
```

---

## Mensagem Gerada por IA

O campo `message` é gerado pelo `analise-svc` via **Spring AI + Ollama** (RAG com cartilhas
Embrapa/SUSEP). Exemplo para uma queda de NDVI severa em soja:

> "Detectamos queda de 42% no índice de vegetação do Talhão A3 (Soja) nos últimos 10 dias.
> O NDVI atual de 0.38 está abaixo do mínimo esperado de 0.65 para esta fase do ciclo.
> Recomendamos verificar sinais de estresse hídrico e considerar irrigação suplementar.
> Caso identifique danos, abra um sinistro pelo aplicativo."

---

## Observações e Armadilhas

- **`viewedAt` nullable**: nunca preencher via setter direto em fluxos de edição de dados
  cadastrais — só deve ser atualizado quando o produtor efetivamente visualizar o alerta
  no app.
- **Não duplicar alertas**: antes de criar um novo alerta, verificar se já existe um
  `PreventiveAlert` `Aberto` ou `Visualizado` para o mesmo `plotId` e `alertTypeId` no
  período. Excesso de alertas gera ruído e o produtor para de ler.
- **`dropPct`**: calculado em relação ao NDVI esperado (média do range da cultura), não
  ao NDVI histórico do talhão. Isso torna o sistema mais sensível em lavouras que já
  vinham com NDVI abaixo do ideal.
