package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.web.AlertRo;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.web.rest.ro.AlertFilterRequestRO;
import eu.domibus.web.rest.ro.AlertResult;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.ValidationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class AlertResourceTest {
    @Tested
    AlertResource alertResource;

    @Injectable
    private AlertService alertService;

    @Injectable
    DateUtil dateUtil;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    DomainTaskExecutor domainTaskExecutor;

    AlertCriteria alertCriteria;
    List<Alert> alerts;

    @Test
    public void findAlertsTest() throws ValidationException {
        initAlertsData();

        new Expectations() {{
            authUtils.isSuperAdmin();
            result = false;

            alertService.countAlerts((AlertCriteria) any);
            result = 2;

            alertService.findAlerts((AlertCriteria) any);
            result = alerts;
        }};
        String[] params = {"USER"};
        AlertFilterRequestRO req = new AlertFilterRequestRO(){{
            setOrderBy("col1");
            setProcessed("false");
            setParameters(params);
            setDomainAlerts(true);
        }};
        AlertResult result = alertResource.findAlerts(req);

        Assert.assertEquals(2, result.getCount());
        Assert.assertEquals(1, result.getAlertsEntries().size());
        Assert.assertEquals("PASSWORD_EXPIRED", result.getAlertsEntries().get(0).getAlertType());
    }

    @Test
    public void retrieveAlertsTest() {
        initAlertsData();

        new Expectations() {{
            alertService.countAlerts(alertCriteria);
            result = 2;

            alertService.findAlerts(alertCriteria);
            result = alerts;
        }};

        AlertResult result = alertResource.retrieveAlerts(alertCriteria, true);

        Assert.assertEquals(2, result.getCount());
        Assert.assertEquals(1, result.getAlertsEntries().size());
        Assert.assertEquals(true, result.getAlertsEntries().get(0).isSuperAdmin());
    }

    private void initAlertsData() {
        alertCriteria = new AlertCriteria();
        Alert a = new Alert();
        a.setAlertLevel(AlertLevel.HIGH);
        a.setAlertStatus(AlertStatus.FAILED);
        a.setAlertType(AlertType.PASSWORD_EXPIRED);
        Set<Event> events = new HashSet<>();
        events.add(new Event());
        a.setEvents(events);
        alerts = Arrays.asList(a);
    }

    @Test
    public void getAlertParametersTest() {
        List<String> result = alertResource.getAlertParameters("PASSWORD_EXPIREDxxx");
        Assert.assertEquals(0, result.size());

        result = alertResource.getAlertParameters("PASSWORD_EXPIRED");
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("USER", result.get(0));
    }

    @Test
    public void processAlerts() {
        AlertRo alertRo = new AlertRo();
        alertRo.setSuperAdmin(false);
        alertRo.setProcessed(true);
        AlertRo alertRo2 = new AlertRo();
        alertRo2.setSuperAdmin(true);
        List<AlertRo> alertRos = Arrays.asList(alertRo, alertRo2);

        alertResource.processAlerts(alertRos);

        new Verifications(1) {{
            List<Alert> domainAlerts;
            alertService.updateAlertProcessed(domainAlerts = withCapture());
            times = 1;
            assertEquals(1, domainAlerts.size());
            assertEquals(true, domainAlerts.get(0).isProcessed());
        }};
    }

    @Test
    public void testFetchAndTransformAlerts() {
        initAlertsData(); // 1 alert

        boolean isSuperAdmin = true;
        new Expectations() {{
            alertService.findAlerts((AlertCriteria) any);
            result = alerts;
        }};

        List<AlertRo> alertsRO = alertResource.fetchAndTransformAlerts(alertCriteria, isSuperAdmin);

        new Verifications(1) {{
            assertEquals(1, alertsRO.size());
            assertEquals(isSuperAdmin, alertsRO.get(0).isSuperAdmin());
            csvServiceImpl.validateMaxRows(1, (Supplier<Long>) any);
            times = 1;
            alertService.countAlerts((AlertCriteria) any);
            times = 0;
        }};
    }
}