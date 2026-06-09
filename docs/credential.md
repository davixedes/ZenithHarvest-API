# Credential

## Visão Geral

`Credential` armazena o hash da senha e o `secret` de cada usuário. É mantida separada de
`User` para que a camada de apresentação nunca acesse dados sensíveis por acidente — a
entidade não tem getters públicos de `password` expostos em DTOs.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/Credential.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `userId` | `UUID` | FK para `User` (1:1) |
| `password` | `String(255)` | Hash BCrypt da senha |
| `secret` | `String(255)` | Valor aleatório por usuário — usado como componente adicional na assinatura JWT ou como salt de segundo fator |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última troca de senha |
| `deletedAt` | `OffsetDateTime` | Soft delete |

---

## Observações

- **Nunca serializar em DTO**: a entidade não possui campos de resposta — só é lida
  internamente pelo `AuthService` no momento do login.
- **Troca de senha**: gera novo hash + novo `secret` e registra `AccessLogAction = PASSWORD_CHANGE`.
- **Soft delete**: ao inativar um usuário, a `Credential` associada também é marcada como
  deletada. A FK `userId` continua intacta para auditoria.
