package com.fiap.zenith.analise_svc.infra.messaging;

import com.fiap.zenith.analise_svc.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica o resultado da análise no exchange "analises" (consumido pelo core-svc).
 * Encapsula o RabbitTemplate — o consumer não conhece detalhes de transporte.
 */
@Component
public class ClaimAnalisadoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ClaimAnalisadoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(ClaimAnalisadoEvent evento) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_ANALISES, "", evento);
    }
}
