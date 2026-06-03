package com.fiap.zenith.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados do Swagger/OpenAPI (SpringDoc). UI disponível em {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI coreOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Zenith Harvest — core-svc API")
                .description("CRUD do domínio, autenticação e financeiro do seguro paramétrico agrícola.")
                .version("v0.0.1")
                .contact(new Contact().name("Grupo Zenith — FIAP"))
                .license(new License().name("Uso acadêmico — Global Solution FIAP")));
    }
}
