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

import java.util.ArrayList;
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

    public static final String SENDER_RECEIVER_SEPARATOR = ">";
    public static final String LIST_ITEM_SEPARATOR = ",";

    @Autowired
    private PartyService partyService;

    @Autowired
    protected TestService testService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public boolean isMonitoringEnabled() {
        boolean monitoringEnabled = !CollectionUtils.isEmpty(getMonitorEnabledParties());
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
                .filter(enabledParty -> testableParties.stream().anyMatch(testableParty -> testableParty.equalsIgnoreCase(enabledParty.split(SENDER_RECEIVER_SEPARATOR)[1])))
                .collect(Collectors.toList());
        for (String partyPair : monitoredParties) {
            String[] pairVals = partyPair.split(SENDER_RECEIVER_SEPARATOR);
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
        String partyPair = senderPartyId + SENDER_RECEIVER_SEPARATOR + partyId;
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
        return getCleanEnabledProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED);
    }

    private List<String> getCleanEnabledProperty(String propertyName) {
        ensureCorrectValueForProperty(propertyName);
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(propertyName);
    }

    public void ensureCorrectValueForProperty(String propertyName) {
        String propValue = domibusPropertyProvider.getProperty(propertyName);

        if (StringUtils.isEmpty(propValue)) {
            LOG.trace("Property [{}] is empty.", propertyName);
            return;
        }

        List<String> monitoredParties = domibusPropertyProvider.getCommaSeparatedPropertyValues(propertyName);
        String newValue = fixParties(monitoredParties);
        if (StringUtils.equals(propValue, newValue)) {
            LOG.trace("Nothing to fix for property [{}] value [{}]", propertyName, propValue);
            return;
        }

        LOG.info("Fixed property [{}] value from [{}] to [{}]", propertyName, propValue, newValue);
        domibusPropertyProvider.setProperty(propertyName, newValue);
    }

    protected String fixParties(List<String> monitoredParties) {
        List<String> result = new ArrayList<>();

        String defaultSelfPartyId = partyService.getGatewayPartyIdentifier();
        List<String> senderPartyIds = partyService.getGatewayPartyIdentifiers();
        List<String> destinationPartyIds = partyService.findPushToPartyNamesForTest();

        monitoredParties.forEach(monitoredPartyPair -> {
            String[] pairValues = monitoredPartyPair.split(SENDER_RECEIVER_SEPARATOR);
            if (pairValues.length == 1) {
                if (!destinationPartyIds.contains(pairValues[0])) {
                    LOG.debug("Party [{}] is not a valid destination party id so it will be eliminated.", pairValues[0]);
                    return;
                }
                String newVal = defaultSelfPartyId + SENDER_RECEIVER_SEPARATOR + pairValues[0];
                LOG.info("Fixing party enabled format for [{}] into [{}]", pairValues, newVal);
                result.add(newVal);
            } else {
                if (!senderPartyIds.contains(pairValues[0])) {
                    LOG.debug("Party [{}] is not a valid sender party id so it will be eliminated.", pairValues[0]);
                    return;
                }
                if (!destinationPartyIds.contains(pairValues[1])) {
                    LOG.debug("Party [{}] is not a valid destination party id so it will be eliminated.", pairValues[1]);
                    return;
                }
                result.add(monitoredPartyPair);
            }
        });

        return String.join(LIST_ITEM_SEPARATOR, result);
    }

}
