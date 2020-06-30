package eu.domibus.web.rest;

import com.google.common.collect.Lists;
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
import mockit.*;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.ValidationException;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void processAlerts_domainAlerts(@Injectable AlertRo alertRo, @Injectable Alert alert) {
        // GIVEN
        List<AlertRo> alertRos = Lists.newArrayList(alertRo);

        new Expectations(alertResource) {{
            alertResource.filterDomainAlerts(alertRos); result = Lists.newArrayList(alert);;
            alertResource.filterSuperAlerts(alertRos);
            alertResource.filterDeletedDomainAlerts(alertRos);
            alertResource.filterDeletedSuperAlerts(alertRos);
            domainTaskExecutor.submit((Runnable) any);
            alertService.deleteAlerts((List<Alert>) any);
        }};

        // WHEN
        alertResource.processAlerts(alertRos);

        // THEN
        new FullVerifications(1) {{
            List<Alert> domainAlerts;
            alertService.updateAlertProcessed(domainAlerts = withCapture()); times = 1;
            assertEquals("Should have updated the domain alerts with the correct alert",
                    Lists.newArrayList(alert), domainAlerts);
        }};
    }

    @Test
    public void processAlerts_superAlerts(@Injectable AlertRo alertRo, @Injectable Alert alert) {
        // GIVEN
        List<AlertRo> alertRos = Lists.newArrayList(alertRo);

        new Expectations(alertResource) {{
            alertResource.filterDomainAlerts(alertRos);
            alertResource.filterSuperAlerts(alertRos); result = Lists.newArrayList(alert);;
            alertResource.filterDeletedDomainAlerts(alertRos);
            alertResource.filterDeletedSuperAlerts(alertRos);
            alertService.deleteAlerts((List<Alert>) any);
        }};

        // WHEN
        alertResource.processAlerts(alertRos);

        // Capture the runnable and execute it
        final List<Runnable> runnables = new ArrayList<>();
        new Verifications() {{
            domainTaskExecutor.submit(withCapture(runnables));
        }};
        runnables.get(0).run();

        // THEN
        new FullVerifications() {{
            List<List<Alert>> invocations = new ArrayList<>();
            alertService.updateAlertProcessed(withCapture(invocations)); times = 2;
            assertEquals("Should have scheduled the update of super alerts with the correct alert",
                    Lists.newArrayList(alert), invocations.get(1));
        }};
    }

    @Test
    public void processAlerts_deletedDomainAlerts(@Injectable AlertRo alertRo, @Injectable Alert alert) {
        // GIVEN
        List<AlertRo> alertRos = Lists.newArrayList(alertRo);

        new Expectations(alertResource) {{
            alertResource.filterDomainAlerts(alertRos);
            alertResource.filterSuperAlerts(alertRos);
            alertResource.filterDeletedDomainAlerts(alertRos); result = Lists.newArrayList(alert);
            alertResource.filterDeletedSuperAlerts(alertRos);
            domainTaskExecutor.submit((Runnable) any);
            alertService.updateAlertProcessed((List<Alert>) any);
        }};

        // WHEN
        alertResource.processAlerts(alertRos);

        // THEN
        new FullVerifications() {{
            List<Alert> deletedAlerts;
            alertService.deleteAlerts(deletedAlerts = withCapture());
            assertEquals("Should have deleted the correct alerts", Lists.newArrayList(alert), deletedAlerts);
        }};
    }

    @Test
    public void processAlerts_deletedSuperAlerts(@Injectable AlertRo alertRo, @Injectable Alert alert) {
        // GIVEN
        List<AlertRo> alertRos = Lists.newArrayList(alertRo);

        new Expectations(alertResource) {{
            alertResource.filterDomainAlerts(alertRos);
            alertResource.filterSuperAlerts(alertRos);
            alertResource.filterDeletedDomainAlerts(alertRos);
            alertResource.filterDeletedSuperAlerts(alertRos); result = Lists.newArrayList(alert);
            alertService.updateAlertProcessed((List<Alert>) any);
        }};

        // WHEN
        alertResource.processAlerts(alertRos);

        // Capture the runnable and execute it
        final List<Runnable> runnables = new ArrayList<>();
        new Verifications() {{
            domainTaskExecutor.submit(withCapture(runnables));
        }};
        runnables.get(1).run();

        // THEN
        new FullVerifications() {{
            List<List<Alert>> invocations = new ArrayList<>();
            alertService.deleteAlerts(withCapture(invocations)); times = 2;
            assertEquals("Should have deleted the correct super alerts",
                    Lists.newArrayList(alert), invocations.get(1));
        }};
    }

    @Test
    public void filterDomainAlerts(@Injectable AlertRo domainAlert, @Injectable AlertRo superAlert,
                                   @Injectable AlertRo deletedDomainAlert, @Injectable AlertRo deletedSuperAlert,
                                   @Injectable Alert filteredAlert) {
        // GIVEN
        new Expectations(alertResource) {{
            domainAlert.isSuperAdmin(); result = false;
            domainAlert.isDeleted(); result = false;
            superAlert.isSuperAdmin(); result = true;
            deletedDomainAlert.isSuperAdmin(); result = false;
            deletedDomainAlert.isDeleted(); result = true;
            deletedSuperAlert.isSuperAdmin(); result = true;

            alertResource.toAlert(domainAlert); result = filteredAlert;
        }};

        // WHEN
        List<Alert> result = alertResource.filterDomainAlerts(Lists.newArrayList(domainAlert, superAlert, deletedDomainAlert, deletedSuperAlert));

        // THEN
        new Verifications() {{
            assertEquals("Should have filtered out the correct domain alerts", Lists.newArrayList(filteredAlert), result);
        }};
    }

    @Test
    public void filterSuperAlerts(@Injectable AlertRo domainAlert, @Injectable AlertRo superAlert,
                                  @Injectable AlertRo deletedDomainAlert, @Injectable AlertRo deletedSuperAlert,
                                  @Injectable Alert filteredAlert) {
        // GIVEN
        new Expectations(alertResource) {{
            domainAlert.isSuperAdmin(); result = false;
            superAlert.isSuperAdmin(); result = true;
            superAlert.isDeleted(); result = false;
            deletedDomainAlert.isSuperAdmin(); result = false;
            deletedSuperAlert.isSuperAdmin(); result = true;
            deletedSuperAlert.isDeleted(); result = true;

            alertResource.toAlert(superAlert); result = filteredAlert;
        }};

        // WHEN
        List<Alert> result = alertResource.filterSuperAlerts(Lists.newArrayList(domainAlert, superAlert, deletedDomainAlert, deletedSuperAlert));

        // THEN
        new Verifications() {{
            assertEquals("Should have filtered out the correct super alerts", Lists.newArrayList(filteredAlert), result);
        }};
    }

    @Test
    public void filterDeletedDomainAlerts(@Injectable AlertRo domainAlert, @Injectable AlertRo superAlert,
                                          @Injectable AlertRo deletedDomainAlert, @Injectable AlertRo deletedSuperAlert,
                                          @Injectable Alert filteredAlert) {
        // GIVEN
        new Expectations(alertResource) {{
            domainAlert.isDeleted(); result = false;
            superAlert.isDeleted(); result = false;
            deletedDomainAlert.isDeleted(); result = true;
            deletedDomainAlert.isSuperAdmin(); result = false;
            deletedSuperAlert.isDeleted(); result = true;
            deletedSuperAlert.isSuperAdmin(); result = true;

            alertResource.toAlert(deletedDomainAlert); result = filteredAlert;
        }};

        // WHEN
        List<Alert> result = alertResource.filterDeletedDomainAlerts(Lists.newArrayList(domainAlert, superAlert, deletedDomainAlert, deletedSuperAlert));

        // THEN
        new Verifications() {{
            assertEquals("Should have filtered out the correct deleted domain alerts", Lists.newArrayList(filteredAlert), result);
        }};
    }

    @Test
    public void filterDeletedSuperAlerts(@Injectable AlertRo domainAlert, @Injectable AlertRo superAlert,
                                         @Injectable AlertRo deletedDomainAlert, @Injectable AlertRo deletedSuperAlert,
                                         @Injectable Alert filteredAlert) {
        // GIVEN
        new Expectations(alertResource) {{
            domainAlert.isDeleted(); result = false;
            superAlert.isDeleted(); result = false;
            deletedDomainAlert.isDeleted(); result = true;
            deletedDomainAlert.isSuperAdmin(); result = false;
            deletedSuperAlert.isDeleted(); result = true;
            deletedSuperAlert.isSuperAdmin(); result = true;

            alertResource.toAlert(deletedSuperAlert); result = filteredAlert;
        }};

        // WHEN
        List<Alert> result = alertResource.filterDeletedSuperAlerts(Lists.newArrayList(domainAlert, superAlert, deletedDomainAlert, deletedSuperAlert));

        // THEN
        new Verifications() {{
            assertEquals("Should have filtered out the correct deleted super alerts", Lists.newArrayList(filteredAlert), result);
        }};
    }


    @Test
    public void toAlert(@Injectable AlertRo alertRo) {
        // GIVEN
        new Expectations() {{
            alertRo.isProcessed(); result = true;
            alertRo.getEntityId(); result = 13l;
        }};

        // WHEN
        Alert result = alertResource.toAlert(alertRo);

        //THEN
        new FullVerifications() {{
           assertEquals("Should have set the correct entity ID when converting", 13, result.getEntityId());
           assertTrue("Should have set the correct processed flag when converting", result.isProcessed());
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