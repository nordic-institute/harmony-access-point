package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private AlertDao alertDao;

    @Autowired
    protected AlertMethodFactory alertMethodFactory;

    @Override
    public void dispatch(Alert alert) {
        if (alertDao.read(alert.getEntityId()) == null) {
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

}
