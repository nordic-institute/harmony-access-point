package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveFileStorageFactoryImpl implements EArchiveFileStorageFactory {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorageFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public EArchiveFileStorage create(Domain domain) {
        LOG.debug("Creating the eArchiveStorageFactory for domain [{}]", domain);
        return (EArchiveFileStorage)applicationContext.getBean("eArchiveStorage", domain);
    }

}
