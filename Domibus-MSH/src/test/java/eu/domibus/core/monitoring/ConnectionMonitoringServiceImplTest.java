package eu.domibus.core.monitoring;

import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.web.rest.ro.ConnectionMonitorRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

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

    @Test
    public void isMonitoringEnabled() {
    }

    @Test
    public void sendTestMessages() {
    }

    @Test
    public void testGetConnectionStatus() {
        // Given
        String[] partyIds = {"partyId1", "partyId2"};

        ConnectionMonitorRO conn1 = new ConnectionMonitorRO();
        ConnectionMonitorRO conn2 = new ConnectionMonitorRO();

        new Expectations() {{
            connectionMonitoringService.getConnectionStatus(partyIds[0]);
            result = conn1;
            connectionMonitoringService.getConnectionStatus(partyIds[1]);
            result = conn2;
        }};

        Map<String, ConnectionMonitorRO> info = new HashedMap();
        info.put(partyIds[0], conn1);
        info.put(partyIds[1], conn2);

        // When
        Map<String, ConnectionMonitorRO> result = connectionMonitoringService.getConnectionStatus(partyIds);

        // Then
        Assert.assertEquals(result, info);
    }
}