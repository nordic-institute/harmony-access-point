package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_FILE_UPLOAD_MAX_SIZE;

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
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        //Global DOMIBUS_FILE_UPLOAD_MAX_SIZE property value is used here
        int size = domibusPropertyProvider.getIntegerProperty(DOMIBUS_FILE_UPLOAD_MAX_SIZE);
        resolver.setMaxUploadSize(size);
        return resolver;
    }

}
