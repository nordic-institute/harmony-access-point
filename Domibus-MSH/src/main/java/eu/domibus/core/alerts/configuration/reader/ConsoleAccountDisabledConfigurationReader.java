package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Reader of console user account disabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsoleAccountDisabledConfigurationReader extends AccountDisabledConfigurationReader {

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Override
    public AlertType getAlertType() {
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
