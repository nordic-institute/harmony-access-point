package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to login failure configuration for console users
 */
@Service
public class AlertAccountDisabledConfigurationChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithAny(propertyName,
                DomibusPropertyMetadataManager.DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        throw new NullPointerException("bla bla");
//        multiDomainAlertConfigurationService.clearAccountDisabledConfiguration();
    }
}
