package eu.domibus.common.model.configuration;

import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

public class PartyIdTypeTest {

    @Tested
    PartyIdType partyIdType;

    @Test
    public void equals() {
        partyIdType = new PartyIdType();
        partyIdType.setName("partyTypeUrn");


        PartyIdType partyIdType1 = new PartyIdType();
        partyIdType1.setName("PARTYTYPEURN");

        Assert.assertFalse(partyIdType.equals(null));
        Assert.assertTrue(partyIdType.equals(partyIdType1));
        Assert.assertTrue(partyIdType1.equals(partyIdType));
        Assert.assertTrue(partyIdType.equals(partyIdType));
        partyIdType.setName(null);
        Assert.assertFalse(partyIdType.equals(partyIdType1));
    }

}
