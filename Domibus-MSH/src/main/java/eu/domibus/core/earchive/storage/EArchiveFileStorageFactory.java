package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface EArchiveFileStorageFactory {

    EArchiveFileStorage create(Domain domain);

}
