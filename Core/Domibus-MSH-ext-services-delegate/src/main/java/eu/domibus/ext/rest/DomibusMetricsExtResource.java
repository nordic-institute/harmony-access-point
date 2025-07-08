package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.MetricsExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Monitoring Domibus.
 * Checking status of DB, JMS Broker and Quartz Triggers)
 *
 * @author Soumya Chandran
 * @since 4.2
 */
@RestController
@RequestMapping(value = "/ext/metrics")
@Tag(name = "metrics", description = "Domibus metrics service API")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
public class DomibusMetricsExtResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMetricsExtResource.class);

    private final MetricsExtService metricsExtService;

    private final ExtExceptionHelper extExceptionHelper;

    public DomibusMetricsExtResource(MetricsExtService metricsExtService, ExtExceptionHelper extExceptionHelper) {
        this.metricsExtService = metricsExtService;
        this.extExceptionHelper = extExceptionHelper;
    }

    @ExceptionHandler(DomibusMonitoringExtException.class)
    public ResponseEntity<ErrorDTO> handleDomibusMonitoringExtException(DomibusMonitoringExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    /**
     * return the Domibus Metric Registry
     */
    @Operation(summary = "Metrics Domibus ", description = "Get Domibus Metric Registry",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @ApiResponse(responseCode = "403", description = "Admin role needed")
    @GetMapping(path = "metrics")
    public ResponseEntity<Object> getDomibusMetrics() {
        LOG.debug("Getting Domibus metrics");
        return ResponseEntity.ok(metricsExtService.getMetricRegistry());
    }
}
