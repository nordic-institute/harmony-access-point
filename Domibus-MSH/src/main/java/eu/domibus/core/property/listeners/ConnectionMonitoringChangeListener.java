package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.common.model.configuration.Party;
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
        List<Party> knownParties = pModeProvider.findAllParties();

        propertyValue = StringUtils.trimToEmpty(propertyValue).toLowerCase();
        List<String> partyNames = Arrays.stream(StringUtils.split(propertyValue, ','))
                .map(name -> name.trim())
                .filter(name -> !name.isEmpty())
                .distinct().collect(Collectors.toList());

        partyNames.forEach(partyName -> {
            if (knownParties.stream().noneMatch(party ->
                    party.getIdentifiers().stream().anyMatch(identifier -> partyName.equalsIgnoreCase(identifier.getPartyId())))) {
                throw new DomibusPropertyException("Could not change the list of monitoring parties: "
                        + partyName + " is not configured in Pmode");
            }
        });
    }
}
