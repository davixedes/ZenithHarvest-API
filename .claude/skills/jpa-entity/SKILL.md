---
name: jpa-entity
description: Cria entidades JPA (@Entity) do Zenith Harvest no padrão correto do projeto. Use ao criar ou alterar qualquer classe de entidade mapeada para o PostgreSQL — User, Farm, Plot, Claim, Policy, Payment, lookups (*Situation), etc. Garante UUID/Integer correto, aspas PascalCase, NOT NULL conservador, BigDecimal para dinheiro.
---

# Criar entidade JPA no padrão Zenith Harvest

Procedimento para mapear uma tabela do schema PostgreSQL para uma classe `@Entity`.

## Antes de começar
1. Abra o script SQL (`*_postgres.sql`) e localize a tabela alvo — use a definição REAL das colunas.
2. Decida: é **entidade** (UUID) ou **lookup** (Integer identity)?

## Regras de mapeamento

### Tipo da PK
- **Entidade** (User, Farm, Plot, Claim, Policy, Payment, Insurance...):
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "Id")
  private UUID id;
  ```
- **Lookup** (PlotSituation, ClaimSituation, Biome, ClaimEventType...):
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Id")
  private Integer id;
  ```

### Tipos de coluna (mapear do schema)
- Dinheiro → `BigDecimal` (coluna NUMERIC(15,2)). NUNCA `Double`/`Float`.
- NDVI → `BigDecimal` (NUMERIC(4,3)). Percentual → `BigDecimal` (NUMERIC(5,2)).
- `Code` (entidades operacionais) → `Integer`, gerado pelo banco (IDENTITY), só leitura:
  ```java
  @Column(name = "Code", insertable = false, updatable = false)
  private Integer code;
  ```
- Booleano → `Boolean` (Postgres BOOLEAN nativo).
- Timestamp → `OffsetDateTime` (coluna TIMESTAMPTZ).
- Texto longo / WKT → `String` (coluna TEXT).
- JSON (DescriptionJson) → `String` ou tipo JSON via Hibernate types (coluna JSONB).

### NOT NULL conservador (CRÍTICO)
Só use `nullable = false` em campos que existem na CRIAÇÃO da linha:
- PK, FKs estruturais, identificadores naturais (Cpf, ClaimNumber), Description de lookup.
Campos operacionais SÃO NULLABLE (preenchidos depois pelo fluxo):
- `approvedAt`, `paidAt`, `cancelledAt`, `ndviBefore`, `calculatedAmount`,
  `approvedAmount`, `mlConfidenceScore`, `rejectionReasonId`, etc.

### Nomes (PascalCase entre aspas)
- `@Table(name = "Claim")` — nome exato do schema.
- `@Column(name = "ClaimNumber")` — nome exato.
- O `application.yml` tem `globally_quoted_identifiers: true`, então NÃO escreva aspas
  manualmente nas anotações; só use o nome PascalCase exato.

### Relacionamentos
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "PolicyId")
private Policy policy;
```
- Use `LAZY` por padrão (evita N+1 e carregamento desnecessário).
- FK aponta para a coluna exata do schema.

### Soft delete (timestamps triplos)
Toda entidade que tem na tabela: `createdAt` / `editedAt` / `deletedAt` (OffsetDateTime).
```java
@Column(name = "CreatedAt", nullable = false, updatable = false)
private OffsetDateTime createdAt;

@Column(name = "EditedAt")
private OffsetDateTime editedAt;

@Column(name = "DeletedAt")
private OffsetDateTime deletedAt;
```

## Construtores e acessores (Java puro — SEM Lombok)

Entidade JPA não pode ser `record` (precisa ser mutável e ter construtor sem-args).
Escreva construtor protegido + getters/setters explícitos (a IDE gera com 1 atalho):

```java
@Entity
@Table(name = "Claim")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "ClaimNumber", nullable = false)
    private String claimNumber;

    protected Claim() {}   // JPA exige construtor sem-args

    // getters e setters explícitos (gerar pela IDE)
    public UUID getId() { return id; }
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }
}
```

NÃO usar Lombok (`@Data`, `@Getter`, `@Builder` etc.). O projeto é Java puro.
Para construção fluente, se necessário, escreva um método factory estático em vez de `@Builder`.

## Checklist final antes de entregar a entity
- [ ] PK do tipo certo (UUID entidade / Integer lookup)
- [ ] Dinheiro em BigDecimal
- [ ] Campos operacionais nullable
- [ ] Nomes PascalCase exatos do schema
- [ ] FKs com @JoinColumn no nome exato, fetch LAZY
- [ ] Timestamps de soft delete presentes
- [ ] Lookup de estado nomeado *Situation
- [ ] NÃO expor essa entity direto na API (criar DTO separado)