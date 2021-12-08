package eu.domibus.core.party;

import eu.domibus.common.model.configuration.Party;

public interface PartyEndpointProvider {

    String getReceiverPartyEndpoint(Party receiverParty, String finalRecipient);

    void setReceiverPartyEndpoint(String finalRecipient, String finalRecipientAPUrl);
}
