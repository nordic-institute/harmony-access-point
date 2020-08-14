package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_FILE_UPLOAD_MAX_SIZE;

@Configuration
public class PropertyListenersConfiguration {

    protected DomibusPropertyProvider domibusPropertyProvider;

    public PropertyListenersConfiguration(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        int size = domibusPropertyProvider.getIntegerProperty(DOMIBUS_FILE_UPLOAD_MAX_SIZE);
        resolver.setMaxUploadSize(size);
        return resolver;
    }


}
