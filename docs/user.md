# User

## Visão Geral

`User` representa o **produtor rural** cadastrado na plataforma Zenith Harvest. É a entidade
central do domínio de acesso: todas as fazendas, cotações, apólices e sinistros partem de um
usuário ativo. O acesso é controlado por `Credential` (hash BCrypt separado da entidade) e
toda autenticação é rastreada via `AccessLog` para conformidade LGPD.

---

## Entidade Principal

**Arquivo:** `core-svc/src/main/java/com/fiap/zenith/core/domain/entity/User.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária — `gen_random_uuid()` |
| `code` | `Integer` | Código sequencial exibido no front (1 em 1, gerado pelo banco no INSERT) |
| `cpf` | `String(18)` | CPF único — formato `000.000.000-00` |
| `name` | `String(150)` | Primeiro nome |
| `lastName` | `String(150)` | Sobrenome |
| `email` | `String(150)` | E-mail único — usado no login |
| `phone` | `String(20)` | Telefone de contato |
| `addressId` | `UUID` | FK para `Address` — armazenado como ID cru (sem `@ManyToOne`) |
| `lastLoginAt` | `OffsetDateTime` | Última autenticação bem-sucedida |
| `createdAt` | `OffsetDateTime` | Timestamp de criação (imutável) |
| `editedAt` | `OffsetDateTime` | Última edição de dados cadastrais |
| `deletedAt` | `OffsetDateTime` | Soft delete — `NULL` = ativo |

---

## Entidades Relacionadas

### `Address`
Endereço postal do produtor. Compõe o `User` mas vive em tabela separada para reutilização.

**Arquivo:** `core-svc/.../domain/entity/Address.java`

| Propriedade | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Chave primária |
| `street` | `String(150)` | Logradouro |
| `number` | `Integer` | Número |
| `neighboor` | `String(100)` | Bairro |
| `city` | `String(100)` | Cidade |
| `complement` | `String(150)` | Complemento (nullable) |
| `postalCode` | `String(9)` | CEP — formato `00000-000` |
| `uf` | `String(2)` | UF (sigla de 2 chars) |
| `country` | `String(60)` | País |

> `Address` não possui timestamps de soft delete — é sempre substituído integralmente
> quando o endereço muda (delete + novo insert).

### `Credential`
Veja [credential.md](credential.md).

### `AccessLog`
Veja [access-log.md](access-log.md).

---

## Fluxo de Cadastro

```
POST /auth/register
  ├─ Valida CPF e e-mail únicos
  ├─ Persiste Address
  ├─ Persiste User (addressId = Address.id)
  ├─ Persiste Credential (BCrypt hash do password, secret aleatório)
  └─ Retorna UserResponse (sem Credential)
```

---

## Fluxo de Autenticação

```
POST /auth/login
  ├─ Busca User por e-mail
  ├─ Verifica BCrypt: Credential.password vs payload
  ├─ Grava AccessLog (LOGIN_SUCCESS ou LOGIN_FAILURE)
  ├─ Atualiza User.lastLoginAt
  └─ Retorna JWT assinado com sub=User.id, roles=["PRODUTOR"]
```

---

## Observações e Armadilhas

- **Nunca expor `Credential`** em nenhum DTO de resposta — a entidade não tem getter de
  `password` acessível na API.
- **`addressId` como ID cru**: a decisão de não usar `@ManyToOne` foi intencional — evita
  lazy-load acidental em contextos onde o endereço não é necessário. Buscar o endereço
  requer chamada explícita ao `AddressRepository`.
- **CPF e e-mail são `unique` no banco**: violações lançam `DataIntegrityViolationException`
  que o `GlobalExceptionHandler` converte em `409 Conflict`.
- **Soft delete**: filtrar `deletedAt IS NULL` em todas as queries. Um usuário deletado
  mantém histórico de sinistros e apólices intacto por LGPD.
