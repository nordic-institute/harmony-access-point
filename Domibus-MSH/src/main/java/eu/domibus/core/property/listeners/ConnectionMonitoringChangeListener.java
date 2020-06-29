package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED;


/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Handles the change of connection monitoring properties, validating that only known party identifiers are used.
 */
@Service
public class ConnectionMonitoringChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringChangeListener.class);

    protected PModeProvider pModeProvider;

    public ConnectionMonitoringChangeListener(PModeProvider pModeProvider) {
        this.pModeProvider = pModeProvider;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED.equalsIgnoreCase(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        List<String> newPartyIds = parsePropertyValue(propertyValue);

        List<Party> knownParties = pModeProvider.findAllParties();
        List<String> testablePartyIds = pModeProvider.findPartyIdByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION, null);

        newPartyIds.forEach(partyId -> {
            LOG.trace("Checking that [{}] is a known party", partyId);
            if (knownParties.stream().noneMatch(party ->
                    party.getIdentifiers().stream().anyMatch(identifier -> partyId.equalsIgnoreCase(identifier.getPartyId())))) {
                throw new DomibusPropertyException("Could not change the list of monitoring parties: "
                        + partyId + " is not configured in Pmode");
            }
            LOG.trace("Checking that [{}] is a known testable party", partyId);
            if (testablePartyIds.stream().noneMatch(testablePartyId -> StringUtils.equalsIgnoreCase(testablePartyId, partyId))) {
                throw new DomibusPropertyException("Could not change the list of monitoring parties: "
                        + partyId + " is not configured to receive test messages in Pmode");
            }
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
