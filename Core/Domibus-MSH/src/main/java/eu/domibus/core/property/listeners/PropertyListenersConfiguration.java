package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Configuration
public class PropertyListenersConfiguration {

    protected DomibusPropertyProvider domibusPropertyProvider;

    public PropertyListenersConfiguration(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }
}
