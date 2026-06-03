---
name: rest-endpoint
description: Cria endpoints REST do Zenith Harvest no padrão do projeto (controller + service + DTO + validação + HATEOAS + tratamento de erro). Use ao adicionar qualquer rota nova ao core-svc ou analise-svc. Garante DTO na fronteira, @Valid, HATEOAS, e separação de camadas.
---

# Criar endpoint REST no padrão Zenith Harvest

Procedimento para adicionar uma rota seguindo a arquitetura em camadas do projeto.

## Camadas (sempre nesta ordem)
```
Controller (web/)  →  Service (application/service/)  →  Repository (domain/repository/)
       ↑ DTO request/response (application/dto/)
```
Regra de ouro: **controller NUNCA toca repository nem retorna @Entity**. Sempre DTO.

## 1. DTOs (application/dto/)

Request — com Bean Validation:
```java
public record CriarClaimRequest(
    @NotNull UUID policyId,
    @NotNull Integer categoryId,
    @NotNull Integer subCategoryId,
    @NotNull LocalDate eventDate,
    @Size(max = 500) String description
) {}
```

Response — nunca expõe a entity:
```java
public record ClaimResponse(
    UUID id,
    String claimNumber,
    Integer code,
    String situation,
    BigDecimal approvedAmount   // pode vir null se ainda não aprovado
) {}
```

## 2. Service (application/service/)

```java
@Service
public class ClaimService {
    private final ClaimRepository claimRepository;

    // injeção por construtor explícito — Spring injeta sozinho (1 construtor)
    public ClaimService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional
    public ClaimResponse abrir(CriarClaimRequest req) {
        // validações de negócio (vigência, carência, cobertura)
        // monta entity, salva, mapeia para response
    }

    @Transactional(readOnly = true)
    @Cacheable("claims")   // se for consulta de baixa volatilidade
    public ClaimResponse buscarPorId(UUID id) { ... }
}
```
- Regras de negócio ficam AQUI, não no controller.
- `@Transactional` em escrita; `readOnly = true` em leitura.

## 3. Controller (web/controller/)

```java
@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<EntityModel<ClaimResponse>> abrir(
            @Valid @RequestBody CriarClaimRequest req) {
        ClaimResponse created = claimService.abrir(req);
        return ResponseEntity
            .created(linkTo(methodOn(ClaimController.class).buscar(created.id())).toUri())
            .body(toModel(created));
    }

    @GetMapping("/{id}")
    public EntityModel<ClaimResponse> buscar(@PathVariable UUID id) {
        return toModel(claimService.buscarPorId(id));
    }

    // HATEOAS — links de navegação (requisito do edital Java)
    private EntityModel<ClaimResponse> toModel(ClaimResponse c) {
        return EntityModel.of(c,
            linkTo(methodOn(ClaimController.class).buscar(c.id())).withSelfRel());
    }
}
```

## 4. Tratamento de erro (web/handler/) — global, criar uma vez

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        // retorna 400 com lista de campos inválidos — sem stack trace
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(...) { /* 404 */ }
}
```

## Requisitos do edital a não esquecer
- **HATEOAS**: respostas de recurso usam `EntityModel`/`CollectionModel` com links.
- **Cache**: `@Cacheable` em GETs de baixa volatilidade (catálogo, lookups).
- **CORS**: garantir que a config global permite o app mobile (origem do Expo/app).
- **Swagger**: a rota aparece no `/swagger-ui.html` automaticamente (SpringDoc).
- **JWT**: rota protegida por padrão; marcar explicitamente as públicas.

## Checklist final
- [ ] Request DTO com `@Valid` + Bean Validation
- [ ] Response DTO (NUNCA retornar @Entity)
- [ ] Regra de negócio no service, não no controller
- [ ] `@Transactional` correto (readOnly em leitura)
- [ ] HATEOAS nos recursos
- [ ] Erros tratados pelo @RestControllerAdvice global
- [ ] Rota protegida por JWT (salvo se for explicitamente pública)