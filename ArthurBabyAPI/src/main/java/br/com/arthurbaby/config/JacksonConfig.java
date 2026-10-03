package br.com.arthurbaby.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Padroniza como datas/horas saem no JSON de toda a API.
 *
 * <p>Por padrão o Jackson escreve {@code LocalDateTime} com toda a precisão do relógio do Java
 * (até 7-9 dígitos na fração de segundo, ex.: {@code 2026-09-23T11:02:00.9942499}). O app Android
 * faz o parse com {@code SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")}, que só entende 3 dígitos
 * (milissegundos). Aqui toda saída de {@code LocalDateTime} passa a ter sempre exatamente 3 dígitos,
 * ex.: {@code 2026-09-23T11:02:00.994}.</p>
 *
 * <p>Obs.: {@code spring.jackson.date-format} não resolve isso porque só vale para {@code java.util.Date};
 * os campos da API são {@code java.time.LocalDateTime}, que precisam de um serializer próprio.
 * A leitura (requisições) continua aceitando qualquer formato ISO-8601, então nada quebra na entrada.</p>
 */
@Configuration
public class JacksonConfig {
    /** Formato único de data/hora devolvido pela API (milissegundos com 3 dígitos fixos). */
    public static final String FORMATO_DATA_HORA = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    @Bean
    Jackson2ObjectMapperBuilderCustomizer formatoDatas() {
        // O customizer é aplicado ao ObjectMapper do Spring, usado tanto nas respostas HTTP
        // quanto no AdminCrudService (que converte entidades em Map), então o formato vale em todo lugar.
        return builder -> builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(FORMATO_DATA_HORA)));
    }
}
