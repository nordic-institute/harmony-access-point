package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.pmode.provider.PModeProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    protected PModeProvider pModeProvider;

    @Override
    public boolean handlesProperty(String propertyName) {
        return DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED.equalsIgnoreCase(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        propertyValue = StringUtils.trimToEmpty(propertyValue).toLowerCase();
        List<String> newPartyIds = Arrays.stream(StringUtils.split(propertyValue, ','))
                .map(name -> name.trim())
                .filter(name -> !name.isEmpty())
                .distinct().collect(Collectors.toList());

        List<Party> knownParties = pModeProvider.findAllParties();
        List<String> testablePartyIds = pModeProvider.findPartyIdByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION, null);

        newPartyIds.forEach(partyId -> {
            if (knownParties.stream().noneMatch(party ->
                    party.getIdentifiers().stream().anyMatch(identifier -> partyId.equalsIgnoreCase(identifier.getPartyId())))) {
                throw new DomibusPropertyException("Could not change the list of monitoring parties: "
                        + partyId + " is not configured in Pmode");
            }
            if (testablePartyIds.stream().noneMatch(testablePartyId -> StringUtils.endsWithIgnoreCase(testablePartyId, partyId))) {
                throw new DomibusPropertyException("Could not change the list of monitoring parties: "
                        + partyId + " is not configured to receive test messages in Pmode");
            }
        });
    }
}
