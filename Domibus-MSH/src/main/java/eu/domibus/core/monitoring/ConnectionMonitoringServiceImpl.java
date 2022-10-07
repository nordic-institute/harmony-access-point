package eu.domibus.core.monitoring;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.ConnectionMonitorRO;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConnectionMonitoringServiceImpl implements ConnectionMonitoringService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringServiceImpl.class);
    public static final String PARTY_SEPARATOR = ">";

    @Autowired
    private PartyService partyService;

    @Autowired
    protected TestService testService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public boolean isMonitoringEnabled() {
        boolean monitoringEnabled = StringUtils.isNotBlank(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED));
        LOG.debug("Connection monitoring enabled: [{}]", monitoringEnabled);
        return monitoringEnabled;
    }

    @Override
    public void sendTestMessages() {
        List<String> testableParties = partyService.findPushToPartyNamesForTest();
        if (CollectionUtils.isEmpty(testableParties)) {
            LOG.debug("There are no available parties to test");
            return;
        }

        List<String> enabledParties = getMonitorEnabledParties();
        List<String> monitoredParties = enabledParties.stream()
                .filter(enabledParty -> testableParties.stream().anyMatch(testableParty -> testableParty.equalsIgnoreCase(enabledParty.split(PARTY_SEPARATOR)[1])))
                .collect(Collectors.toList());
        for (String partyPair : monitoredParties) {
            String[] pairVals = partyPair.split(PARTY_SEPARATOR);
            String senderParty = pairVals[0];
            String receiverParty = pairVals[1];
            try {
                String testMessageId = testService.submitTest(senderParty, receiverParty);
                LOG.debug("Test message submitted from [{}] to [{}]: [{}]", senderParty, receiverParty, testMessageId);
            } catch (Exception ex) {
                LOG.warn("Could not send test message from [{}] to [{}]", senderParty, receiverParty, ex);
            }
        }
    }

    @Override
    public Map<String, ConnectionMonitorRO> getConnectionStatus(String senderPartyId, List<String> partyIds) {
        ensureMewFormatForEnabledProperty();

        Map<String, ConnectionMonitorRO> result = new HashMap<>();
        for (String partyId : partyIds) {
            ConnectionMonitorRO status = this.getConnectionStatus(senderPartyId, partyId);
            result.put(partyId, status);
        }
        return result;
    }

    protected ConnectionMonitorRO getConnectionStatus(String senderPartyId, String partyId) {
        ConnectionMonitorRO result = new ConnectionMonitorRO();

        TestServiceMessageInfoRO lastSent = testService.getLastTestSent(senderPartyId, partyId);
        result.setLastSent(lastSent);

        if (lastSent != null) {
            TestServiceMessageInfoRO lastReceived = testService.getLastTestReceived(senderPartyId, partyId, null);
            result.setLastReceived(lastReceived);
        }

        List<String> testableParties = partyService.findPushToPartyNamesForTest();
        if (testableParties.stream().anyMatch(partyId::equalsIgnoreCase)) {
            result.setTestable(true);
        }

        List<String> enabledParties = getMonitorEnabledParties();
        String partyPair = senderPartyId + PARTY_SEPARATOR + partyId;
        if (result.isTestable() && enabledParties.stream().anyMatch(partyPair::equalsIgnoreCase)) {
            result.setMonitored(true);
        }

        result.setStatus(getConnectionStatus(lastSent));

        return result;
    }

    private ConnectionMonitorRO.ConnectionStatus getConnectionStatus(TestServiceMessageInfoRO lastSent) {
        if (lastSent != null) {
            if (lastSent.getMessageStatus() == MessageStatus.SEND_FAILURE) {
                return ConnectionMonitorRO.ConnectionStatus.BROKEN;
            } else if (lastSent.getMessageStatus() == MessageStatus.ACKNOWLEDGED) {
                return ConnectionMonitorRO.ConnectionStatus.OK;
            } else {
                return ConnectionMonitorRO.ConnectionStatus.PENDING;
            }
        }
        return ConnectionMonitorRO.ConnectionStatus.UNKNOWN;
    }

    private List<String> getMonitorEnabledParties() {
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED);
    }

    protected void ensureMewFormatForEnabledProperty() {
        String selfPartyId = partyService.getGatewayPartyIdentifier();
        List<String> monitoredParties = getMonitorEnabledParties();
        String newValue = transformToNewFormat(monitoredParties, selfPartyId);
        domibusPropertyProvider.setProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED, newValue);
    }

    protected String transformToNewFormat(List<String> monitoredParties, String selfPartyId) {
        for (int i = 0; i < monitoredParties.size(); i++) {
            String monitoredPartyPair = monitoredParties.get(i);
            String[] pairVals = monitoredPartyPair.split(PARTY_SEPARATOR);
            if (pairVals.length == 1) {
                monitoredParties.set(i, selfPartyId + PARTY_SEPARATOR + pairVals[0]);
            }
        }
        String newValue = monitoredParties.stream().collect(Collectors.joining(","));
        return newValue;
    }

}
