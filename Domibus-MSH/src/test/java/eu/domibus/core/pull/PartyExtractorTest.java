package eu.domibus.core.pull;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.PartyIdType;
import eu.domibus.ebms3.common.model.PartyId;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PartyExtractorTest {

    @Test
    public void getSamePartyId() {
        //pmode configuration
        Party party = new Party();
        Identifier e = new Identifier();
        e.setPartyId("domibus_red");
        PartyIdType partyIdType = new PartyIdType();
        partyIdType.setName("partyTypeUrn");
        partyIdType.setValue("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        e.setPartyIdType(partyIdType);
        party.getIdentifiers().add(e);

        //message configuration
        Collection<PartyId> partyIds = new ArrayList<>();
        PartyId messagePartyId = new PartyId();
        messagePartyId.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        messagePartyId.setValue("domibus_red");
        partyIds.add(messagePartyId);
        assertEquals("domibus_red", new PartyExtractor(party, partyIds).getPartyId());
    }

    @Test
    public void getSamePartyIdDifferentCase() {
        //pmode configuration
        Party party = new Party();
        Identifier e = new Identifier();
        e.setPartyId("domibus_red");
        PartyIdType partyIdType = new PartyIdType();
        partyIdType.setName("partyTypeUrn");
        partyIdType.setValue("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        e.setPartyIdType(partyIdType);
        party.getIdentifiers().add(e);

        //message configuration
        Collection<PartyId> partyIds = new ArrayList<>();
        PartyId messagePartyId = new PartyId();
        messagePartyId.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        messagePartyId.setValue("domibus_Red");
        partyIds.add(messagePartyId);
        assertEquals("domibus_red", new PartyExtractor(party, partyIds).getPartyId());
    }

    @Test
    public void getSamePartyIdMultiplePmodeIdentifier() {
        //pmode configuration
        Party party = new Party();
        Identifier e = new Identifier();
        e.setPartyId("domibus_green");
        PartyIdType partyIdType = new PartyIdType();
        partyIdType.setName("partyTypeUrn");
        partyIdType.setValue("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        e.setPartyIdType(partyIdType);
        party.getIdentifiers().add(e);

        //message configuration
        Collection<PartyId> partyIds = new ArrayList<>();
        PartyId messagePartyId = new PartyId();
        messagePartyId.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        messagePartyId.setValue("domibus_Red");
        partyIds.add(messagePartyId);
        messagePartyId = new PartyId();
        messagePartyId.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        messagePartyId.setValue("domibus_grEen");
        partyIds.add(messagePartyId);
        assertEquals("domibus_green", new PartyExtractor(party, partyIds).getPartyId());
    }
}
