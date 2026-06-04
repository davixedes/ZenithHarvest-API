package com.fiap.zenith.analise_svc.application.service;

import com.fiap.zenith.analise_svc.infra.messaging.SinistroAbertoEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaudoServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Test
    void gerarLaudoDeveRetornarRespostaDoChatClient() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Laudo sintetizado");

        LaudoService service = new LaudoService(chatClientBuilder);

        String laudo = service.gerarLaudo(evento(), new BigDecimal("0.511"), new BigDecimal("24.30"),
                new BigDecimal("92.10"));

        assertThat(laudo).isEqualTo("Laudo sintetizado");
    }

    @Test
    void gerarLaudoDeveUsarFallbackQuandoOllamaFalhar() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt().user(anyString()).call().content()).thenThrow(new RuntimeException("offline"));

        LaudoService service = new LaudoService(chatClientBuilder);

        String laudo = service.gerarLaudo(evento(), new BigDecimal("0.511"), new BigDecimal("24.30"),
                new BigDecimal("92.10"));

        assertThat(laudo)
                .contains("Zenith Harvest")
                .contains("CLM-2026-0001")
                .contains("24.30%");
    }

    private SinistroAbertoEvent evento() {
        return new SinistroAbertoEvent(
                UUID.randomUUID(),
                "CLM-2026-0001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                2,
                new BigDecimal("0.733"),
                new BigDecimal("-22.9100"),
                new BigDecimal("-47.0650"),
                "Queda brusca de vigor vegetativo",
                new BigDecimal("150000.00"),
                new BigDecimal("80000.00")
        );
    }
}
