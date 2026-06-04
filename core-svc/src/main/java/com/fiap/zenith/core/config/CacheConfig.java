package com.fiap.zenith.core.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache em memória para lookups de baixa volatilidade.
 * ConcurrentMapCacheManager não expira — o cache se invalida ao reiniciar o serviço,
 * que é aceitável para dados que mudam raramente (biomas, situações, categorias).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final String[] LOOKUP_CACHES = {
            "biomes", "production-systems", "plot-situations",
            "insurer-situations", "insurance-situations", "quote-situations",
            "policy-situations", "claim-situations", "claim-event-types",
            "claim-categories", "claim-subcategories", "rejection-reasons",
            "satellite-sources", "satellite-classes",
            "alert-types", "alert-severities", "alert-situations",
            "payment-types", "payment-situations",
            "crops", "insurances"
    };

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(LOOKUP_CACHES);
    }
}
