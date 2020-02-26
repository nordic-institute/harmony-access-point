package eu.domibus.core.monitoring;

import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.ebms3.common.model.Ebms3Constants;
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
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED;

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

    public boolean isMonitoringEnabled() {
        boolean monitoringEnabled = StringUtils.isNotBlank(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED));
        LOG.debug("Connection monitoring enabled: [{}]", monitoringEnabled);
        return monitoringEnabled;
    }

    public void sendTestMessages() {
        List<String> enabledParties = getMonitorEnabledParties();
        List<String> testableParties = partyService.findPushToPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
        List<String> monitoredParties = testableParties.stream().filter(p -> enabledParties.contains(p)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(testableParties)) {
            LOG.debug("There are no available parties to test");
            return;
        }

        String selfParty = partyService.getGatewayPartyIdentifier();
        if (StringUtils.isEmpty(selfParty)) {
            LOG.warn("The self party is not configured"); // TODO: lower level
            return;
        }

        for (String party : monitoredParties) {
            try {
                String testMessageId = testService.submitTest(selfParty, party);
                LOG.debug("Test message submitted from [{}] to [{}]: [{}]", selfParty, party, testMessageId);
            } catch (IOException | MessagingProcessingException e) {
                LOG.warn("Could not send test message from [{}] to [{}]", selfParty, party);
            }
        }
    }

    public ConnectionMonitorRO getConnectionStatus(String partyId) {
        ConnectionMonitorRO r = new ConnectionMonitorRO();

        TestServiceMessageInfoRO lastSent = testService.getLastTestSent(partyId);
        r.setLastSent(lastSent);

        TestServiceMessageInfoRO lastReceived = testService.getLastTestReceived(partyId, null);
        r.setLastReceived(lastReceived);

        List<String> testableParties = partyService.findPushToPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
        if (testableParties.contains(partyId)) {
            r.setTestable(true);
        }
        List<String> enabledParties = getMonitorEnabledParties();
        if (enabledParties.contains(partyId) && r.isTestable()) {
            r.setMonitored(true);
        }

        r.setStatus(getConnectionStatus(lastSent));

        return r;
    }

    private ConnectionMonitorRO.ConnectionStatus getConnectionStatus(TestServiceMessageInfoRO lastSent) {
        ConnectionMonitorRO.ConnectionStatus status;
        if (lastSent != null) {
            if (lastSent.getMessageStatus() == MessageStatus.SEND_FAILURE) {
                status = ConnectionMonitorRO.ConnectionStatus.BROKEN;
            } else if (lastSent.getMessageStatus() == MessageStatus.ACKNOWLEDGED) {
                status = ConnectionMonitorRO.ConnectionStatus.OK;
            } else {
                status = ConnectionMonitorRO.ConnectionStatus.PENDING;
            }
        } else {
            status = ConnectionMonitorRO.ConnectionStatus.UNKNOWN;
        }
        return status;
    }

    private List<String> getMonitorEnabledParties() {
        List<String> enabledParties = Arrays.asList(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED).split(","));
        enabledParties = enabledParties.stream().map(p -> p.trim()).collect(Collectors.toList());
        return enabledParties;
    }

}
