package eu.domibus.common.model.configuration;

import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
public class IdentifierTest {

    @Tested
    Identifier identifier;

    @Test
    public void equals() {
        identifier = new Identifier();
        identifier.setPartyId("domibus-blue");


        Identifier identifier1 = new Identifier();
        identifier1.setPartyId("domibus-BLUE");

        Assert.assertFalse(identifier.equals(null));
        Assert.assertTrue(identifier.equals(identifier));

        Assert.assertTrue(identifier.equals(identifier1));
        Assert.assertTrue(identifier1.equals(identifier));

        identifier.setPartyId(null);
        Assert.assertFalse(identifier.equals(identifier1));
    }

    @Test
    public void equalsPartyIdType(@Injectable PartyIdType partyIdType) {
        identifier = new Identifier();
        identifier.setPartyId("domibus-blue");
        partyIdType.setName("partyTypeUrn");
        identifier.setPartyIdType(partyIdType);


        Identifier identifier1 = new Identifier();
        identifier1.setPartyId("domibus-BLUE");

        Assert.assertFalse(identifier.equals(identifier1));
    }

}