package eu.domibus.core.message;


import eu.domibus.common.MSHRole;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.Messaging;


/**
 * @author Ioana Dragusanu
 * @since 3.3
 */
public interface MessagingService {

    void storeMessage(Messaging messaging, MSHRole mshRole, final LegConfiguration legConfiguration, String backendName) throws CompressionException;

    void storePayloads(Messaging messaging, MSHRole mshRole, LegConfiguration legConfiguration, String backendName);

}
