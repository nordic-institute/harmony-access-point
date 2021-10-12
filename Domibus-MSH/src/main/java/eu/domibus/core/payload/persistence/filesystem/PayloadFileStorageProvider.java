package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.jms.DomainsAware;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface PayloadFileStorageProvider extends DomainsAware {

    PayloadFileStorage forDomain(Domain domain) ;

    PayloadFileStorage getCurrentStorage();

    boolean isPayloadsPersistenceInDatabaseConfigured();

    boolean isPayloadsPersistenceFileSystemConfigured();
}
