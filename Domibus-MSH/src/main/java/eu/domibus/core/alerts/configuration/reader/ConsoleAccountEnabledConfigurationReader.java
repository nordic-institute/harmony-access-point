package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

@Service
public class ConsoleAccountEnabledConfigurationReader extends AccountEnabledConfigurationReader {

    @Override
    public AlertType getAlertType() {
        return AlertType.USER_ACCOUNT_ENABLED;
    }

    @Override
    protected String getModuleName() {
        return "Alert account enabled";
    }

    @Override
    protected String getAlertActivePropertyName() {
        return DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE;
    }

    @Override
    protected String getAlertLevelPropertyName() {
        return DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL;
    }

    @Override
    protected String getAlertEmailSubjectPropertyName() {
        return DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT;
    }

}
