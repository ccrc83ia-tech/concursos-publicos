package com.dian.prep.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expone un {@link ObjectMapper} como bean para que los adaptadores que
 * serializan/deserializan JSON (p. ej. persistencia JSONB y parseo de la IA)
 * puedan inyectarlo. {@code @ConditionalOnMissingBean} evita duplicarlo si la
 * autoconfiguración de Spring ya lo provee.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
