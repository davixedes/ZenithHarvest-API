package com.fiap.zenith.core.infra.messaging;

import com.fiap.zenith.core.domain.entity.Claim;
import com.fiap.zenith.core.domain.repository.ClaimRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Consome o resultado da análise do analise-svc e atualiza o sinistro com os dados
 * de NDVI, percentual de perda, valor calculado e score de confiança da IA.
 */
@Component
public class ClaimAnalysisConsumer {

    private final ClaimRepository claimRepository;

    public ClaimAnalysisConsumer(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_ANALISADO)
    @Transactional
    public void onAnaliseRecebida(ClaimAnalisadoEvent event) {
        claimRepository.findByIdAndDeletedAtIsNull(event.claimId()).ifPresent(claim -> {
            atualizarComResultado(claim, event);
            claimRepository.save(claim);
        });
    }

    // Situações em que a análise ainda pode decidir o estado do sinistro.
    private static final int SITUACAO_ABERTO = 1;
    private static final int SITUACAO_EM_ANALISE = 2;

    private void atualizarComResultado(Claim claim, ClaimAnalisadoEvent event) {
        // Dados da análise são sempre gravados (métricas reais do satélite/IA).
        claim.setNdviAfter(event.ndviAfter());
        claim.setTotalLossPct(event.totalLossPct());
        claim.setTotalAffectedAreaHa(event.totalAffectedAreaHa());
        claim.setCalculatedAmount(event.calculatedAmount());
        claim.setMlConfidenceScore(event.mlConfidenceScore());
        claim.setFraudFlag(event.fraudFlag());

        // A transição de situação só vale se o sinistro ainda não foi decidido por um humano.
        // Evita que um evento atrasado reverta um sinistro já aprovado/pago/rejeitado (race condition).
        boolean aindaPodeDecidir = claim.getClaimSituationId() == SITUACAO_ABERTO
                || claim.getClaimSituationId() == SITUACAO_EM_ANALISE;
        if (event.newSituationId() != null && aindaPodeDecidir) {
            claim.setClaimSituationId(event.newSituationId());
        }
        claim.setEditedAt(OffsetDateTime.now());
    }
}
