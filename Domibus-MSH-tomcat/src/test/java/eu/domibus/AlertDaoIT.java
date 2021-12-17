package eu.domibus;

import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Soumya
 * @since 5.0
 */
public class AlertDaoIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(AlertDaoIT.class);

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private EventDao eventDao;

    @Before
    public void setUp() {
        createAlert("blue_gw", "red_gw", false, null);
        createAlert("blue_gw", "red_gw", true, null);
        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }


    public void createAlert(String fromParty, String toParty, boolean processed, Date reportingTime) {
        Event event = new Event();
        final StringEventProperty blue_gw = new StringEventProperty();
        blue_gw.setStringValue(fromParty);

        final StringEventProperty red_gw = new StringEventProperty();
        red_gw.setStringValue(toParty);

        final StringEventProperty role = new StringEventProperty();
        role.setStringValue("SENDER");

        event.addProperty("FROM_PARTY", blue_gw);
        event.addProperty("TO_PARTY", red_gw);
        event.addProperty("ROLE", role);
        event.setType(EventType.MSG_STATUS_CHANGED);
        event.setReportingTime(new Date());


        Alert alert = new Alert();
        alert.setAlertStatus(AlertStatus.FAILED);
        alert.setAlertType(AlertType.MSG_STATUS_CHANGED);
        alert.addEvent(event);
        alert.setAlertLevel(AlertLevel.MEDIUM);
        alert.setMaxAttempts(1);
        alert.setCreationTime(new Date());
        alert.setProcessed(processed);
        alert.setReportingTime(reportingTime);
        alertDao.create(alert);
    }

    public static Date asDate(LocalDateTime localDate) {
        return Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    public void deleteAlerts() {
        final LocalDateTime now = LocalDateTime.now();
        final Date deletionDate = asDate(now.plusDays(1));
        final Date reportingDate = asDate(now);
        createAlert("blue_gw", "red_gw", true, reportingDate);
        alertDao.deleteAlerts(deletionDate);
        assertEquals(Collections.emptyList(), alertDao.findRetryAlerts());
    }
}