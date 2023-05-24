package eu.domibus.core.payload.persistence.filesystem;

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
public class PayloadFileStorageConfiguration {

    @Bean(name = "storage")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public PayloadFileStorage storage(Domain domain) {
        PayloadFileStorage storage = new PayloadFileStorage(domain);
        return  storage;
    }

}
