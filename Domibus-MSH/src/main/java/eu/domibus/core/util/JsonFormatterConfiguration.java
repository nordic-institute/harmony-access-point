package eu.domibus.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class JsonFormatterConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper bean = new ObjectMapper();
        bean.registerModule(new JavaTimeModule());
        bean.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return bean;
    }
}
