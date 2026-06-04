---
description: Cria publishers e consumers RabbitMQ no padrão Zenith Harvest, com DTO de evento, convenção de nomes, durabilidade e idempotência.
---

# Criar evento RabbitMQ no padrão Zenith Harvest

Use este prompt ao implementar comunicação assíncrona entre `core-svc` e `analise-svc`.

Fluxo principal do projeto:

- `core-svc` publica `sinistro.aberto`
- `analise-svc` consome e processa
- `analise-svc` publica `sinistro.analisado`
- `core-svc` consome e atualiza o `Claim`

## Convenção de nomes

| Recurso | Padrão | Exemplo |
|---|---|---|
| Exchange | `zenith.<dominio>.exchange` | `zenith.sinistro.exchange` |
| Routing key | `<dominio>.<evento>` | `sinistro.aberto` |
| Queue | `<servico>.<dominio>.<evento>.queue` | `analise.sinistro.aberto.queue` |
| DLQ | `<queue>.dlq` | `analise.sinistro.aberto.queue.dlq` |

## Configuração

- exchange e queues devem ser `durable`
- registrar serialização JSON
- configurar DLQ

## Payload do evento

- usar `record`
- nunca enviar `@Entity`
- incluir identificador único para idempotência, como `claimId`

Exemplo:

```java
public record SinistroAbertoEvent(
    UUID claimId,
    String claimNumber,
    UUID plotId,
    Integer eventTypeId,
    OffsetDateTime occurredAt
) implements Serializable {}
```

## Publisher

- publicar no `infra/messaging/`
- enviar o evento apenas **após commit**
- preferir `@TransactionalEventListener(phase = AFTER_COMMIT)` quando aplicável

## Consumer

- consumir com `@RabbitListener`
- garantir idempotência
- tolerar redelivery
- enviar para DLQ em falhas permanentes

## Princípios obrigatórios

- payload é DTO
- consumer idempotente
- exchange e queue duráveis
- DLQ configurada
- publicação após commit
- retry com limite

## Checklist final

- convenção de nomes correta
- payload serializável com identificador único
- publisher após commit
- consumer idempotente
- queue durável com DLQ
- conversor JSON registrado
