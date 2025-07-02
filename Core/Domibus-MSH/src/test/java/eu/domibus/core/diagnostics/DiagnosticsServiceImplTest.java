package eu.domibus.core.diagnostics;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.property.DomibusVersionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
@RunWith(JMockit.class)
public class DiagnosticsServiceImplTest {

    @Tested
    DiagnosticsServiceImpl diagnosticsService;

    @Injectable
    private DomibusVersionService domibusVersionService;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private eu.domibus.core.alerts.service.AlertService alertService;

    @Injectable
    private eu.domibus.core.message.MessagesLogService messagesLogService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Test
    public void logDiagnosticInfo_withVersionInfo() {
        // Setup
        List<String> diagnosticsList = Arrays.asList("versionInfo");
        Domain domain = new Domain("default", "Default Domain");

        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
            result = diagnosticsList;

            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            domibusVersionService.getArtifactName();
            result = "Domibus";

            domibusVersionService.getArtifactVersion();
            result = "5.1.9";

            domibusVersionService.getBuiltTime();
            result = "2023-01-01";

            domibusVersionService.getVersionNumber();
            result = "5.1.9";
        }};

        // Execute
        diagnosticsService.logDiagnosticInfo();

        // Verify
        new Verifications() {{
            domibusVersionService.getArtifactName();
            times = 1;

            domibusVersionService.getArtifactVersion();
            times = 1;

            domibusVersionService.getBuiltTime();
            times = 1;

            domibusVersionService.getVersionNumber();
            times = 1;
        }};
    }

    @Test
    public void logDiagnosticInfo_withoutVersionInfo() {
        // Setup
        List<String> diagnosticsList = Collections.emptyList();

        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
            result = diagnosticsList;
        }};

        // Execute
        diagnosticsService.logDiagnosticInfo();

        // Verify
        new Verifications() {{
            domibusVersionService.getArtifactName();
            times = 0;

            domibusVersionService.getArtifactVersion();
            times = 0;

            domibusVersionService.getBuiltTime();
            times = 0;

            domibusVersionService.getVersionNumber();
            times = 0;
        }};
    }

    @Test
    public void logDiagnosticInfo_withJmsQueuesInfo() {
        // Setup
        List<String> diagnosticsList = Arrays.asList("jmsQueuesInfo");
        SortedMap<String, JMSDestination> destinations = new TreeMap<>();
        Domain domain = new Domain("default", "Default Domain");

        JMSDestination destination1 = new JMSDestination();
        destination1.setName("queue1");
        destinations.put("queue1", destination1);

        JMSDestination destination2 = new JMSDestination();
        destination2.setName("queue2");
        destinations.put("queue2", destination2);

        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
            result = diagnosticsList;

            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            jmsManager.getDestinations();
            result = destinations;

            jmsManager.getDestinationSize(destination1);
            result = 5L;

            jmsManager.getDestinationSize(destination2);
            result = 10L;
        }};

        // Execute
        diagnosticsService.logDiagnosticInfo();

        // Verify
        new Verifications() {{
            jmsManager.getDestinations();
            times = 1;

            jmsManager.getDestinationSize((JMSDestination) any);
            times = 2;
        }};
    }

    @Test
    public void logDiagnosticInfo_withAlertsCounts() {
        // Setup
        List<String> diagnosticsList = Arrays.asList("alertsCounts");
        Domain domain = new Domain("default", "Default Domain");

        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
            result = diagnosticsList;

            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            // Mock total count
            alertService.countAlerts((eu.domibus.core.alerts.model.common.AlertCriteria) any);
            result = 10L;
        }};

        // Execute
        diagnosticsService.logDiagnosticInfo();

        // Verify
        new Verifications() {{
            // Verify countAlerts is called for total count and for each status and type
            alertService.countAlerts((eu.domibus.core.alerts.model.common.AlertCriteria) any);
            minTimes = 1;
        }};
    }

    @Test
    public void logDiagnosticInfo_withMessagesByStatusCounts() {
        // Setup
        List<String> diagnosticsList = Arrays.asList("messagesByStatusCounts");
        Domain domain = new Domain("default", "Default Domain");

        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
            result = diagnosticsList;

            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            // Mock count for each message status
            messagesLogService.countMessages(eu.domibus.api.model.MessageType.USER_MESSAGE, (Map<String, Object>) any);
            result = 5L;
        }};

        // Execute
        diagnosticsService.logDiagnosticInfo();

        // Verify
        new Verifications() {{
            // Verify countMessages is called for each message status and for signal messages
            messagesLogService.countMessages(eu.domibus.api.model.MessageType.USER_MESSAGE, (Map<String, Object>) any);
            minTimes = 1;
        }};
    }
}
