package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class PayloadFileStorageFactoryImpl implements PayloadFileStorageFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadFileStorageFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public PayloadFileStorage create(Domain domain) {
        LOG.debug("Creating the StorageFactory for domain [{}]", domain);
        return (PayloadFileStorage)applicationContext.getBean("storage", domain);
    }

}
