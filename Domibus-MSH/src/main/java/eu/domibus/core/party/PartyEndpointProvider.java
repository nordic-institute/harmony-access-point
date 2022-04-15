package eu.domibus.core.party;

import eu.domibus.common.model.configuration.Party;

/**
 * @since 4.2.6
 * @author Cosmin Baciu
 */
public interface PartyEndpointProvider {

    String getReceiverPartyEndpoint(Party receiverParty, String finalRecipient);

    void setReceiverPartyEndpoint(String finalRecipient, String finalRecipientAPUrl);
}
