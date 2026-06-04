package com.fiap.zenith.analise_svc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Chatbot de suporte ao produtor rural usando Spring AI (Ollama/llama3.2).
 * Endpoint público para o app mobile consumir sem autenticação.
 */
@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "Assistente IA para suporte ao produtor — Spring AI")
public class ChatbotController {

    private static final String SYSTEM_PROMPT = """
            Você é o assistente virtual da Zenith Harvest, plataforma de seguro paramétrico agrícola.
            Responda perguntas sobre: apólices de seguro, abertura de sinistros, análise satelital
            de NDVI, cobertura de culturas, prazo de pagamento de indenizações via PIX.
            Use linguagem simples e acessível ao produtor rural brasileiro.
            Se não souber a resposta, oriente o usuário a contatar o suporte.
            Responda em português brasileiro.
            """;

    private final ChatClient chatClient;

    public ChatbotController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public record ChatRequest(@NotBlank @Size(max = 1000) String mensagem) {}
    public record ChatResponse(String resposta) {}

    @PostMapping
    @Operation(summary = "Envia mensagem ao assistente IA (Spring AI / Ollama)")
    public ChatResponse chat(@Valid @RequestBody ChatRequest req) {
        String resposta = chatClient.prompt()
                .user(req.mensagem())
                .call()
                .content();
        return new ChatResponse(resposta);
    }
}
