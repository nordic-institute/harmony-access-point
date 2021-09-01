package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Configuration
public class EArchiveFileStorageConfiguration {

    @Bean(name = "eArchiveStorage")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public EArchiveFileStorage storage(Domain domain) {
        return new EArchiveFileStorage(domain);
    }

}
