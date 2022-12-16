package eu.domibus.core.monitoring;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.message.testservice.TestServiceException;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
@Service
public class ConnectionMonitoringHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringHelper.class);

    public static final String SENDER_RECEIVER_SEPARATOR = ">";
    public static final String LIST_ITEM_SEPARATOR = ",";
    public static final String ALL_PARTIES = "ALL";

    private final PartyService partyService;

    protected final PModeProvider pModeProvider;

    private final DomibusPropertyProvider domibusPropertyProvider;

    public ConnectionMonitoringHelper(PartyService partyService, PModeProvider pModeProvider, DomibusPropertyProvider domibusPropertyProvider) {
        this.partyService = partyService;
        this.pModeProvider = pModeProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public void validateReceiver(String receiverParty) {
        List<String> toParties = partyService.findPushToPartyNamesForTest();
        if (!toParties.contains(receiverParty)) {
            throw new TestServiceException(DomibusCoreErrorCode.DOM_003, "Cannot send a test message because the receiverParty party [" + receiverParty + "] is not a responder in any test process.");
        }
    }

    public void validateSender(String senderParty) {
        List<String> fromParties = partyService.findPushFromPartyNamesForTest();
        if (!fromParties.contains(senderParty)) {
            throw new TestServiceException(DomibusCoreErrorCode.DOM_003, "Cannot send a test message because the senderParty party [" + senderParty + "] is not an initiator in any test process.");
        }
    }

    public List<String> getMonitorEnabledParties() {
        if (StringUtils.containsIgnoreCase(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED), ALL_PARTIES)) {
            return getTestableParties();
        }
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED);
    }

    public List<String> getAlertableParties() {
        if (StringUtils.containsIgnoreCase(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES), ALL_PARTIES)) {
            return getTestableParties();
        }
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES);
    }

    public List<String> getDeleteHistoryForParties() {
        if (StringUtils.containsIgnoreCase(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_DELETE_HISTORY_FOR_PARTIES), ALL_PARTIES)) {
            return getTestableParties();
        }
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_MONITORING_CONNECTION_DELETE_HISTORY_FOR_PARTIES);
    }

    public List<String> getTestableParties() {
        List<String> testableParties = partyService.findPushToPartyNamesForTest();
        List<String> selfPartyIds = partyService.getGatewayPartyIdentifiers();
        List<String> result = new ArrayList<>();

        selfPartyIds.forEach(selfPartyId -> testableParties.forEach(partyId ->
                result.add(selfPartyId + SENDER_RECEIVER_SEPARATOR + partyId)));

        return result;
    }

    public String getDestinationParty(String pair) {
        return getElementFromPair(pair, 1);
    }

    public String getSourceParty(String pair) {
        return getElementFromPair(pair, 0);
    }

    private String getElementFromPair(String pair, int index) {
        String[] pairValues = pair.split(SENDER_RECEIVER_SEPARATOR);
        if (pairValues.length < 2) {
            LOG.info("Value [{}] is not a pair", pairValues);
            return StringUtils.EMPTY;
        }
        return pairValues[index];
    }

    public void ensureCorrectFormatForProperty(String propertyName) {
        String propValue = domibusPropertyProvider.getProperty(propertyName);
        if (StringUtils.equals(propValue, ALL_PARTIES)) {
            LOG.trace("Property [{}] has [{}] value so no correcting", propertyName, ALL_PARTIES);
            return;
        }
        List<String> monitoredParties = domibusPropertyProvider.getCommaSeparatedPropertyValues(propertyName);
        String selfPartyId = partyService.getGatewayPartyIdentifier();
        String newValue = transformToNewFormat(monitoredParties, selfPartyId);
        if (!StringUtils.equals(propValue, newValue)) {
            domibusPropertyProvider.setProperty(propertyName, newValue);
            try {
                // put some time between writes because the FilesCopy method that backs the property file crashes if copying while changed
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public String transformToNewFormat(List<String> monitoredParties, String selfPartyId) {
        for (int i = 0; i < monitoredParties.size(); i++) {
            String monitoredPartyPair = monitoredParties.get(i);
            String[] pairVals = monitoredPartyPair.split(SENDER_RECEIVER_SEPARATOR);
            if (pairVals.length == 1) {
                String newVal = selfPartyId + SENDER_RECEIVER_SEPARATOR + pairVals[0];
                monitoredParties.set(i, newVal);
                LOG.info("Fixing party enabled format for [{}] into [{}]", pairVals, newVal);
            }
        }
        return String.join(LIST_ITEM_SEPARATOR, monitoredParties);
    }

    public boolean partiesAreTestable(List<String> testableParties, String pair) {
        return testableParties.contains(getSourceParty(pair)) && testableParties.contains(getDestinationParty(pair));
    }

    public void validateEnabledPartiesValue(String propertyValue) {
        List<String> newPartyIds = parsePropertyValue(propertyValue);

        List<Party> knownParties = pModeProvider.findAllParties();
        List<String> testablePartyIds = partyService.findPushToPartyNamesForTest();

        newPartyIds.forEach(partyIdPair -> {
            Arrays.stream(partyIdPair.split(SENDER_RECEIVER_SEPARATOR)).forEach(partyId -> {
                LOG.trace("Checking that [{}] is a known party", partyId);
                if (knownParties.stream().noneMatch(party ->
                        party.getIdentifiers().stream().anyMatch(identifier -> partyId.equalsIgnoreCase(identifier.getPartyId())))) {
                    throw new DomibusPropertyException("Could not change the list of monitoring parties: "
                            + partyId + " is not configured in pMode");
                }
                LOG.trace("Checking that [{}] is a known testable party", partyId);
                if (testablePartyIds.stream().noneMatch(testablePartyId -> StringUtils.equalsIgnoreCase(testablePartyId, partyId))) {
                    throw new DomibusPropertyException("Could not change the list of monitoring parties: "
                            + partyId + " is not configured to receive test messages in pMode");
                }
            });
        });
    }

    protected List<String> parsePropertyValue(String propertyValue) {
        String[] propertyValueParts = StringUtils.split(StringUtils.trimToEmpty(propertyValue), ',');
        return Arrays.stream(propertyValueParts)
                .map(name -> name.trim().toLowerCase())
                .filter(name -> !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
