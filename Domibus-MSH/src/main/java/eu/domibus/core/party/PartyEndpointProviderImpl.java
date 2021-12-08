package eu.domibus.core.party;

import eu.domibus.common.model.configuration.Party;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PartyEndpointProviderImpl implements PartyEndpointProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyEndpointProviderImpl.class);

    protected Map<String, String> finalRecipientAccessPointUrls = new HashMap<>();

    @Override
    public String getReceiverPartyEndpoint(Party receiverParty, String finalRecipient) {
        final String finalRecipientAPUrl = finalRecipientAccessPointUrls.get(finalRecipient);
        if (StringUtils.isNotBlank(finalRecipientAPUrl)) {
            LOG.debug("Determined endpoint URL [{}] for party [{}] and final recipient", finalRecipientAPUrl, receiverParty.getName(), finalRecipient);
            return finalRecipientAPUrl;
        }

        final String receiverPartyEndpoint = receiverParty.getEndpoint();
        LOG.debug("Determined endpoint URL [{}] for party [{}]", receiverPartyEndpoint, receiverParty.getName());
        return receiverPartyEndpoint;
    }

    @Override
    public synchronized void setReceiverPartyEndpoint(String finalRecipient, String finalRecipientAPUrl) {
        LOG.debug("Setting the endpoint URL to [{}] for final recipient [{}]", finalRecipientAPUrl, finalRecipient);
        finalRecipientAccessPointUrls.put(finalRecipient, finalRecipientAPUrl);
    }
}
