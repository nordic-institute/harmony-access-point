package eu.domibus.core.property.listeners;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Configuration
public class PropertyListenersConfiguration {
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }
}
