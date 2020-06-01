package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

public class ConsoleLoginFailConfigurationReader extends LoginFailConfigurationReader {

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Override
    protected AlertType getAlertType() {
        return AlertType.USER_LOGIN_FAILURE;
    }

    @Override
    protected String getModuleName() {
        return "Alert Login failure";
    }

    @Override
    protected String getAlertActivePropertyName() {
        return DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE;
    }

    @Override
    protected String getAlertLevelPropertyName() {
        return DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL;
    }

    @Override
    protected String getAlertEmailSubjectPropertyName() {
        return DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return domibusConfigurationService.isExtAuthProviderEnabled();
    }
}
