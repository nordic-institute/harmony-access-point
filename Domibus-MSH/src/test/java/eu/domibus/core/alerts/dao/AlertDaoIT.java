package eu.domibus.core.alerts.dao;

import eu.domibus.core.alerts.model.common.*;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.dao.InMemoryDataBaseConfig;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, AlertDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class AlertDaoIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(AlertDaoIT.class);

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private EventDao eventDao;

    @Before
    public void setUp(){
        createAlert("blue_gw","red_gw",false,null);
        createAlert("blue_gw","red_gw",true,null);
        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }


    public void createAlert(String fromParty,String toParty,boolean processed,Date reportingTime){
        Event event=new Event();
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


        Alert alert=new Alert();
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
        alertCriteria.getParameters().put("FROM_PARTY","blue_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setPage(0);
        alertCriteria.setPageSize(10);
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        assertEquals(2,alerts.size());
        alerts.forEach(alert1 -> alert1.getEvents().
                forEach(event1 -> event1.getProperties().
                        forEach((ke, eventProperty) ->  LOG.info("Key[{}] value[{}]",ke,eventProperty.getValue()))));
    }

    @Test
    @Transactional
    public void findRetryAlertsOnPartyButProcessed() {
        createAlert("black_gw","red_gw",true,null);
        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","black_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setProcessed(true);
        alertCriteria.setPage(0);
        alertCriteria.setPageSize(10);
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        assertEquals(1,alerts.size());
        alerts.forEach(alert1 -> alert1.getEvents().
                forEach(event1 -> event1.getProperties().
                        forEach((ke, eventProperty) ->  LOG.info("Key[{}] value[{}]",ke,eventProperty.getValue()))));
    }

    @Test
    public void findRetryAlertsOnPartyAndReportingTime() {
        final LocalDateTime now = LocalDateTime.now();
        final Date reportingDate = asDate(now.minusMinutes(15));
        final Date reportingFrom = asDate(now.minusMinutes(16));
        final Date reportingTo = asDate(now.minusMinutes(14));
        createAlert("blue_gw","red_gw",true,reportingDate);
        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","blue_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setReportingFrom(reportingFrom);
        alertCriteria.setReportingTo(reportingTo);
        alertCriteria.setProcessed(true);
        alertCriteria.setPage(0);
        alertCriteria.setPageSize(10);
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        assertEquals(1,alerts.size());
        assertNotNull(alerts.get(0).getCreationTime());
        assertNotNull(alerts.get(0).getModificationTime());
    }

    @Test
    public void countAlerts() {
        final LocalDateTime now = LocalDateTime.now();
        final Date reportingDate = asDate(now.minusMinutes(25));
        final Date reportingFrom = asDate(now.minusMinutes(26));
        final Date reportingTo = asDate(now.minusMinutes(24));
        createAlert("blue_gw","red_gw",true,reportingDate);
        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","blue_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setReportingFrom(reportingFrom);
        alertCriteria.setReportingTo(reportingTo);
        alertCriteria.setProcessed(true);
        final Long count = alertDao.countAlerts(alertCriteria);
        assertEquals(1,count.intValue());
    }

    public static Date asDate(LocalDateTime localDate) {
        return Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
    }
}