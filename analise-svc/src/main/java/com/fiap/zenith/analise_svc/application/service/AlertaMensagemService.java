package com.fiap.zenith.analise_svc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Gera a mensagem do alerta preventivo via Spring AI (Ollama).
 * Fallback textual garante que o alerta é sempre criado mesmo sem IA disponível.
 */
@Service
public class AlertaMensagemService {

    private static final Logger log = LoggerFactory.getLogger(AlertaMensagemService.class);

    private static final String SYSTEM_PROMPT = """
            Você é um agrônomo especialista em seguros paramétricos da Zenith Harvest.
            Sua função é alertar produtores rurais sobre quedas de NDVI detectadas por satélite.
            Use linguagem direta e prática. Máximo de 2 parágrafos.
            Sempre inclua uma recomendação de ação preventiva concreta.
            """;

    private final ChatClient chatClient;

    public AlertaMensagemService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public String gerarMensagem(String cropName, BigDecimal observedNdvi,
                                 BigDecimal expectedNdviMin, BigDecimal dropPct,
                                 String severidadeDescricao) {
        String prompt = """
                Cultura: %s
                NDVI observado: %s
                NDVI mínimo esperado: %s
                Queda em relação ao esperado: %s%%
                Severidade: %s

                Gere um alerta preventivo para este produtor com recomendação de ação.
                """.formatted(
                cropName,
                fmt(observedNdvi),
                fmt(expectedNdviMin),
                dropPct != null ? dropPct.setScale(1, RoundingMode.HALF_UP).toPlainString() : "?",
                severidadeDescricao);

        try {
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.warn("Ollama indisponível para alerta preventivo ({}) — usando fallback. Causa: {}",
                    cropName, e.getMessage());
            return gerarFallback(cropName, observedNdvi, expectedNdviMin, dropPct, severidadeDescricao);
        }
    }

    private String gerarFallback(String cropName, BigDecimal observedNdvi,
                                   BigDecimal expectedNdviMin, BigDecimal dropPct,
                                   String severidadeDescricao) {
        return "Alerta %s detectado na lavoura de %s. ".formatted(severidadeDescricao, cropName)
                + "NDVI atual: %s (esperado mínimo: %s — queda de %s%%). ".formatted(
                        fmt(observedNdvi), fmt(expectedNdviMin),
                        dropPct != null ? dropPct.setScale(1, RoundingMode.HALF_UP).toPlainString() : "?")
                + "Verifique a lavoura e, se necessário, abra um sinistro pelo aplicativo.";
    }

    private String fmt(BigDecimal v) {
        return v != null ? v.toPlainString() : "não disponível";
    }
}
