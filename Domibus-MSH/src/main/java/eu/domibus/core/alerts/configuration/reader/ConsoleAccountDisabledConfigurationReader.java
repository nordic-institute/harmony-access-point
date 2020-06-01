package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

public class ConsoleAccountDisabledConfigurationReader extends AccountDisabledConfigurationReader {

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Override
    protected AlertType getAlertType() {
        return AlertType.USER_ACCOUNT_DISABLED;
    }

    @Override
    protected String getModuleName() {
        return "Alert account disabled";
    }

    @Override
    protected String getAlertActivePropertyName() {
        return DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE;
    }

    @Override
    protected String getAlertLevelPropertyName() {
        return DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL;
    }

    @Override
    protected String getAlertMomentPropertyName() {
        return DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT;
    }

    @Override
    protected String getAlertEmailSubjectPropertyName() {
        return DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return domibusConfigurationService.isExtAuthProviderEnabled();
    }
}
