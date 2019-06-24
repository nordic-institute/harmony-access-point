package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface PayloadFileStorageFactory {

    PayloadFileStorage create(Domain domain);

}
