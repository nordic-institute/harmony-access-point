package eu.domibus.core.monitoring;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.web.rest.ro.ConnectionMonitorRO;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConnectionMonitoringServiceImpl implements ConnectionMonitoringService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringServiceImpl.class);

    private final PartyService partyService;

    protected final TestService testService;

    private final DomibusPropertyProvider domibusPropertyProvider;

    public ConnectionMonitoringServiceImpl(PartyService partyService, TestService testService, DomibusPropertyProvider domibusPropertyProvider) {
        this.partyService = partyService;
        this.testService = testService;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public boolean isMonitoringEnabled() {
        List<String> monitoredParties = domibusPropertyProvider.getStringListProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED);
        if (CollectionUtils.isEmpty(monitoredParties)) {
            LOG.debug("Connection monitoring is not enabled");
            return false;
        }
        String selfParty = partyService.getGatewayPartyIdentifier();
        boolean monitoringEnabled = monitoredParties.stream()
                .anyMatch(party -> !StringUtils.equals(party, selfParty));
        LOG.debug("Connection monitoring enabled: [{}]", monitoringEnabled);
        return monitoringEnabled;
    }

    @Override
    public boolean isSelfMonitoringEnabled() {
        String selfParty = partyService.getGatewayPartyIdentifier();
        if (StringUtils.isEmpty(selfParty)) {
            LOG.info("The self party is not configured -> connection self monitoring disabled");
            return false;
        }

        boolean monitoringEnabled = StringUtils.contains(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED), selfParty);
        LOG.debug("Connection monitoring enabled: [{}]", monitoringEnabled);
        return monitoringEnabled;
    }

    @Override
    public void sendTestMessages() {
        sendTestMessagesTo(this::getAllMonitoredPartiesButMyself);
    }

    @Override
    public void sendTestMessageToMyself() {
        sendTestMessagesTo(this::getMyself);
    }

    private void sendTestMessagesTo(BiFunction<List<String>, String, List<String>> getMonitoredPartiesFn) {
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

        List<String> monitoredParties = getMonitoredPartiesFn.apply(testableParties, selfParty);
        if (CollectionUtils.isEmpty(monitoredParties)) {
            LOG.debug("There are no monitored parties to test");
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

    private List<String> getAllMonitoredPartiesButMyself(List<String> testableParties, String selfParty) {
        List<String> enabledParties = getMonitorEnabledParties();
        List<String> monitoredParties = testableParties.stream()
                .filter(partyId -> enabledParties.stream().anyMatch(partyId::equalsIgnoreCase))
                .filter(partyId -> !StringUtils.equals(partyId, selfParty))
                .collect(Collectors.toList());
        return monitoredParties;
    }

    private List<String> getMyself(List<String> testableParties, String selfParty) {
        List<String> enabledParties = getMonitorEnabledParties();
        List<String> monitoredParties = testableParties.stream()
                .filter(partyId -> StringUtils.equals(partyId, selfParty))
                .filter(partyId -> enabledParties.stream().anyMatch(partyId::equalsIgnoreCase))
                .collect(Collectors.toList());
        return monitoredParties;
    }

    @Override
    public Map<String, ConnectionMonitorRO> getConnectionStatus(String[] partyIds) {

        handleAllValue();

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

        if (lastSent != null) {
            TestServiceMessageInfoRO lastReceived = testService.getLastTestReceived(partyId, null);
            result.setLastReceived(lastReceived);
        }

        List<String> testableParties = partyService.findPushToPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
        if (testableParties.stream().anyMatch(partyId::equalsIgnoreCase)) {
            result.setTestable(true);
        }

        List<String> enabledParties = getMonitorEnabledParties();
        if (result.isTestable() && enabledParties.stream().anyMatch(partyId::equalsIgnoreCase)) {
            result.setMonitored(true);
        }

        List<String> alertableParties = getAlertableParties();
        if (result.isTestable() && alertableParties.stream().anyMatch(partyId::equalsIgnoreCase)) {
            result.setAlertable(true);
        }

        List<String> deleteHistoryForParties = getDeleteHistoryForParties();
        if (result.isTestable() && deleteHistoryForParties.stream().anyMatch(partyId::equalsIgnoreCase)) {
            result.setDeleteHistory(true);
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
        return domibusPropertyProvider.getStringListProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED);
    }

    private List<String> getAlertableParties() {
        return domibusPropertyProvider.getStringListProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES);
    }

    private List<String> getDeleteHistoryForParties() {
        return domibusPropertyProvider.getStringListProperty(DOMIBUS_MONITORING_CONNECTION_DELETE_OLD_FOR_PARTIES);
    }

    private void handleAllValue() {
        List<String> testableParties = partyService.findPushToPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
        String testablePartiesStr = testableParties.stream().collect(Collectors.joining(","));

        handleAllValue(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED, testablePartiesStr);
        handleAllValue(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES, testablePartiesStr);
        handleAllValue(DOMIBUS_MONITORING_CONNECTION_DELETE_OLD_FOR_PARTIES, testablePartiesStr);
    }

    private void handleAllValue(String propName, String propValue) {
        if (StringUtils.equals("ALL", domibusPropertyProvider.getProperty(propName))) {
            domibusPropertyProvider.setProperty(propName, propValue);
        }
    }
}
