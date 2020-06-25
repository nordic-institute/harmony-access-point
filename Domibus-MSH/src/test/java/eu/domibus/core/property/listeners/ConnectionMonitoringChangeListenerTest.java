package eu.domibus.core.property.listeners;

import com.google.gson.Gson;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class ConnectionMonitoringChangeListenerTest {

    @Tested
    protected ConnectionMonitoringChangeListener listener = new ConnectionMonitoringChangeListener();

    @Injectable
    protected PModeProvider pModeProvider;

    String jsonParty1 = "{name:'blue_gw', identifiers:[{partyId:'domibus-blue',partyIdType:{name:'partyTypeUrn'}}, {partyId:'domibus-bluish',partyIdType:{name:'partyTypeUrn2'}}]}";
    String jsonParty2 = "{name:'red_gw', identifiers:[{partyId:'domibus-red',partyIdType:{name:'partyTypeUrn'}}]}";
    String jsonParty3 = "{name:'green_gw', identifiers:[{partyId:'domibus-green',partyIdType:{name:'partyTypeUrn'}}]}";

    @Test
    public void propertyValueChanged() {
        Gson gson = new Gson();
        Party party1 = gson.fromJson(jsonParty1, Party.class);
        Party party2 = gson.fromJson(jsonParty2, Party.class);
        Party party3 = gson.fromJson(jsonParty3, Party.class);

        List<Party> knownParties = Arrays.asList(party1, party2, party3);
        List<String> testablePartyIds = Arrays.asList("domibus-blue", "domibus-red");

        new Expectations() {{
            pModeProvider.findAllParties();
            result = knownParties;
            pModeProvider.findPartyIdByServiceAndAction(anyString, anyString, null);
            result = testablePartyIds;
        }};

        List<String> valuesThatShouldFail = Arrays.asList(
                "#$%%$^&",
                "domibus-unknowncolor,domibus-blue",
                "domibus-blue,domibus-red,domibus-green",
                "domibus-bluish"
        );
        List<String> valuesThatShouldPass = Arrays.asList(
                "domibus-red, domibus-blue",
                "domibus-blue, domibus-blue",
                " ,, domibus-blue, ",
                "Domibus-BLUE",
                "",
                null
        );

        for (String value : valuesThatShouldFail) {
            try {
                listener.propertyValueChanged("default", DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED, value);
                Assert.fail("[" + value + "] property value shouldn't have been accepted");
            } catch (DomibusPropertyException ex) {
            }
        }

        for (String value : valuesThatShouldPass) {
            try {
                listener.propertyValueChanged("default", DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED, value);
            } catch (DomibusPropertyException ex) {
                Assert.fail("[" + value + "] property value should have been accepted");
            }
        }
    }
}
