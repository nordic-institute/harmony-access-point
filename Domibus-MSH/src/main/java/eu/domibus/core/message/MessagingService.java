package eu.domibus.core.message;


import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.api.model.Messaging;

import java.util.List;


/**
 * @author Ioana Dragusanu
 * @since 3.3
 */
public interface MessagingService {

    void storeMessagePayloads(UserMessage userMessage, List<PartInfo> partInfoList, MSHRole mshRole, final LegConfiguration legConfiguration, String backendName) throws CompressionException;

    void saveUserMessageAndPayloads(UserMessage userMessage, List<PartInfo> partInfoList);

    void storePayloads(UserMessage userMessage, List<PartInfo> partInfoList, MSHRole mshRole, LegConfiguration legConfiguration, String backendName);

}
