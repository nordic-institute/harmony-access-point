package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.FileSystemUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class EArchiveFileStorageConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public EArchiveFileStorage eArchiveFileStorage(Domain domain, DomibusPropertyProvider domibusPropertyProvider, FileSystemUtil fileSystemUtil) {
        return new EArchiveFileStorage(domain, domibusPropertyProvider, fileSystemUtil);
    }

}
