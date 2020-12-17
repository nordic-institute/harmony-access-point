package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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

    @Autowired
    private AlertConfigurationService alertConfigurationService;

    public AlertMethod getAlertMethod() {
        AlertMethod result = alertMethodLog;

        final boolean mailActive = alertConfigurationService.isSendEmailActive();
        if (mailActive) {
            result = alertMethodEmail;
        }

        return result;
    }
}
