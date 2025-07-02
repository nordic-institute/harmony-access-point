package eu.domibus.core.diagnostics;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.core.property.DomibusVersionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private ServerInfoService serverInfoService;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

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

            serverInfoService.getServerName();
            result = "TestServer";
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

            serverInfoService.getServerName();
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

            serverInfoService.getServerName();
            times = 0;
        }};
    }
}