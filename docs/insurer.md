# Insurer

## Visão Geral

`Insurer` representa a **seguradora parceira** credenciada na plataforma Zenith Harvest
(Brasilseg, Porto Seguro, Mapfre, Tokio Marine, etc.). É um ator B2B: não acessa a plataforma
diretamente — seus produtos (`Insurance`) são ofertados aos produtores e ela recebe repasses
financeiros após sinistros pagos.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Insurer.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `corporateName` | `String(200)` | Razão social |
| `tradeName` | `String(150)` | Nome fantasia (nullable) |
| `cnpj` | `String(18)` | CNPJ único — formato `00.000.000/0000-00` |
| `susepCode` | `String(20)` | Código SUSEP da seguradora (nullable, único) |
| `commercialEmail` | `String(150)` | E-mail comercial para notificações (nullable) |
| `phone` | `String(20)` | Telefone de contato (nullable) |
| `logoUrl` | `String(500)` | URL do logotipo para exibição no app |
| `adminFeePct` | `BigDecimal(5,2)` | Taxa de administração Zenith (default: 5.00%) |
| `takeRatePct` | `BigDecimal(5,2)` | Take rate da plataforma sobre o prêmio (default: 8.00%) |
| `active` | `Boolean` | `false` = suspensa temporariamente (sem deletar produtos) |
| `insurerSituationId` | `Integer` | FK para `InsurerSituation` (lookup) |
| `accreditedAt` | `LocalDate` | Data de credenciamento |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Situação (`InsurerSituation`)

| ID | Descrição | Permite Novas Apólices |
|---|---|---|
| 1 | Ativa | Sim |
| 2 | Suspensa | Não |
| 3 | Descredenciada | Não |

### Ciclo de vida

```
Ativa ──► Suspensa ──► Ativa (reativação)
  │
  └──► Descredenciada (terminal)
```

---

## Relacionamentos

```
Insurer (1) ──── (N) Insurance
         (1) ──── (N) Policy (via Insurance)
         (1) ──── (N) PaymentInvoice (repasses)
```

---

## Observações e Armadilhas

- **`adminFeePct` e `takeRatePct`**: definem a divisão financeira entre a seguradora e a
  Zenith. Alterações afetam o cálculo de prêmio em **novas** cotações; apólices em vigor
  não são retroativamente recalculadas.
- **`cnpj` e `susepCode`** são `unique` no banco — inserções duplicadas lançam
  `DataIntegrityViolationException`.
- **Seguradora descredenciada**: apólices e sinistros existentes continuam válidos — apenas
  novos produtos e apólices ficam bloqueados.
