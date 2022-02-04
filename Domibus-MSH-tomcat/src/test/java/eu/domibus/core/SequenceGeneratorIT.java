package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertThat;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Ignore("EDELIVERY-8927: Bamboo - Sonar Branch plan is failing due to IT test failures")
public class SequenceGeneratorIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(SequenceGeneratorIT.class);

    @Autowired
    private AlertDao alertDao;

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

    @Test
    @Transactional
    public void findRetryAlertsOnParty() {

        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY", "blue_gw");
        alertCriteria.getParameters().put("TO_PARTY", "red_gw");
        alertCriteria.setPage(0);
        alertCriteria.setPageSize(10);
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        //yyMMddHHDDDDDDDDDD
        for (Alert alert : alerts) {
            long entityId = alert.getEntityId();
            LOG.info("Entity id: [{}]", entityId);
            assertThat(String.valueOf(entityId).length(), Is.is(18));
        }

    }
}