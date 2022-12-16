package eu.domibus.core.monitoring;

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
import static eu.domibus.core.monitoring.ConnectionMonitoringHelper.ALL_PARTIES;
import static eu.domibus.core.monitoring.ConnectionMonitoringHelper.SENDER_RECEIVER_SEPARATOR;

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

    private final ConnectionMonitoringHelper connectionMonitoringHelper;

    public ConnectionMonitoringServiceImpl(PartyService partyService, TestService testService, DomibusPropertyProvider domibusPropertyProvider, ConnectionMonitoringHelper connectionMonitoringHelper) {
        this.partyService = partyService;
        this.testService = testService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.connectionMonitoringHelper = connectionMonitoringHelper;
    }

    @Override
    public void sendTestMessages() {
        if (!isMonitoringEnabled()) {
            LOG.debug("Connection monitoring for others is not enabled; exiting;");
            return;
        }

        sendTestMessagesTo(this::getAllMonitoredPartiesButMyself);
    }

    @Override
    public void sendTestMessageToMyself() {
        if (!isSelfMonitoringEnabled()) {
            LOG.info("Self Connection monitoring is not enabled; exiting;");
            return;
        }

        sendTestMessagesTo(this::getMyself);
    }

    @Override
    public void deleteReceivedTestMessageHistory() {
        if (!isDeleteHistoryEnabled()) {
            LOG.debug("Delete received test message history is not enabled; exiting.");
            return;
        }

        List<String> testableParties = partyService.findPushFromPartyNamesForTest();
        if (CollectionUtils.isEmpty(testableParties)) {
            LOG.debug("There are no available parties to delete test message history; exiting.");
            return;
        }

        List<String> deleteHistoryParties = connectionMonitoringHelper.getDeleteHistoryForParties();
        deleteHistoryParties = deleteHistoryParties.stream()
                .filter(pair -> connectionMonitoringHelper.partiesAreTestable(testableParties, pair))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deleteHistoryParties)) {
            LOG.debug("There are no parties to delete test message history; exiting.");
            return;
        }

        for (String party : deleteHistoryParties) {
            testService.deleteReceivedMessageHistoryFromParty(connectionMonitoringHelper.getDestinationParty(party));
        }
    }

    protected boolean isDeleteHistoryEnabled() {
        return !CollectionUtils.isEmpty(connectionMonitoringHelper.getDeleteHistoryForParties());
    }

    /**
     * Checks whether the self monitoring is enabled.
     */
    protected boolean isSelfMonitoringEnabled() {
        List<String> selfPartyIds = partyService.getGatewayPartyIdentifiers();
        if (CollectionUtils.isEmpty(selfPartyIds)) {
            LOG.info("The self party is not configured -> connection self monitoring disabled");
            return false;
        }

        List<String> monitorEnabledParties = getMonitorEnabledParties();
        boolean monitoringEnabled = monitorEnabledParties.stream()
                .map(connectionMonitoringHelper::getDestinationParty)
                .anyMatch(selfPartyIds::contains);
        LOG.debug("Connection self-monitoring enabled: [{}]", monitoringEnabled);
        return monitoringEnabled;
    }

    /**
     * Checks whether the monitoring is enabled for at least a party except self.
     */
    protected boolean isMonitoringEnabled() {
        List<String> selfPartyIds = partyService.getGatewayPartyIdentifiers();
        if (CollectionUtils.isEmpty(selfPartyIds)) {
            LOG.info("The self party is not configured -> connection monitoring disabled");
            return false;
        }

        List<String> monitorEnabledParties = getMonitorEnabledParties();
        boolean monitoringEnabled = monitorEnabledParties.stream()
                .map(connectionMonitoringHelper::getDestinationParty)
                .anyMatch(destParty -> !selfPartyIds.contains(destParty));
        LOG.debug("Connection monitoring enabled: [{}]", monitoringEnabled);
        return monitoringEnabled;
    }

    private void sendTestMessagesTo(BiFunction<List<String>, List<String>, List<String>> getMonitoredPartiesFn) {
        List<String> testableParties = partyService.findPushToPartyNamesForTest();
        if (CollectionUtils.isEmpty(testableParties)) {
            LOG.debug("There are no available parties to test");
            return;
        }

        List<String> selfPartyIds = partyService.getGatewayPartyIdentifiers();
        if (CollectionUtils.isEmpty(selfPartyIds)) {
            LOG.info("The self party is not configured -> could not send test messages");
            return;
        }

        List<String> monitoredParties = getMonitoredPartiesFn.apply(testableParties, selfPartyIds);
        if (CollectionUtils.isEmpty(monitoredParties)) {
            LOG.debug("There are no monitored parties to test");
            return;
        }

        for (String partyPair : monitoredParties) {
            String senderParty = connectionMonitoringHelper.getSourceParty(partyPair);
            String receiverParty = connectionMonitoringHelper.getDestinationParty(partyPair);
            try {
                String testMessageId = testService.submitTest(senderParty, receiverParty);
                LOG.debug("Test message submitted from [{}] to [{}]: [{}]", senderParty, receiverParty, testMessageId);
            } catch (IOException | MessagingProcessingException e) {
                LOG.warn("Could not send test message from [{}] to [{}]", senderParty, receiverParty);
            }
        }
    }

    protected List<String> getAllMonitoredPartiesButMyself(List<String> testableParties, List<String> selfPartyIds) {
        List<String> enabledParties = getMonitorEnabledParties();
        List<String> monitoredParties = enabledParties.stream()
                .filter(ePair -> connectionMonitoringHelper.partiesAreTestable(testableParties, ePair))
                .filter(ePair -> !selfPartyIds.contains(connectionMonitoringHelper.getDestinationParty(ePair)))
                .collect(Collectors.toList());
        return monitoredParties;
    }

    private List<String> getMyself(List<String> testableParties, List<String> selfPartyIds) {
        List<String> enabledParties = getMonitorEnabledParties();
        List<String> monitoredParties = enabledParties.stream()
                .filter(ePair -> connectionMonitoringHelper.partiesAreTestable(testableParties, ePair))
                .filter(ePair -> selfPartyIds.contains(connectionMonitoringHelper.getDestinationParty(ePair)))
                .collect(Collectors.toList());
        return monitoredParties;
    }

    @Override
    public Map<String, ConnectionMonitorRO> getConnectionStatus(String senderPartyId, List<String> partyIds) {
        connectionMonitoringHelper.ensureCorrectFormatForProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED);
        connectionMonitoringHelper.ensureCorrectFormatForProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES);
        connectionMonitoringHelper.ensureCorrectFormatForProperty(DOMIBUS_MONITORING_CONNECTION_DELETE_HISTORY_FOR_PARTIES);

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

        String partyPair = senderPartyId + SENDER_RECEIVER_SEPARATOR + partyId;
        List<String> enabledParties = getMonitorEnabledParties();
        if (result.isTestable() && enabledParties.stream().anyMatch(partyPair::equalsIgnoreCase)) {
            result.setMonitored(true);
        }

        List<String> alertableParties = getAlertableParties();
        if (result.isTestable() && alertableParties.stream().anyMatch(partyPair::equalsIgnoreCase)) {
            result.setAlertable(true);
        }

        List<String> deleteHistoryForParties = connectionMonitoringHelper.getDeleteHistoryForParties();
        if (result.isTestable() && deleteHistoryForParties.stream().anyMatch(partyPair::equalsIgnoreCase)) {
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
        if (StringUtils.containsIgnoreCase(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED), ALL_PARTIES)) {
            return connectionMonitoringHelper.getTestableParties();
        }
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED);
    }

    private List<String> getAlertableParties() {
        if (StringUtils.containsIgnoreCase(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES), ALL_PARTIES)) {
            return connectionMonitoringHelper.getTestableParties();
        }
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES);
    }


}
