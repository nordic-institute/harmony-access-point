package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Party Identifier class for external API
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class PartyIdentifierDTO {

    private String partyId;

    private PartyIdentifierTypeDTO partyIdType;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public PartyIdentifierTypeDTO getPartyIdType() {
        return partyIdType;
    }

    public void setPartyIdType(PartyIdentifierTypeDTO partyIdType) {
        this.partyIdType = partyIdType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("partyId", partyId)
                .append("partyIdType", partyIdType)
                .toString();
    }
}
