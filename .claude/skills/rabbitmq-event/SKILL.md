---
name: rabbitmq-event
description: Cria publishers e consumers de eventos RabbitMQ no padrão Zenith Harvest. Use ao implementar comunicação assíncrona entre core-svc e analise-svc — ex: evento de sinistro aberto, sinistro analisado, alerta emitido. Garante naming de exchange/queue/routing-key, payload DTO e idempotência.
---

# Criar evento RabbitMQ no padrão Zenith Harvest

A mensageria é requisito do edital Java. O fluxo principal: `core-svc` publica
`sinistro.aberto` → `analise-svc` consome, processa satélite/IA → publica
`sinistro.analisado` → `core-svc` consome e atualiza o Claim.

## Convenção de nomes

| Recurso | Padrão | Exemplo |
|---|---|---|
| Exchange | `zenith.<dominio>.exchange` (topic) | `zenith.sinistro.exchange` |
| Routing key | `<dominio>.<evento>` | `sinistro.aberto`, `sinistro.analisado` |
| Queue | `<servico>.<dominio>.<evento>.queue` | `analise.sinistro.aberto.queue` |
| DLQ | `<queue>.dlq` | `analise.sinistro.aberto.queue.dlq` |

## 1. Configuração (config/RabbitConfig.java)

```java
@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "zenith.sinistro.exchange";
    public static final String RK_ABERTO = "sinistro.aberto";
    public static final String QUEUE_ABERTO = "analise.sinistro.aberto.queue";

    @Bean TopicExchange sinistroExchange() { return new TopicExchange(EXCHANGE, true, false); }

    @Bean Queue filaAberto() {
        return QueueBuilder.durable(QUEUE_ABERTO)
            .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
            .build();
    }

    @Bean Binding bindAberto(Queue filaAberto, TopicExchange sinistroExchange) {
        return BindingBuilder.bind(filaAberto).to(sinistroExchange).with(RK_ABERTO);
    }

    // Serialização JSON do payload
    @Bean MessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }
}
```

## 2. Payload (evento) — DTO serializável

```java
public record SinistroAbertoEvent(
    UUID claimId,
    String claimNumber,
    UUID plotId,
    LocalDate eventDate,
    Integer eventTypeId,
    OffsetDateTime occurredAt   // quando o evento de negócio aconteceu
) implements Serializable {}
```
- Payload é DTO próprio do evento — NÃO envie a @Entity.
- Inclua um identificador único (claimId) para idempotência no consumer.

## 3. Publisher (infra/messaging/) — no core-svc

```java
@Component
public class SinistroPublisher {
    private final RabbitTemplate rabbit;

    public SinistroPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    public void publicarAberto(SinistroAbertoEvent event) {
        rabbit.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.RK_ABERTO,
            event
        );
    }
}
```
Chamar o publisher DEPOIS do commit da transação que criou o Claim (evita publicar
evento de algo que deu rollback). Idealmente via `@TransactionalEventListener(AFTER_COMMIT)`.

## 4. Consumer (infra/messaging/) — no analise-svc

```java
@Component
public class SinistroConsumer {
    private final AnaliseService analiseService;

    public SinistroConsumer(AnaliseService analiseService) {
        this.analiseService = analiseService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_ABERTO)
    public void onSinistroAberto(SinistroAbertoEvent event) {
        // IDEMPOTÊNCIA: se já processou esse claimId, ignora (pode haver redelivery)
        if (analiseService.jaProcessado(event.claimId())) return;

        analiseService.analisarSatelite(event);
        // ao fim, publica sinistro.analisado de volta
    }
}
```

## Princípios obrigatórios

- **Idempotência**: o consumer DEVE tolerar receber a mesma mensagem 2x (RabbitMQ garante
  at-least-once, não exactly-once). Cheque se já processou antes de agir.
- **Durabilidade**: exchange e queue `durable` (sobrevivem a restart do broker).
- **DLQ**: configurar dead-letter para mensagens que falham repetidamente — não perder evento.
- **Payload é DTO**, versionável, nunca a entity.
- **Publicar após commit** — nunca dentro da transação que ainda pode dar rollback.
- **Retry com limite**: falha transitória reprocessa; falha permanente vai pra DLQ.

## Checklist final
- [ ] Nomes seguem a convenção (exchange/rk/queue/dlq)
- [ ] Payload é DTO serializável com identificador único
- [ ] Publisher dispara após commit
- [ ] Consumer é idempotente
- [ ] Queue durável + DLQ configurada
- [ ] JSON converter registrado