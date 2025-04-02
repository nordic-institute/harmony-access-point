package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.core.spi.payload.DeleteFolderResult;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface PayloadFileStorageProvider extends DomainsAware {

    void initialize();

    PayloadFileStorage forDomain(Domain domain) ;

    PayloadFileStorage getCurrentStorage();

    boolean isPayloadsPersistenceInDatabaseConfigured();

    boolean isPayloadsPersistenceFileSystemConfigured();

    DeleteFolderResult deleteFolder(String currentDomain, String absoluteFolderLocation);
}
