package com.fiap.zenith.core.infra.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ da Zenith Harvest.
 *
 * Exchange fanout "sinistros" → fila "sinistro.aberto" (consumida por analise-svc).
 * Exchange fanout "analises"  → fila "sinistro.analisado" (consumida por core-svc).
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_SINISTROS = "sinistros";
    public static final String EXCHANGE_ANALISES  = "analises";
    public static final String EXCHANGE_DLX       = "zenith.dlx";
    public static final String QUEUE_ABERTO       = "sinistro.aberto";
    public static final String QUEUE_ANALISADO    = "sinistro.analisado";
    public static final String QUEUE_ABERTO_DLQ   = "sinistro.aberto.dlq";

    @Bean
    public FanoutExchange sinistrosExchange() {
        return new FanoutExchange(EXCHANGE_SINISTROS, true, false);
    }

    @Bean
    public FanoutExchange analisesExchange() {
        return new FanoutExchange(EXCHANGE_ANALISES, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue filaAbertoQueue() {
        // DLX configurado para evitar loop infinito caso analise-svc falhe ao processar
        return QueueBuilder.durable(QUEUE_ABERTO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_ABERTO_DLQ)
                .build();
    }

    @Bean
    public Queue filaAnalisadoQueue() {
        return QueueBuilder.durable(QUEUE_ANALISADO).build();
    }

    @Bean
    public Binding bindingAberto(FanoutExchange sinistrosExchange, Queue filaAbertoQueue) {
        return BindingBuilder.bind(filaAbertoQueue).to(sinistrosExchange);
    }

    @Bean
    public Binding bindingAnalisado(FanoutExchange analisesExchange, Queue filaAnalisadoQueue) {
        return BindingBuilder.bind(filaAnalisadoQueue).to(analisesExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
