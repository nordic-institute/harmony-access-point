package eu.domibus.web.rest.ro;

import eu.domibus.web.rest.validators.PropsNotBlacklisted;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@PropsNotBlacklisted
public class LatestIncomingMessageRequestRO implements Serializable {
    private String partyId;
    private String userMessageId;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getUserMessageId() {
        return userMessageId;
    }

    public void setUserMessageId(String userMessageId) {
        this.userMessageId = userMessageId;
    }
}
