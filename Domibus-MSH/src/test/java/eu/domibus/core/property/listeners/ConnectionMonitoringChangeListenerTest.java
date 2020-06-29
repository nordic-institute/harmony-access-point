package eu.domibus.core.property.listeners;

import com.google.gson.Gson;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(Parameterized.class)
public class ConnectionMonitoringChangeListenerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringChangeListenerTest.class);

    @Injectable
    protected PModeProvider pModeProvider;

    @Tested
    protected ConnectionMonitoringChangeListener listener = new ConnectionMonitoringChangeListener(pModeProvider);

    @Before
    public void setupTest() {
        Gson gson = new Gson();

        String jsonParty1 = "{name:'blue_gw', identifiers:[{partyId:'domibus-blue',partyIdType:{name:'partyTypeUrn'}}, {partyId:'domibus-bluish',partyIdType:{name:'partyTypeUrn2'}}]}";
        String jsonParty2 = "{name:'red_gw', identifiers:[{partyId:'domibus-red',partyIdType:{name:'partyTypeUrn'}}]}";
        String jsonParty3 = "{name:'green_gw', identifiers:[{partyId:'domibus-green',partyIdType:{name:'partyTypeUrn'}}]}";

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
    }

    @Parameterized.Parameter(0)
    public String value;

    @Parameterized.Parameter(1)
    public boolean valid;

    @Parameterized.Parameters(name = "Test setting propertyValue=\"{0}\"")
    public static Collection<Object[]> testedValues() {
        return Arrays.asList(new Object[][]{
                // invalid values :
                {"#$%%$^&", false},
                {"<foo val=“bar” />", false},
                {"１２３", false},
                {"domibus-unknowncolor,domibus-blue", false},
                {"domibus-blue,domibus-red,domibus-green", false},
                {"domibus-bluish", false},
                // valid values :
                {"domibus-red, domibus-blue", true},
                {"domibus-blue, domibus-blue", true},
                {" ,, domibus-blue  , ", true},
                {"Domibus-BLUE", true},
                {"", true},
                {null, true},
        });
    }

    @Test
    public void propertyValueChanged() {
        try {
            listener.propertyValueChanged("default", DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED, value);

            if (valid == false) {
                Assert.fail("[" + value + "] property value shouldn't have been accepted");
            }
        } catch (DomibusPropertyException ex) {
            if (valid == false) {
                LOG.info("Exception thrown as expected when trying to set invalid property value: [{}]", value, ex);
            } else {
                LOG.error("Unexpected exception thrown when trying to set valid property value: [{}]", value, ex);
                Assert.fail("[" + value + "] property value should have been accepted");
            }
        }
    }
}
