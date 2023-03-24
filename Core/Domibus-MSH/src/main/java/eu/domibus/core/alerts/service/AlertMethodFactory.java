package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_MAIL_SENDING_ACTIVE;


/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class AlertMethodFactory {

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected AlertMethodEmail alertMethodEmail;

    @Autowired
    protected AlertMethodLog alertMethodLog;

    public AlertMethod getAlertMethod() {
        AlertMethod result = alertMethodLog;

        final boolean mailActive = isSendEmailActive();
        if (mailActive) {
            result = alertMethodEmail;
        }

        return result;
    }

    private Boolean isSendEmailActive() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE);
    }
}
