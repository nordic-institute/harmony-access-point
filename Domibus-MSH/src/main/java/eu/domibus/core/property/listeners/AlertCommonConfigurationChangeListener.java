package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.alerts.configuration.common.CommonConfigurationManager;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to common configuration
 */
@Service
public class AlertCommonConfigurationChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private CommonConfigurationManager commonConfigurationManager;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName,
                DOMIBUS_ALERT_MAIL_SENDING_ACTIVE,
                DOMIBUS_ALERT_SENDER_EMAIL,
                DOMIBUS_ALERT_RECEIVER_EMAIL,
                DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        commonConfigurationManager.reset();
    }

}
