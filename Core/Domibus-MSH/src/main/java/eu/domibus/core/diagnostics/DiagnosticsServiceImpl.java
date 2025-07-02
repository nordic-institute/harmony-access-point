package eu.domibus.core.diagnostics;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
@Service
public class DiagnosticsServiceImpl implements DiagnosticsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DiagnosticsServiceImpl.class);

    private static final String VERSION_INFO = "versionInfo";
    private static final String JMS_QUEUES_INFO = "jmsQueuesInfo";

    @Autowired
    private DomibusVersionService domibusVersionService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private JMSManager jmsManager;

    public void logDiagnosticInfo() {
        LOG.info("Running Domibus diagnostics");

        // TODO IB does this works ok for domain?
        List<String> diagnosticsList = domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
        if (diagnosticsList.contains(VERSION_INFO)) {
            logVersionInfo();
        }

        if (diagnosticsList.contains(JMS_QUEUES_INFO)) {
            logJmsQueuesInfo();
        }

        LOG.info("Diagnostics completed");
    }

    private void logVersionInfo() {
        LOG.info("Artifact Name: {}", domibusVersionService.getArtifactName());
        LOG.info("Artifact Version: {}", domibusVersionService.getArtifactVersion());
        LOG.info("Build Time: {}", domibusVersionService.getBuiltTime());
        LOG.info("Version Number: {}", domibusVersionService.getVersionNumber());
    }

    private void logJmsQueuesInfo() {
        LOG.info("JMS Queues Information:");
        Map<String, JMSDestination> destinations = jmsManager.getDestinations();
        for (Map.Entry<String, JMSDestination> entry : destinations.entrySet()) {
            JMSDestination destination = entry.getValue();
            long size = jmsManager.getDestinationSize(destination);
            LOG.info("Queue: {}, Size: {}", destination.getName(), size);
        }
    }
}
