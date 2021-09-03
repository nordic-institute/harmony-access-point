package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface EArchiveFileStorageFactory {

    EArchiveFileStorage create(Domain domain);

}
