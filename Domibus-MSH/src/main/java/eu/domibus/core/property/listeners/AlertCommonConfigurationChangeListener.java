package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.alerts.model.service.CommonConfiguration;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to common configuration
 */
@Service
public class AlertCommonConfigurationChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    private ConfigurationLoader<CommonConfiguration> commonConfigurationConfigurationLoader;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName,
                DOMIBUS_ALERT_MAIL_SENDING_ACTIVE,
                DOMIBUS_ALERT_SUPER_MAIL_SENDING_ACTIVE,
                DOMIBUS_ALERT_SENDER_EMAIL, DOMIBUS_ALERT_SUPER_SENDER_EMAIL,
                DOMIBUS_ALERT_RECEIVER_EMAIL, DOMIBUS_ALERT_SUPER_RECEIVER_EMAIL,
                DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME,
                DOMIBUS_ALERT_SUPER_CLEANER_ALERT_LIFETIME);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);

        commonConfigurationConfigurationLoader.resetConfiguration(domain);
    }

}
