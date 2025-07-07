package eu.domibus.core.diagnostics;

import eu.domibus.api.diagnostics.DiagnosticsService;
import eu.domibus.ext.domain.diagnostics.AlertsCountsDTO;
import eu.domibus.ext.domain.diagnostics.MessagesByStatusCountsDTO;
import eu.domibus.ext.domain.diagnostics.VersionInfoDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.AbstractIT;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
public class DiagnosticsServiceIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DiagnosticsServiceIT.class);

    @Autowired
    private DiagnosticsService diagnosticsService;

    @Test
    public void testGetVersionInfo() {
        // Get version information
        VersionInfoDTO versionInfo = diagnosticsService.getVersionInfo();
        
        // Verify the result is not null and contains expected data
        Assert.assertNotNull("Version info should not be null", versionInfo);
        Assert.assertNotNull("Artifact name should not be null", versionInfo.getArtifactName());
        Assert.assertNotNull("Artifact version should not be null", versionInfo.getArtifactVersion());
        
        LOG.info("Version info: {}", versionInfo);
    }

    @Test
    public void testGetAlertsCounts() {
        // Get alerts counts
        AlertsCountsDTO alertsCounts = diagnosticsService.getAlertsCounts();
        
        // Verify the result is not null
        Assert.assertNotNull("Alerts counts should not be null", alertsCounts);
        
        LOG.info("Alerts counts: {}", alertsCounts);
    }

    @Test
    public void testGetMessagesByStatusCounts() {
        // Get messages by status counts
        MessagesByStatusCountsDTO messagesByStatusCounts = diagnosticsService.getMessagesByStatusCounts();
        
        // Verify the result is not null
        Assert.assertNotNull("Messages by status counts should not be null", messagesByStatusCounts);
        
        LOG.info("Messages by status counts: {}", messagesByStatusCounts);
    }
}