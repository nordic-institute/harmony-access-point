package eu.domibus.core.alerts.configuration.connectionMonitoring;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.alerts.configuration.generic.FrequencyAlertConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static eu.domibus.core.monitoring.ConnectionMonitoringHelper.ALL_PARTIES;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class ConnectionMonitoringModuleConfiguration extends FrequencyAlertConfiguration {

    private List<String> enabledParties;

    public ConnectionMonitoringModuleConfiguration() {
        super(AlertType.CONNECTION_MONITORING_FAILED);
    }

    public boolean shouldGenerateAlert(MessageStatus messageStatus, String toParty) {
        return isActive()
                && MessageStatus.getUnsuccessfulStates().contains(messageStatus)
                && (enabledParties.stream().anyMatch(party -> StringUtils.equalsAnyIgnoreCase(party, toParty, ALL_PARTIES)));
    }

    public void setEnabledParties(List<String> enabledParties) {
        this.enabledParties = enabledParties;
    }

    @Override
    public String toString() {
        return "ConnectionMonitoringConfiguration{" +
                "Active=" + isActive() +
                "Frequency=" + getFrequency() +
                "EnabledParties=" + enabledParties +
                '}';
    }

}
