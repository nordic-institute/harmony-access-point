package eu.domibus.core.payload.temp;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public interface TemporaryPayloadService {

    void cleanTemporaryPayloads(Domain domain);
}
