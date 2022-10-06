package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class LatestOutgoingMessageRequestRO implements Serializable {
    private String partyId;

    private String senderPartyId;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getSenderPartyId() {
        return senderPartyId;
    }

    public void setSenderPartyId(String senderPartyId) {
        this.senderPartyId = senderPartyId;
    }
}
