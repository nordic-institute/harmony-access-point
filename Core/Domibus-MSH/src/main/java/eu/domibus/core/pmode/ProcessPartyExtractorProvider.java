package eu.domibus.core.pmode;

import eu.domibus.api.ebms3.MessageExchangePattern;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Component
public class ProcessPartyExtractorProvider {

    public ProcessTypePartyExtractor getProcessTypePartyExtractor(String processType, final String senderParty, final String receiverParty) {
        ProcessTypePartyExtractor processTypePartyExtractor = new PushProcessPartyExtractor(senderParty, receiverParty);
        if (MessageExchangePattern.ONE_WAY_PULL.getUri().equalsIgnoreCase(processType)) {
            processTypePartyExtractor = new PullProcessPartyExtractor(senderParty, receiverParty);
        }
        return processTypePartyExtractor;
    }
}
