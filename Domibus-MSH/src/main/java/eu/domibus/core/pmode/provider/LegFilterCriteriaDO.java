package eu.domibus.core.pmode.provider;

import eu.domibus.common.model.configuration.Role;

/**
 * @author Arun Raj
 * @since 4.2
 */
public class LegFilterCriteriaDO {
    private final String agreementName;
    private final String senderParty;
    private final String receiverParty;
    private final Role initiatorRole;
    private final Role responderRole;
    private final String service;
    private final String action;

    public LegFilterCriteriaDO(String agreementName, String senderParty, String receiverParty, Role initiatorRole, Role responderRole, String service, String action) {
        this.agreementName = agreementName;
        this.senderParty = senderParty;
        this.receiverParty = receiverParty;
        this.initiatorRole = initiatorRole;
        this.responderRole = responderRole;
        this.service = service;
        this.action = action;
    }

    public String getAgreementName() {
        return agreementName;
    }

    public String getSenderParty() {
        return senderParty;
    }

    public String getReceiverParty() {
        return receiverParty;
    }

    public Role getInitiatorRole() {
        return initiatorRole;
    }

    public Role getResponderRole() {
        return responderRole;
    }

    public String getService() {
        return service;
    }

    public String getAction() {
        return action;
    }
}
