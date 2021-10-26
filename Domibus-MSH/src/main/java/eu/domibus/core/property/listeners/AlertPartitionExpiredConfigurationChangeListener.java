package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.alerts.configuration.partitions.PartitionsConfigurationManager;
import eu.domibus.core.alerts.configuration.partitions.PartitionsModuleConfiguration;
import eu.domibus.core.alerts.configuration.password.expired.console.ConsolePasswordExpiredAlertConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author idragusa
 * @since 5.0
 * <p>
 * Handles the change of alert properties that are related to configuration of partition expired alerts
 */
@Service
public class AlertPartitionExpiredConfigurationChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private PartitionsConfigurationManager partitionsConfigurationManager;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_PARTITION_EXPIRATION_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        partitionsConfigurationManager.reset();
    }
}

