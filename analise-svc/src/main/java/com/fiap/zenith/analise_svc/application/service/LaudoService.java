package com.fiap.zenith.analise_svc.application.service;

import com.fiap.zenith.analise_svc.infra.messaging.SinistroAbertoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Gera laudos técnicos de sinistro usando Spring AI (Ollama/llama3.2).
 * O laudo é gerado em linguagem natural com base nos dados do evento e NDVI calculado.
 */
@Service
public class LaudoService {

    private static final Logger log = LoggerFactory.getLogger(LaudoService.class);

    private static final String SYSTEM_PROMPT = """
            Você é um perito agrícola especialista em seguros paramétricos da Zenith Harvest.
            Sua função é gerar laudos técnicos objetivos e profissionais para sinistros agrícolas.
            Use linguagem formal, mas acessível ao produtor rural.
            Sempre mencione o NDVI (Normalized Difference Vegetation Index) e sua interpretação.
            Máximo de 3 parágrafos.
            """;

    private final ChatClient chatClient;

    public LaudoService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public String gerarLaudo(SinistroAbertoEvent evento, BigDecimal ndviAfter,
                              BigDecimal totalLossPct, BigDecimal mlConfidenceScore) {
        String prompt = """
                Sinistro: %s
                Apólice: %s
                Talhão: %s
                NDVI antes do evento: %s
                NDVI após análise satelital: %s
                Percentual de perda estimado: %s%%
                Confiança do modelo de IA: %s%%
                Descrição do produtor: %s

                Gere um laudo técnico para este sinistro.
                """.formatted(
                evento.claimNumber(),
                evento.policyId(),
                evento.plotId(),
                evento.ndviBefore() != null ? evento.ndviBefore().toPlainString() : "não informado",
                ndviAfter != null ? ndviAfter.toPlainString() : "em análise",
                totalLossPct != null ? totalLossPct.toPlainString() : "calculando",
                mlConfidenceScore != null ? mlConfidenceScore.toPlainString() : "calculando",
                evento.description() != null ? evento.description() : "sem descrição"
        );

        try {
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.warn("Ollama indisponível para laudo do sinistro {} — usando fallback. Causa: {}",
                    evento.claimNumber(), e.getMessage());
            return "Laudo gerado automaticamente pelo sistema Zenith Harvest. "
                    + "Sinistro " + evento.claimNumber() + " registrado e em processamento. "
                    + "Análise NDVI concluída com percentual de perda estimado em "
                    + (totalLossPct != null ? totalLossPct.toPlainString() : "0") + "%.";
        }
    }
}
