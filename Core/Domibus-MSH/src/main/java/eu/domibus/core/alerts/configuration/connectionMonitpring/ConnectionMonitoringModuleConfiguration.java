package eu.domibus.core.alerts.configuration.connectionMonitpring;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class ConnectionMonitoringModuleConfiguration extends AlertModuleConfigurationBase {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringModuleConfiguration.class);

    public ConnectionMonitoringModuleConfiguration(final AlertLevel alertLevel, final String mailSubject) {
        super(AlertType.CONNECTION_MONITORING_FAILED, alertLevel, mailSubject);
    }

    public ConnectionMonitoringModuleConfiguration() {
        super(AlertType.CONNECTION_MONITORING_FAILED);
    }

    public boolean shouldMonitorMessageStatus(MessageStatus messageStatus) {
        return isActive() && MessageStatus.getUnSuccessfulStates().contains(messageStatus);
    }

    @Override
    public String toString() {
        return "MessagingConfiguration{" +
                "messageCommunicationActive=" + isActive() +
                '}';
    }

}
