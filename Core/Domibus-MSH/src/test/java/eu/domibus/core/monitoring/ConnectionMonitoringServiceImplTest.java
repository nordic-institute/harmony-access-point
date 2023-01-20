package eu.domibus.core.monitoring;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.web.rest.ro.ConnectionMonitorRO;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
@RunWith(JMockit.class)
public class ConnectionMonitoringServiceImplTest {

    @Tested
    ConnectionMonitoringServiceImpl connectionMonitoringService;

    @Injectable
    PartyService partyService;

    @Injectable
    TestService testService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    ConnectionMonitoringHelper connectionMonitoringHelper;

    @Injectable
    MetricRegistry metricRegistry;

    @Test
    public void sendTestMessages_NotApplicable() throws IOException, MessagingProcessingException {
        String selfParty = "self";
        String partyId2 = "partyId2";

        connectionMonitoringService.sendTestMessages();

        new Verifications() {{
            testService.submitTest(selfParty, partyId2);
            times = 0;
        }};
    }

    @Test
    public void sendTestMessages_NotEnabled() throws IOException, MessagingProcessingException {
        String selfParty = "self";
        String partyId2 = "partyId2";

        new Expectations(connectionMonitoringService) {{
            connectionMonitoringService.isMonitoringEnabled();
            result = true;

            partyService.findPushToPartyNamesForTest();
            result = Arrays.asList(selfParty, partyId2);
        }};

        connectionMonitoringService.sendTestMessages();

        new Verifications() {{
            testService.submitTest(selfParty, partyId2);
            times = 0;
        }};
    }

    @Test
    public void testGetConnectionStatus() {
        // Given
        String senderPartyId = "senderPartyId";
        String partyId1 = "partyId1";
        String partyId2 = "partyId2";
        String enabledPair = "senderPartyId>partyId1";
        List<String> partyIds = Arrays.asList(partyId1, partyId2);

        TestServiceMessageInfoRO lastSent1 = new TestServiceMessageInfoRO() {{
            setMessageStatus(MessageStatus.ACKNOWLEDGED);
        }};
        TestServiceMessageInfoRO lastReceived1 = new TestServiceMessageInfoRO() {{
            setMessageStatus(MessageStatus.ACKNOWLEDGED);
        }};

        TestServiceMessageInfoRO lastSent2 = new TestServiceMessageInfoRO() {{
            setMessageStatus(MessageStatus.SEND_FAILURE);
        }};
        TestServiceMessageInfoRO lastReceived2 = new TestServiceMessageInfoRO() {{
            setMessageStatus(MessageStatus.ACKNOWLEDGED);
        }};

        new Expectations() {{

            testService.getLastTestSent(senderPartyId, partyId1);
            result = lastSent1;

            testService.getLastTestReceived(senderPartyId, partyId1, null);
            result = lastReceived1;

            testService.getLastTestSent(senderPartyId, partyId2);
            result = lastSent2;

            testService.getLastTestReceived(senderPartyId, partyId2, null);
            result = lastReceived2;

            partyService.findPushToPartyNamesForTest();
            result = Arrays.asList(partyId1, partyId2);

            connectionMonitoringHelper.getMonitorEnabledParties();
            result = Arrays.asList(enabledPair);

        }};

        // When
        Map<String, ConnectionMonitorRO> result = connectionMonitoringService.getConnectionStatus(senderPartyId, partyIds);

        // Then
        Assert.assertEquals(result.size(), 2);

        Assert.assertEquals(result.get(partyId1).isTestable(), true);
        Assert.assertEquals(result.get(partyId1).isMonitored(), true);
        Assert.assertEquals(result.get(partyId1).getStatus(), ConnectionMonitorRO.ConnectionStatus.OK);

        Assert.assertEquals(result.get(partyId2).isTestable(), true);
        Assert.assertEquals(result.get(partyId2).isMonitored(), false);
        Assert.assertEquals(result.get(partyId2).getStatus(), ConnectionMonitorRO.ConnectionStatus.BROKEN);

    }

}
