package eu.domibus.web.rest.ro;

import eu.domibus.web.rest.validators.PropsNotBlacklisted;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@PropsNotBlacklisted
public class LatestOutgoingMessageRequestRO implements Serializable {
    private String partyId;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }
}
