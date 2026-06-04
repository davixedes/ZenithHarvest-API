# Zenith Harvest — instruções para GitHub Copilot

## Visão geral do produto

Zenith Harvest é um monorepo de microsserviços Java para seguro paramétrico agrícola via satélite. O produto monitora lavouras por NDVI, detecta perdas com visão computacional + IA e paga sinistros via PIX.

**Stack principal:** Java 21, Spring Boot 3.5.14, PostgreSQL, MongoDB, RabbitMQ, Spring AI com Ollama.

## Arquitetura

| Serviço | Porta | Responsabilidade |
|---|---|---|
| `gateway` | 8080 | Borda. Roteamento, validação de JWT, rate limit. Spring Cloud Gateway. |
| `core-svc` | 8081 | CRUD do domínio, auth/JWT, financeiro e publicação de eventos RabbitMQ. |
| `analise-svc` | 8082 | Análise satelital/NDVI, IA generativa, consumo de eventos e geração de laudos. |

### Comunicação entre serviços

- Síncrona: `core-svc` chama `analise-svc` via OpenFeign.
- Assíncrona: `core-svc` publica `sinistro.aberto` no RabbitMQ; `analise-svc` consome e devolve `sinistro.analisado`.

### Fora deste repositório

- A API .NET é outra entrega e não faz parte deste código.
- O app mobile React Native consome estas APIs, mas também está fora deste repositório.

## Estrutura esperada

```text
src/main/java/com/fiap/zenith/<svc>/
 ├─ config/         # Security, OpenAPI, RabbitMQ, Feign
 ├─ domain/
 │   ├─ entity/     # @Entity JPA
 │   ├─ repository/ # Spring Data repositories
 │   └─ enums/
 ├─ application/
 │   ├─ service/    # regras de negócio
 │   ├─ dto/        # request/response
 │   └─ mapper/     # entity -> response DTO
 ├─ web/
 │   ├─ controller/ # @RestController
 │   └─ handler/    # tratamento global de erro
 └─ infra/
     ├─ feign/
     └─ messaging/
```

## Banco de dados

### Fonte de verdade

Antes de criar ou alterar qualquer `@Entity`, consulte sempre:

- `db/schema/zenith_harvest_postgres.sql`

Esse arquivo define os nomes e tipos canônicos de tabelas e colunas.

### Regras de modelagem JPA

- Tabelas e colunas usam **PascalCase** e precisam estar entre aspas no Postgres.
- O projeto já usa `spring.jpa.properties.hibernate.globally_quoted_identifiers: true`.
- PK de entidade operacional: `UUID` com `GenerationType.UUID`.
- PK de lookup: `Integer`.
- `Code` existe apenas em entidades operacionais específicas; não existe em lookups.
- Lookups de estado usam o sufixo `*Situation`, nunca `*Status`.
- Use `BigDecimal` para dinheiro, NDVI e percentuais.
- Campos operacionais preenchidos em etapas futuras do fluxo devem continuar nullable.
- Não use `ddl-auto: update`; o projeto usa schema SQL e `validate`.

### Domínios relevantes

- Operacional: `Policy`, `PolicyItem`, `Claim`, `ClaimItem`, `SatelliteAnalysis`, `PreventiveAlert`
- Financeiro: `Payment`, `PaymentInvoice`
- Auditoria: `AuditLog`

Detalhes importantes do schema:

- `Payment` tem regra XOR entre `ClaimId` e `PolicyId`.
- `PreventiveAlert.AlertSituationId` é FK para lookup.
- `Claim.CalculatedAmount` e `ApprovedAmount` são monetários.
- `AuditLog.DescriptionJson` é `JSONB`; `TargetEntityId` é string polimórfica.

## Convenções de código

- Use arquitetura em camadas: controller -> service -> repository.
- Controller nunca acessa repository diretamente.
- Controller nunca retorna `@Entity`; sempre use DTO.
- Use `record` para DTOs e eventos.
- Não use Lombok.
- Use injeção por construtor explícito.
- Use `@Valid` e Bean Validation em requests.
- Use `@Transactional` em escrita e `@Transactional(readOnly = true)` em leitura.
- No `core-svc`, respostas REST devem usar HATEOAS (`EntityModel`/`CollectionModel`).
- Tratamento global de erro deve passar por `@RestControllerAdvice`.
- Use `@Cacheable` em consultas de baixa volatilidade, como lookups e catálogos.
- Comentários e nomes podem estar em português, acompanhando o domínio do projeto.

