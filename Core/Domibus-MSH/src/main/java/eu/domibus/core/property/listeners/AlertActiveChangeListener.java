package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_ACTIVE;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert enabled property
 */
@Service
public class AlertActiveChangeListener implements DomibusPropertyChangeListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertActiveChangeListener.class);

    @Autowired
    private MailSender mailSender;

    @Autowired
    private AlertConfigurationService alertConfigurationService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName, DOMIBUS_ALERT_ACTIVE);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.debug("Global active property for alerts has changed for domain [{}]. Resetting all configuration loaders.", domainCode);

        mailSender.reset();

        alertConfigurationService.resetAll();
    }

}
