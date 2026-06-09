# AccessLog

## Visão Geral

`AccessLog` registra toda tentativa de autenticação — sucedida ou não. É a trilha de acesso
exigida pela LGPD: quem entrou, quando, de qual IP, e se falhou por quê. Junto com `AuditLog`
(que cobre operações de negócio), forma a camada de rastreabilidade de segurança.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/AccessLog.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `userId` | `UUID` | FK para `User` — **nullable**: em falhas de login por e-mail inexistente o userId é desconhecido |
| `accessLogActionId` | `Integer` | FK para `AccessLogAction` (lookup) |
| `ip` | `String(45)` | IP de origem (suporta IPv6) |
| `userAgent` | `String(500)` | User-Agent do cliente |
| `success` | `Boolean` | `true` = autenticação bem-sucedida |
| `failureReason` | `String(200)` | Motivo da falha quando `success = false` |
| `createdAt` | `OffsetDateTime` | Timestamp do evento (imutável) |

---

## Lookup: `AccessLogAction`

| ID | Descrição |
|---|---|
| 1 | `LOGIN_SUCCESS` |
| 2 | `LOGIN_FAILURE` |
| 3 | `LOGOUT` |
| 4 | `PASSWORD_CHANGE` |
| 5 | `ANONYMIZATION` |
| 6 | `BLOCK` |

---

## Observações

- **Imutável**: `AccessLog` nunca é atualizado após criação — é append-only por design.
- **`userId` nullable**: quando o login falha por e-mail não encontrado, não há `userId`
  para associar. O `failureReason` captura o contexto.
- **Retenção**: registros de `ANONYMIZATION` e `BLOCK` devem ser retidos pelo prazo legal
  mesmo após soft-delete do `User`.
