# Farm

## Visão Geral

`Farm` representa a **fazenda** do produtor rural — a unidade cadastral de nível mais alto
da propriedade. Contém dados de identificação legal (CAR, NIRF), geolocalização, área total
e bioma. Cada fazenda é subdividida em `Plot` (talhões), que são a unidade monitorada por
satélite.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Farm.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front (gerado pelo banco no INSERT) |
| `userId` | `UUID` | FK para `User` — produtor proprietário (armazenado como ID cru) |
| `name` | `String(150)` | Nome da fazenda |
| `carRegistration` | `String(50)` | Número do CAR (Cadastro Ambiental Rural) |
| `nirf` | `String(20)` | NIRF — número do imóvel na Receita Federal |
| `latitude` | `BigDecimal(10,7)` | Latitude centroide da fazenda |
| `longitude` | `BigDecimal(10,7)` | Longitude centroide da fazenda |
| `totalAreaHectares` | `BigDecimal(10,2)` | Área total em hectares |
| `state` | `String(2)` | UF (ex: `SP`, `MT`) |
| `biomeId` | `Integer` | FK para `Biome` (lookup) — influencia o fator de risco regional |
| `propertyType` | `String(30)` | Tipo de imóvel (ex: "Rural", "Assentamento") — nullable |
| `polygonWkt` | `String` (TEXT) | Polígono do perímetro em formato WKT para sobreposição de mapa |
| `active` | `Boolean` | `false` = fazenda inativada (sem excluir registros históricos) |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Lookup: `Biome`

| ID | Descrição | Fator de Risco Regional |
|---|---|---|
| 1 | Amazônia | 1.30 |
| 2 | Cerrado | 1.10 |
| 3 | Caatinga | 1.40 |
| 4 | Mata Atlântica | 1.00 |
| 5 | Pampa | 0.90 |
| 6 | Pantanal | 1.20 |

O `RegionalRiskFactor` do bioma é usado pelo motor de cotação para ajustar o prêmio de seguro.

---

## Relacionamentos

```
User (1) ──── (N) Farm (1) ──── (N) Plot
                       │
                       └── Biome (lookup)
```

---

## Observações e Armadilhas

- **`polygonWkt`**: campo TEXT com WKT (Well-Known Text) do polígono. Não é validado pelo
  JPA — a validação de geometria deve ser feita na camada de serviço ou via PostGIS se disponível.
- **`active` vs `deletedAt`**: a fazenda usa ambos. `active = false` desativa operacionalmente
  (bloqueia novos talhões/cotações) sem soft-delete. `deletedAt != null` é o apagamento lógico
  definitivo para LGPD.
- **`carRegistration` e `nirf`** não têm `unique` no banco — o mesmo imóvel pode ter dois
  registros se o usuário cadastrar errado. Validação de duplicidade deve ser feita no serviço.
