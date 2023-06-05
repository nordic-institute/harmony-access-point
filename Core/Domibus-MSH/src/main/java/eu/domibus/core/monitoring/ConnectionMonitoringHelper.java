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
        List<String> fromParties = partyService.getGatewayPartyIdentifiers();
        if (!fromParties.contains(senderParty)) {
            throw new TestServiceException(DomibusCoreErrorCode.DOM_003, "Cannot send a test message because the senderParty party [" + senderParty + "] is not an initiator in any test process.");
        }
    }

    public List<String> getMonitorEnabledParties() {
        return getCleanEnabledProperty(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED);
    }

    public List<String> getAlertableParties() {
        return getCleanEnabledProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES);
    }

    public List<String> getDeleteHistoryForParties() {
        return getCleanEnabledProperty(DOMIBUS_MONITORING_CONNECTION_DELETE_HISTORY_FOR_PARTIES);
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

    public boolean partiesAreTestable(List<String> testableParties, String pair) {
        return testableParties.contains(getSourceParty(pair)) && testableParties.contains(getDestinationParty(pair));
    }

    public void ensureCorrectValueForProperty(String propertyName) {
        String propValue = domibusPropertyProvider.getProperty(propertyName);

        if (StringUtils.isEmpty(propValue)) {
            LOG.trace("Property [{}] is empty.", propertyName);
            return;
        }

        if (StringUtils.equalsIgnoreCase(propValue, ALL_PARTIES)) {
            LOG.trace("Property [{}] has [{}] value so no correcting", propertyName, ALL_PARTIES);
            return;
        }

        List<String> monitoredParties = domibusPropertyProvider.getCommaSeparatedPropertyValues(propertyName);
        String newValue = fixParties(monitoredParties);
        if (StringUtils.equals(propValue, newValue)) {
            LOG.debug("Nothing to fix for property [{}]", propertyName);
            return;
        }

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
                    LOG.info("Party [{}] is not a valid destination party id so it will be eliminated.", pairValues[0]);
                    return;
                }
                String newVal = defaultSelfPartyId + SENDER_RECEIVER_SEPARATOR + pairValues[0];
                LOG.info("Fixing party enabled format for [{}] into [{}]", pairValues, newVal);
                result.add(newVal);
            } else {
                if (!senderPartyIds.contains(pairValues[0])) {
                    LOG.info("Party [{}] is not a valid sender party id so it will be eliminated.", pairValues[0]);
                    return;
                }
                if (!destinationPartyIds.contains(pairValues[1])) {
                    LOG.info("Party [{}] is not a valid destination party id so it will be eliminated.", pairValues[1]);
                    return;
                }
                result.add(monitoredPartyPair);
            }
        });

        return String.join(LIST_ITEM_SEPARATOR, result);
    }

    public void validateEnabledPartiesValue(String propertyValue) {
        if (StringUtils.isEmpty(propertyValue)) {
            LOG.trace("Property is empty.");
            return;
        }

        if (StringUtils.equalsIgnoreCase(propertyValue, ALL_PARTIES)) {
            LOG.trace("Property has [{}] value so no correcting", ALL_PARTIES);
            return;
        }

        List<String> newPartyIds = parsePropertyValue(propertyValue);

        List<Party> knownParties = pModeProvider.findAllParties();
        List<String> testablePartyIds = partyService.findPushToPartyNamesForTest();
        List<String> senderPartyIds = partyService.getGatewayPartyIdentifiers();

        newPartyIds.forEach(partyIdPair -> {
            String[] pairValues = partyIdPair.split(SENDER_RECEIVER_SEPARATOR);
            if (pairValues.length < 2) {
                throw new DomibusPropertyException("Invalid list of monitoring parties: "
                        + pairValues[0] + " must be in a senderPartyId>destinationPartyId format");
            }

            String partyId = pairValues[0];
            checkKnownParty(knownParties, partyId);
            checkSenderParty(senderPartyIds, partyId);

            partyId = pairValues[1];
            checkKnownParty(knownParties, partyId);
            checkDestinationParty(testablePartyIds, partyId);
        });
    }

    private void checkDestinationParty(List<String> testablePartyIds, String partyId) {
        LOG.trace("Checking that [{}] is a known testable party", partyId);
        if (testablePartyIds.stream().noneMatch(testablePartyId -> StringUtils.equalsIgnoreCase(testablePartyId, partyId))) {
            throw new DomibusPropertyException("Invalid list of monitoring parties: "
                    + partyId + " is not configured to receive test messages in pMode");
        }
    }

    private void checkSenderParty(List<String> senderPartyIds, String partyId) {
        LOG.trace("Checking that [{}] is a known testable party", partyId);
        if (senderPartyIds.stream().noneMatch(testablePartyId -> StringUtils.equalsIgnoreCase(testablePartyId, partyId))) {
            throw new DomibusPropertyException("Invalid list of monitoring parties: "
                    + partyId + " is not configured to send test messages in pMode");
        }
    }

    private void checkKnownParty(List<Party> knownParties, String partyId) {
        LOG.trace("Checking that [{}] is a known party", partyId);
        if (knownParties.stream().noneMatch(party ->
                party.getIdentifiers().stream().anyMatch(identifier -> partyId.equalsIgnoreCase(identifier.getPartyId())))) {
            throw new DomibusPropertyException("Invalid list of monitoring parties: "
                    + partyId + " is not configured in pMode");
        }
    }

    protected List<String> parsePropertyValue(String propertyValue) {
        String[] propertyValueParts = StringUtils.split(StringUtils.trimToEmpty(propertyValue), ',');
        return Arrays.stream(propertyValueParts)
                .map(name -> name.trim().toLowerCase())
                .filter(name -> !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private String getElementFromPair(String pair, int index) {
        String[] pairValues = pair.split(SENDER_RECEIVER_SEPARATOR);
        if (pairValues.length < 2) {
            LOG.info("Value [{}] is not a pair", pairValues);
            return StringUtils.EMPTY;
        }
        return pairValues[index];
    }

    private List<String> getCleanEnabledProperty(String propertyName) {
        if (StringUtils.containsIgnoreCase(domibusPropertyProvider.getProperty(propertyName), ALL_PARTIES)) {
            return getTestableParties();
        }
        ensureCorrectValueForProperty(propertyName);
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(propertyName);
    }
}
