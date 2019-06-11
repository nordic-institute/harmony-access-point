package eu.domibus.core.payload.filesystem;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadFileStorageConfiguration.class);

    @Bean(name = "storage")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public PayloadFileStorage storage(Domain domain) {

        PayloadFileStorage storage = new PayloadFileStorage();
        storage.setDomain(domain);

        return  storage;
    }

}
