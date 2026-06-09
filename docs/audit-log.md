# AuditLog

## Visão Geral

`AuditLog` é a trilha de auditoria **cross-cutting** para operações sensíveis — exigência
da LGPD. Cobre eventos que não pertencem a nenhum domínio específico (login, logout,
anonimização, bloqueio de conta) e operações críticas que precisam de rastreabilidade
legal permanente.

O projeto usa **arquitetura de auditoria híbrida**:
- **`AuditLog` (Postgres)**: operações LGPD e cross-cutting com garantia transacional.
- **Collections MongoDB** (`claim_audit_events`, `policy_audit_events`, `payment_audit_events`):
  trilhas detalhadas de negócio, com payload completo de cada transição de estado.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/AuditLog.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `userId` | `UUID` | Usuário que executou a operação (nullable — operações de sistema) |
| `systemActor` | `String(50)` | Identificador do serviço/job quando não há usuário humano |
| `operation` | `String(30)` | Tipo da operação: `LOGIN`, `LOGOUT`, `PASSWORD_CHANGE`, `ANONYMIZATION`, `BLOCK`, etc. |
| `targetEntity` | `String(50)` | Nome da entidade afetada (ex: `User`, `Claim`) |
| `targetEntityId` | `String(64)` | ID polimórfico da entidade afetada — `VARCHAR` porque pode ser UUID ou Integer |
| `descriptionJson` | `String` (JSONB) | Payload JSON com detalhes adicionais da operação |
| `ip` | `String(45)` | IP de origem (suporta IPv6) |
| `userAgent` | `String(500)` | User-Agent do cliente |
| `requestId` | `UUID` | ID de correlação da requisição HTTP (nullable) |
| `success` | `Boolean` | `true` = operação executada com sucesso (default: true) |
| `failureReason` | `String(500)` | Motivo da falha quando `success = false` |
| `operatedAt` | `OffsetDateTime` | Timestamp da operação (imutável) |

---

## Operações Rastreadas

| Operação | Quando Gravar | `targetEntity` | `targetEntityId` |
|---|---|---|---|
| `LOGIN` | Login bem-sucedido ou falho | `User` | `User.id` (ou `null`) |
| `LOGOUT` | Logout explícito | `User` | `User.id` |
| `PASSWORD_CHANGE` | Troca de senha | `Credential` | `User.id` |
| `ANONYMIZATION` | LGPD: remoção de dados pessoais | `User` | `User.id` |
| `BLOCK` | Bloqueio de conta | `User` | `User.id` |

> **Nota**: logins são preferencialmente rastreados via `AccessLog` (mais detalhado).
> `AuditLog` registra os eventos LGPD que exigem retenção com garantia transacional.

---

## Exemplo de `descriptionJson`

```json
{
  "reason": "Solicitação do titular via portal LGPD",
  "fieldsAnonymized": ["cpf", "name", "email", "phone"],
  "requestProtocol": "LGPD-2026-001234"
}
```

---

## Arquitetura Híbrida de Auditoria

```
Evento de negócio (ex: Claim muda de status)
  ├─ AuditLog (Postgres): se for operação LGPD ou cross-cutting
  └─ claim_audit_events (MongoDB): sempre — payload completo da transição
         {
           "claimId": "<uuid>",
           "fromStatus": "Em análise",
           "toStatus": "Aprovado",
           "changedBy": "<userId>",
           "changedAt": "2026-06-09T14:30:00Z",
           "approvedAmount": 40625.00
         }
```

---

## Observações e Armadilhas

- **Imutável**: `AuditLog` é append-only — não há `editedAt` nem `deletedAt`. Qualquer
  tentativa de `UPDATE` na tabela deve ser bloqueada por policy no banco.
- **`targetEntityId` como `VARCHAR`**: design intencional para suportar tanto UUIDs de
  entidades quanto IDs Integer de lookups sem precisar de colunas separadas.
- **Retenção mínima**: registros de `ANONYMIZATION` devem ser retidos por 5 anos mesmo
  após a anonimização do usuário — é a prova de que a solicitação LGPD foi atendida.
- **`systemActor`**: usado por jobs automáticos (ex: `job-expirador-cotacoes`,
  `analise-svc`). Quando `systemActor != null`, `userId` deve ser `null`.
- **Não usar para trilha de negócio**: o volume de eventos de negócio (cada transição
  de `Claim`, cada pagamento) tornaria o PostgreSQL um gargalo. Use as collections MongoDB
  para isso.
