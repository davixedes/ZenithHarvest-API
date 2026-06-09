# Payment

## Visão Geral

`Payment` registra qualquer **movimentação financeira** da plataforma: indenização de
sinistro (PIX ao produtor), cobrança de prêmio, estorno ou repasse à seguradora. O design
usa uma constraint XOR no banco para garantir que cada pagamento está vinculado **ou** a
um sinistro **ou** a uma apólice — nunca aos dois.

`PaymentInvoice` é a fatura que agrupa cobranças de prêmio do produtor (entrada de dinheiro).

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Payment.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `paymentTypeId` | `Integer` | FK para `PaymentType` (lookup) |
| `paymentSituationId` | `Integer` | FK para `PaymentSituation` (lookup) |
| `claimId` | `UUID` | FK para `Claim` (nullable — XOR com `policyId`) |
| `policyId` | `UUID` | FK para `Policy` (nullable — XOR com `claimId`) |
| `paymentInvoiceId` | `UUID` | FK para `PaymentInvoice` (nullable) |
| `amount` | `BigDecimal(15,2)` | Valor em R$ |
| `pixKey` | `String(140)` | Chave PIX de destino (CPF, e-mail, telefone ou chave aleatória) |
| `sentAt` | `OffsetDateTime` | Quando o PIX foi enviado ao PSP |
| `confirmedAt` | `OffsetDateTime` | Quando o PSP confirmou o crédito |
| `pspTransactionId` | `String(100)` | ID da transação no PSP (para rastreabilidade) |
| `attempts` | `Short` | Número de tentativas de envio (default: 0) |
| `failureReason` | `String(500)` | Motivo da falha na última tentativa |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição |
| `deletedAt` | `OffsetDateTime` | Soft delete |

> **Constraint XOR no banco**: `CHECK (("ClaimId" IS NOT NULL)::int + ("PolicyId" IS NOT NULL)::int = 1)`
> Garante que exatamente um dos dois seja preenchido.

---

## Situação (`PaymentSituation`)

| ID | Descrição | Terminal | Sucesso |
|---|---|---|---|
| 1 | Pendente | Não | Não |
| 2 | Processando | Não | Não |
| 3 | Confirmado | **Sim** | **Sim** |
| 4 | Falhou | **Sim** | Não |
| 5 | Estornado | **Sim** | Não |

### Ciclo de vida

```
Pendente ──► Processando ──► Confirmado (terminal)
                   │
                   └──► Falhou (terminal) ──► (novo Payment criado para retry)
                                                      
Confirmado ──► Estornado (terminal, via solicitação)
```

---

## Tipo (`PaymentType`)

| ID | Descrição | Direção |
|---|---|---|
| 1 | Indenização de sinistro (PIX) | OUT |
| 2 | Pagamento de prêmio | IN |
| 3 | Estorno | OUT |
| 4 | Repasse à seguradora | OUT |

---

## Entidade Relacionada: `PaymentInvoice`

Fatura que consolida cobranças de prêmio mensais do produtor.

**Arquivo:** `core-svc/.../domain/entity/PaymentInvoice.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `code` | `Integer` | Código sequencial exibido no front |
| `invoiceNumber` | `String(30)` | Número único da fatura |
| `userId` | `UUID` | FK para `User` — produtor cobrado |
| `insurerId` | `UUID` | FK para `Insurer` — seguradora destinatária (nullable) |
| `totalAmount` | `BigDecimal(15,2)` | Valor total da fatura |
| `dueDate` | `LocalDate` | Vencimento |
| `issuedAt` | `OffsetDateTime` | Emissão (imutável) |
| `paidAt` | `OffsetDateTime` | Quando o produtor pagou (nullable) |
| `active` | `Boolean` | `false` = fatura desativada (usa flag, não soft-delete) |

---

## Fluxo de Pagamento de Sinistro

```
Claim → situação: Aprovado
  │
  ▼
Cria Payment (tipo: Indenização, situação: Pendente, claimId = Claim.id)
  │
  ▼
Job de pagamento PIX (assíncrono)
  ├─ Envia PIX ao PSP (sentAt = agora, situação: Processando)
  │
  ├─ PSP confirma
  │    ├─ Payment: situação → Confirmado, confirmedAt = agora, pspTransactionId
  │    ├─ Claim: situação → Pago, paidAt = agora
  │    └─ Policy.accumulatedPaid += approvedAmount
  │
  └─ PSP rejeita
       ├─ Payment: situação → Falhou, failureReason, attempts++
       └─ Claim: permanece Aprovado (aguarda retry)
```

---

## Observações e Armadilhas

- **Constraint XOR**: ao criar um `Payment` programaticamente, garantir que apenas um dos
  dois campos (`claimId` ou `policyId`) seja passado. O banco rejeita com `CHECK violation`
  se ambos forem preenchidos ou ambos forem nulos.
- **`attempts`**: tipo `Short` (max 32.767). O job de retry deve ter um limite máximo de
  tentativas (ex: 3) antes de marcar como `Falhou` definitivamente e acionar alerta.
- **Estorno**: ao estornar, criar novo `Payment` com `paymentTypeId = 3 (Estorno)` e
  decrementar `Policy.accumulatedPaid`. Não alterar o `Payment` original.
- **`PaymentInvoice.active`**: usa flag booleano em vez de `deletedAt` porque faturas
  nunca são deletadas por exigência contábil — apenas desativadas.
