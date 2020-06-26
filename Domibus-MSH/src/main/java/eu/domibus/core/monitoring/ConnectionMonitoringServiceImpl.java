package eu.domibus.core.monitoring;

import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.web.rest.ro.ConnectionMonitorRO;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
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

    private final static Logger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringServiceImpl.class);

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
        List<String> testableParties = partyService.findPushToPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
        if (CollectionUtils.isEmpty(testableParties)) {
            LOG.debug("There are no available parties to test");
            return;
        }

        String selfParty = partyService.getGatewayPartyIdentifier();
        if (StringUtils.isEmpty(selfParty)) {
            LOG.info("The self party is not configured -> could not send test messages");
            return;
        }

        List<String> enabledParties = getMonitorEnabledParties();
        List<String> monitoredParties = testableParties.stream()
                .filter(partyId -> enabledParties.stream().anyMatch(enabledPartyId -> enabledPartyId.equalsIgnoreCase(partyId)))
                .collect(Collectors.toList());
        for (String party : monitoredParties) {
            try {
                String testMessageId = testService.submitTest(selfParty, party);
                LOG.debug("Test message submitted from [{}] to [{}]: [{}]", selfParty, party, testMessageId);
            } catch (IOException | MessagingProcessingException e) {
                LOG.warn("Could not send test message from [{}] to [{}]", selfParty, party);
            }
        }
    }

    @Override
    public Map<String, ConnectionMonitorRO> getConnectionStatus(String[] partyIds) {
        Map<String, ConnectionMonitorRO> result = new HashMap<>();
        for (String partyId : partyIds) {
            ConnectionMonitorRO status = this.getConnectionStatus(partyId);
            result.put(partyId, status);
        }
        return result;
    }

    protected ConnectionMonitorRO getConnectionStatus(String partyId) {
        ConnectionMonitorRO result = new ConnectionMonitorRO();

        TestServiceMessageInfoRO lastSent = testService.getLastTestSent(partyId);
        result.setLastSent(lastSent);

        TestServiceMessageInfoRO lastReceived = testService.getLastTestReceived(partyId, null);
        result.setLastReceived(lastReceived);

        List<String> testableParties = partyService.findPushToPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
        if (testableParties.stream().anyMatch(testablePartyId -> testablePartyId.equalsIgnoreCase(partyId))) {
            result.setTestable(true);
        }

        List<String> enabledParties = getMonitorEnabledParties();
        if (result.isTestable() && enabledParties.stream().anyMatch(enabledPartyId -> enabledPartyId.equalsIgnoreCase(partyId))) {
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
        List<String> enabledParties = Arrays.asList(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED).split(","));
        enabledParties = enabledParties.stream()
                .map(enabledPartyId -> StringUtils.trim(enabledPartyId))
                .collect(Collectors.toList());
        return enabledParties;
    }

}
