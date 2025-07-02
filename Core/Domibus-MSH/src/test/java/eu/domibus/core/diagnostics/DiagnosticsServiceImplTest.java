package eu.domibus.core.diagnostics;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
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

    @Test
    public void logDiagnosticInfo_withVersionInfo() {
        // Setup
        List<String> diagnosticsList = Arrays.asList("versionInfo");

        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
            result = diagnosticsList;

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

        JMSDestination destination1 = new JMSDestination();
        destination1.setName("queue1");
        destinations.put("queue1", destination1);

        JMSDestination destination2 = new JMSDestination();
        destination2.setName("queue2");
        destinations.put("queue2", destination2);

        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
            result = diagnosticsList;

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
}
