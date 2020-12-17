package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class AlertDispatcherServiceImpl implements AlertDispatcherService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertDispatcherServiceImpl.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    protected AlertMethodFactory alertMethodFactory;

    @Override
    @Transactional
    public void dispatch(Alert alert) {
        if (findAlert(alert.getEntityId()) == null) {
            if (alert.getAttempts() >= alert.getMaxAttempts()) {
                LOG.debug("Alert not found in the database, skip dispatching: [{}]", alert);
                return;
            }
            alert.setAttempts(alert.getAttempts() + 1);
            alertService.enqueueAlert(alert);
            LOG.debug("Alert enqueued for retry: [{}]", alert);
            return;
        }
        LOG.debug("Dispatching alert [{}]", alert);
        try {
            alert.setAlertStatus(AlertStatus.FAILED);
            alertMethodFactory.getAlertMethod().sendAlert(alert);
            alert.setAlertStatus(AlertStatus.SUCCESS);
        } finally {
            alertService.handleAlertStatus(alert);
        }
    }

    protected Alert findAlert(long alertId) {
        AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.setPageSize(1);
        alertCriteria.setAlertID(alertId);
        return alertService.findAlerts(alertCriteria).stream().findFirst().orElse(null);
    }

}
