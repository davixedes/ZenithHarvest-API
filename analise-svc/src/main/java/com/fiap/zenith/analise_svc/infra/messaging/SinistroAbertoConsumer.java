package com.fiap.zenith.analise_svc.infra.messaging;

import com.fiap.zenith.analise_svc.application.service.SatelliteAnalysisService;
import com.fiap.zenith.analise_svc.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Consome o evento {@code sinistro.aberto} do core-svc, executa a análise satelital
 * e de IA, e publica o resultado em {@code sinistro.analisado}.
 *
 * Erros são enviados para a DLQ (sinistro.aberto.dlq) via Dead Letter Exchange,
 * evitando loop infinito de requeue.
 */
@Component
public class SinistroAbertoConsumer {

    private static final Logger log = LoggerFactory.getLogger(SinistroAbertoConsumer.class);

    private final SatelliteAnalysisService analysisService;
    private final RabbitTemplate rabbitTemplate;

    public SinistroAbertoConsumer(SatelliteAnalysisService analysisService,
                                   RabbitTemplate rabbitTemplate) {
        this.analysisService = analysisService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_ABERTO)
    public void processar(SinistroAbertoEvent evento) {
        try {
            ClaimAnalisadoEvent resultado = analysisService.analisarSinistro(evento);
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_ANALISES, "", resultado);
        } catch (Exception e) {
            log.error("Falha ao processar sinistro {} — enviando para DLQ. Causa: {}",
                    evento.claimNumber(), e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Erro na análise do sinistro " + evento.claimId(), e);
        }
    }
}
