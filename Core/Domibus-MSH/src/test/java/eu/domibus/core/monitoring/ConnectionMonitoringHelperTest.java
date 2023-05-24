package eu.domibus.core.monitoring;

import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.pmode.provider.PModeProvider;
import junit.framework.TestCase;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@RunWith(JMockit.class)
public class ConnectionMonitoringHelperTest extends TestCase {

    @Tested
    ConnectionMonitoringHelper connectionMonitoringHelper;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PartyService partyService;

    @Injectable
    PModeProvider pModeProvider;

    @Test
    public void transformToNewFormatTest() {
        String selfParty = "self";
        String partyId1 = "partyId1";
        String partyId2 = "partyId2";
        String enabledPair = "self>partyId1,self>partyId2";
        List<String> senderPartyIds = Arrays.asList(selfParty);
        List<String> destinationPartyIds = Arrays.asList(partyId1, partyId2);

        new Expectations() {{
            partyService.getGatewayPartyIdentifier();
            result = selfParty;

            partyService.getGatewayPartyIdentifiers();
            result = senderPartyIds;

            partyService.findPushToPartyNamesForTest();
            result = destinationPartyIds;
        }};

        String res = connectionMonitoringHelper.fixParties(Arrays.asList(partyId1, partyId2));
        Assert.assertEquals(enabledPair, res);
    }
}
