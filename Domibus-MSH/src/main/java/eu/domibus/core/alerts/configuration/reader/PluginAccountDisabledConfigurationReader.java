package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Reader of plugin user account disabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginAccountDisabledConfigurationReader extends AccountDisabledConfigurationReader {

    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_USER_ACCOUNT_DISABLED;
    }

    @Override
    protected String getModuleName() {
        return "Alert plugin account disabled";
    }

    @Override
    protected String getAlertActivePropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE;
    }

    @Override
    protected String getAlertLevelPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL;
    }

    @Override
    protected String getAlertMomentPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT;
    }

    @Override
    protected String getAlertEmailSubjectPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return false;
    }
}