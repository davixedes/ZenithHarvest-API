package com.fiap.zenith.core.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publica eventos de sinistro no RabbitMQ. */
@Component
public class ClaimEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ClaimEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarSinistroAberto(SinistroAbertoEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_SINISTROS, "", event);
    }
}
