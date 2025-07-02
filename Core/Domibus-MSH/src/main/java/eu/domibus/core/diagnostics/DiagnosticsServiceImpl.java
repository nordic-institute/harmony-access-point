package eu.domibus.core.diagnostics;

import eu.domibus.api.server.ServerInfoService;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
@Service
public class DiagnosticsServiceImpl implements DiagnosticsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DiagnosticsServiceImpl.class);

    @Autowired
    private DomibusVersionService domibusVersionService;

    @Autowired
    private ServerInfoService serverInfoService;

    public void logDiagnosticInfo() {
        LOG.info("Running Domibus diagnostics");

        logVersionInfo();

        LOG.info("Diagnostics completed");
    }

    private void logVersionInfo() {
        LOG.info("Artifact Name: {}", domibusVersionService.getArtifactName());
        LOG.info("Artifact Version: {}", domibusVersionService.getArtifactVersion());
        LOG.info("Build Time: {}", domibusVersionService.getBuiltTime());
        LOG.info("Version Number: {}", domibusVersionService.getVersionNumber());

        LOG.info("Server name: {}", serverInfoService.getServerName());
    }
}
