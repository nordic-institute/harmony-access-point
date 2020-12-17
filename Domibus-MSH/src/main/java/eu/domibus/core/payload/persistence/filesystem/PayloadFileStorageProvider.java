package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface PayloadFileStorageProvider {

    PayloadFileStorage forDomain(Domain domain) ;

    PayloadFileStorage getCurrentStorage();

    boolean isPayloadsPersistenceInDatabaseConfigured();

    boolean isPayloadsPersistenceFileSystemConfigured();
}
