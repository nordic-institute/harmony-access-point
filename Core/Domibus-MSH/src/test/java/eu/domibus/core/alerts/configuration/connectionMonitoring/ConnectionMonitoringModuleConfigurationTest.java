package eu.domibus.core.alerts.configuration.connectionMonitoring;

import eu.domibus.api.model.MessageStatus;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static eu.domibus.core.monitoring.ConnectionMonitoringHelper.ALL_PARTIES;

@RunWith(JMockit.class)
public class ConnectionMonitoringModuleConfigurationTest {

    @Test
    public void shouldGenerateAlertTest() {
        String partyA = "partyA";
        String partyB = "partyB";
        String partyC = "partyC";

        ConnectionMonitoringModuleConfiguration configuration = new ConnectionMonitoringModuleConfiguration();
        configuration.setEnabledParties(Arrays.asList(partyA + ">" + partyB, partyA + ">" + partyC));
        configuration.setActive(true);

        new Verifications() {{
            Assert.assertEquals(true, configuration.shouldGenerateAlert(MessageStatus.SEND_FAILURE, partyA, partyB));
            Assert.assertEquals(false, configuration.shouldGenerateAlert(MessageStatus.SEND_FAILURE, partyB, partyC));
            Assert.assertEquals(false, configuration.shouldGenerateAlert(MessageStatus.ACKNOWLEDGED, partyA, partyB));
        }};

        configuration.setEnabledParties(Arrays.asList(ALL_PARTIES));

        new Verifications() {{
            Assert.assertEquals(true, configuration.shouldGenerateAlert(MessageStatus.SEND_FAILURE, partyA, partyB));
            Assert.assertEquals(true, configuration.shouldGenerateAlert(MessageStatus.SEND_FAILURE, partyB, partyC));
        }};
    }
}
