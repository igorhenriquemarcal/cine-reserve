package br.com.cinereserve.repository.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utilitário central para configuração do ObjectMapper do Jackson.
 * Configura suporte a datas Java 8+ (JavaTimeModule) e formatação legível.
 */
public final class JsonMapperConfig {

    private static final ObjectMapper MAPPER = criarMapper();

    private JsonMapperConfig() {
    }

    private static ObjectMapper criarMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
