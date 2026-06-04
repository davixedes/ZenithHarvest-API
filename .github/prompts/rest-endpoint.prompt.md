---
description: Cria endpoints REST do Zenith Harvest no padrão do projeto, com DTO, service, validação, HATEOAS e tratamento de erro.
---

# Criar endpoint REST no padrão Zenith Harvest

Use este prompt ao adicionar rotas no `core-svc` ou `analise-svc`.

## Ordem das camadas

```text
Controller (web/) -> Service (application/service/) -> Repository (domain/repository/)
       ^ DTO request/response (application/dto/)
```

Regra de ouro:

- controller nunca acessa repository direto
- controller nunca retorna `@Entity`
- API sempre usa DTO

## DTOs

### Request

- usar `record`
- aplicar Bean Validation
- receber com `@Valid`

Exemplo:

```java
public record CriarClaimRequest(
    @NotNull UUID policyId,
    @NotNull Integer categoryId,
    @NotNull Integer subCategoryId,
    @Size(max = 500) String description
) {}
```

### Response

- usar `record`
- nunca expor entity diretamente

## Service

- regras de negócio ficam aqui
- usar injeção por construtor explícito
- escrita com `@Transactional`
- leitura com `@Transactional(readOnly = true)`
- usar `@Cacheable` apenas em consultas de baixa volatilidade

## Controller

- usar `@RestController`
- documentar com OpenAPI quando fizer sentido
- no `core-svc`, retornar `EntityModel` / `CollectionModel`
- incluir links HATEOAS
- usar `ResponseEntity.created(...)` em criação

## Tratamento de erro

- usar o `@RestControllerAdvice` global do projeto
- não espalhar `try/catch` em controller para fluxo normal

## Segurança

- endpoints são protegidos por JWT por padrão
- só rotas explicitamente públicas devem ser liberadas em `SecurityConfig`

## Checklist final

- request DTO com validação
- response DTO separado
- regra de negócio no service
- transaction correta
- HATEOAS aplicado no `core-svc`
- rota protegida por JWT, salvo exceção explícita
- sem retorno direto de entity
