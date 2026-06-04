---
description: Cria ou revisa entidades JPA do Zenith Harvest no padrão correto do projeto.
---

# Criar entidade JPA no padrão Zenith Harvest

Use este prompt ao criar ou alterar qualquer `@Entity` mapeada para o PostgreSQL.

## Antes de começar

1. Abra `db/schema/zenith_harvest_postgres.sql` e localize a tabela alvo.
2. Confirme se é **entidade operacional** (PK `UUID`) ou **lookup** (PK `Integer`).

## Regras de mapeamento

### PK

- Entidade operacional:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "Id", updatable = false, nullable = false)
private UUID id;
```

- Lookup:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "Id", updatable = false, nullable = false)
private Integer id;
```

### Tipos de coluna

- Dinheiro -> `BigDecimal` (`NUMERIC(15,2)`)
- NDVI -> `BigDecimal` (`NUMERIC(4,3)`)
- Percentual -> `BigDecimal` (`NUMERIC(5,2)`)
- `Code` -> `Integer`, gerado pelo banco, somente leitura
- `TIMESTAMPTZ` -> `OffsetDateTime`
- `TEXT` -> `String`
- `JSONB` -> `String` ou mapeamento JSON adequado ao Hibernate

### NOT NULL conservador

Use `nullable = false` apenas no que existe no momento da criação:

- PK
- FKs estruturais
- identificadores naturais
- descrições obrigatórias de lookup

Campos operacionais preenchidos depois devem continuar nullable:

- `ApprovedAt`
- `PaidAt`
- `CancelledAt`
- `CalculatedAmount`
- `ApprovedAmount`
- `MlConfidenceScore`
- `RejectionReasonId`

### Nomes

- Use exatamente os nomes do schema em PascalCase:
  - `@Table(name = "Claim")`
  - `@Column(name = "ClaimNumber")`
- Não escreva aspas manualmente; o projeto já usa `globally_quoted_identifiers: true`.

### Relacionamentos

- Prefira `fetch = FetchType.LAZY`
- Use `@JoinColumn(name = "...")` com o nome exato do schema

### Soft delete

Quando a tabela tiver esses campos, mapeie:

```java
@Column(name = "CreatedAt", nullable = false, updatable = false)
private OffsetDateTime createdAt;

@Column(name = "EditedAt")
private OffsetDateTime editedAt;

@Column(name = "DeletedAt")
private OffsetDateTime deletedAt;
```

## Convenções de código

- Entidade JPA **não** deve ser `record`
- Use construtor sem argumentos `protected`
- Use getters e setters explícitos
- Não use Lombok
- Se precisar facilitar criação, prefira factory method estático

## Checklist final

- PK correta (`UUID` ou `Integer`)
- `BigDecimal` para dinheiro/NDVI/percentual
- nomes exatos do schema
- campos operacionais nullable
- relacionamentos com `LAZY`
- sem Lombok
- entity não exposta direto na API
