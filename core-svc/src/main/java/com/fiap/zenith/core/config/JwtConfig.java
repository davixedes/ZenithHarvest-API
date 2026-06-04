package com.fiap.zenith.core.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * JWT <strong>assimétrico (RS256)</strong>. O core-svc é o único que detém a chave privada e
 * assina os tokens; a chave pública é publicada via JWKS (ver {@code JwksController}). Gateway e
 * analise-svc validam apenas com a pública (jwk-set-uri) — <strong>sem segredo compartilhado</strong>,
 * mantendo os serviços desacoplados.
 *
 * <p>O par RSA é gerado em memória no startup (sem material de chave em config/arquivo). Trade-off:
 * tokens emitidos antes de um restart do core deixam de valer (kid novo). Para persistir entre
 * restarts, trocar este bean por uma chave carregada de um keystore/PEM via env.</p>
 */
@Configuration
public class JwtConfig {

    @Bean
    public RSAKey rsaKey() {
        try {
            return new RSAKeyGenerator(2048)
                    .keyIDFromThumbprint(true)
                    .generate();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar o par de chaves RSA do JWT", e);
        }
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) {
        try {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao construir o JwtDecoder a partir da chave pública", e);
        }
    }
}
