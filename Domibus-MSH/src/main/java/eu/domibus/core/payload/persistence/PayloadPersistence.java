package eu.domibus.core.payload.persistence;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;

import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface PayloadPersistence {

    int DEFAULT_BUFFER_SIZE = 32 * 1024;

    void storeIncomingPayload(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration) throws IOException;

    void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, final LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception;
}
