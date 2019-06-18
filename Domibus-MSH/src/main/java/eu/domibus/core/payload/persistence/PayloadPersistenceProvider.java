package eu.domibus.core.payload.persistence;

import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PayloadPersistenceProvider {

    PayloadPersistence getPayloadPersistence(PartInfo partInfo, UserMessage userMessage);
}
