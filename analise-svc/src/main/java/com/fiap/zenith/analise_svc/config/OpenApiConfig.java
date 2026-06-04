package com.fiap.zenith.analise_svc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI analiseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zenith Harvest — analise-svc API")
                        .description("Visão computacional (NDVI/satélite), IA generativa (Spring AI/Ollama), chatbot e histórico de análises.")
                        .version("v0.0.1")
                        .contact(new Contact().name("Grupo Zenith — FIAP"))
                        .license(new License().name("Uso acadêmico — Global Solution FIAP")));
    }
}