## Prompts reutilizáveis do Copilot

Além destas instruções globais, o repositório possui prompt files em `.github/prompts/`.
Quando a tarefa combinar com um deles, consulte e siga esse prompt antes de responder ou gerar código.

| Prompt file | Origem | Quando usar |
|---|---|---|
| `.github/prompts/jpa-entity.prompt.md` | skill | Ao criar ou alterar qualquer `@Entity` JPA |
| `.github/prompts/rest-endpoint.prompt.md` | skill | Ao adicionar endpoint REST no `core-svc` ou `analise-svc` |
| `.github/prompts/rabbitmq-event.prompt.md` | skill | Ao implementar publisher/consumer RabbitMQ |
| `.github/prompts/feature-planner.prompt.md` | agent | Antes de implementar feature nova ou quando o pedido ainda precisa de desenho técnico |
| `.github/prompts/code-reviewer.prompt.md` | agent | Após alterar classes Java, antes de considerar o trabalho pronto |
| `.github/prompts/solid-reviewer.prompt.md` | agent | Quando houver pedido explícito de revisão SOLID ou suspeita de violação SOLID |

### Ordem preferencial de uso

Quando aplicável, siga esta sequência:

1. `feature-planner.prompt.md` para desenhar a solução
2. `jpa-entity.prompt.md`, `rest-endpoint.prompt.md` e/ou `rabbitmq-event.prompt.md` para implementar
3. `code-reviewer.prompt.md` para revisão final
4. `solid-reviewer.prompt.md` apenas quando o usuário pedir revisão SOLID ou quando houver risco claro de acoplamento/responsabilidade excessiva

### Regras de uso desses prompts

- Não ignore um prompt especializado quando a tarefa corresponder claramente a ele.
- Reaproveite os padrões desses arquivos em vez de improvisar estrutura diferente.
- Em revisão, aponte correção concreta; em planejamento, não escreva código de produção.
- Se houver conflito entre um prompt especializado e estas instruções globais, preserve o schema SQL, as convenções de arquitetura e os requisitos obrigatórios do projeto.

## Requisitos obrigatórios do projeto Java

Ao implementar código, preserve estes requisitos:

- microsserviços com responsabilidades separadas
- RabbitMQ em pelo menos um fluxo relevante
- Spring Security com JWT
- HATEOAS no `core-svc`
- cache com Spring Cache
- CORS configurado
- Swagger/OpenAPI com SpringDoc
- Spring AI no `analise-svc`

## Regras por serviço

### `gateway`

- É gateway reativo.
- Não adicionar Spring Web MVC nele.

### `core-svc`

- Centraliza CRUD do domínio, auth, financeiro e publicação de eventos.
- APIs devem usar DTOs, validação, HATEOAS e tratamento global de erro.

### `analise-svc`

- Centraliza análise satelital, IA generativa, consumo de eventos e persistência adicional em MongoDB.

## Build e execução

Comandos comuns:

```bash
mvn clean install
docker compose up -d
cd core-svc && mvn spring-boot:run
```

## O que evitar

- Não expor entidades JPA direto na API.
- Não usar `float` ou `double` para valores monetários.
- Não criar `Code` em lookups.
- Não trocar `*Situation` por `*Status`.
- Não adicionar NOT NULL em campo que só existe numa etapa futura do fluxo.
- Não usar `ddl-auto: update`.
- Não colocar Spring MVC no `gateway`.

## Diretriz prática para geração de código

Quando implementar uma feature:

1. Consulte o schema SQL antes de tocar em entities.
2. Siga o padrão DTO -> service -> repository -> controller.
3. Preserve nomes de tabelas/colunas exatamente como no banco.
4. Reutilize padrões já existentes em `core-svc` e `analise-svc`.
5. Prefira mudanças pequenas, coerentes e alinhadas às convenções do monorepo.
