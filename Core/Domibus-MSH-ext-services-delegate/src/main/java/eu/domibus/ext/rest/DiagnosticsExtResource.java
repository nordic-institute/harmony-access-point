package eu.domibus.ext.rest;

import eu.domibus.api.diagnostics.DiagnosticsService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.ext.domain.diagnostics.AlertsCountsDTO;
import eu.domibus.ext.domain.diagnostics.JmsQueuesInfoDTO;
import eu.domibus.ext.domain.diagnostics.MessagesByStatusCountsDTO;
import eu.domibus.ext.domain.diagnostics.VersionInfoDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
@RestController
@RequestMapping(value = "/ext/diagnostics")
@Tag(name = "diagnostics", description = "Domibus diagnostics service API")
public class DiagnosticsExtResource {
    // TODO IB add changes.txt
    // TODO IB add documentation
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DiagnosticsExtResource.class);

    @Autowired
    private DiagnosticsService diagnosticsService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    /**
     * Get version information
     *
     * @return VersionInfoDTO with version information
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get version information", description = "Returns version information about the application",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @ApiResponse(responseCode = "403", description = "Admin role needed")
    @GetMapping(path = "version")
    public VersionInfoDTO getVersionInfo() {
        LOG.debug("Getting version information");
        return diagnosticsService.getVersionInfo();
    }

    /**
     * Get JMS queues information
     *
     * @return JmsQueuesInfoDTO with JMS queues information
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get JMS queues information", description = "Returns information about JMS queues",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @ApiResponse(responseCode = "403", description = "Admin role needed")
    @GetMapping(path = "jmsQueuesInfo")
    public JmsQueuesInfoDTO getJmsQueuesInfo() {
        LOG.debug("Getting JMS queues information");
        return diagnosticsService.getJmsQueuesInfo();
    }

    /**
     * Get alerts counts
     *
     * @return AlertsCountsDTO with alerts counts
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get alerts counts", description = "Returns counts of alerts by status and type",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @ApiResponse(responseCode = "403", description = "Admin role needed")
    @GetMapping(path = "alerts")
    public AlertsCountsDTO getAlertsCounts() {
        LOG.debug("Getting alerts counts");
        return diagnosticsService.getAlertsCounts();
    }

    /**
     * Get messages by status counts
     *
     * @return MessagesByStatusCountsDTO with messages by status counts
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get messages by status counts", description = "Returns counts of messages by status",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @ApiResponse(responseCode = "403", description = "Admin role needed")
    @GetMapping(path = "messages")
    public MessagesByStatusCountsDTO getMessagesByStatusCounts() {
        LOG.debug("Getting messages by status counts");
        return diagnosticsService.getMessagesByStatusCounts();
    }
}
