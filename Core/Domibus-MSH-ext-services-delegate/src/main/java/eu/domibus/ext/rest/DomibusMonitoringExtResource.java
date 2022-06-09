package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.monitoring.MonitoringInfoDTO;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.DomibusMonitoringExtService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Monitoring Domibus.
 * Checking status of DB, JMS Broker and Quartz Triggers)
 *
 * @author Soumya Chandran
 * @since 4.2
 */
@RestController
@RequestMapping(value = "/ext/monitoring/application")
@Tag(name = "status", description = "Domibus monitoring service API")
public class DomibusMonitoringExtResource {
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringExtResource.class);

    @Autowired
    DomibusMonitoringExtService domibusMonitoringExtService;

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(DomibusMonitoringExtException.class)
    public ResponseEntity<ErrorDTO> handleDomibusMonitoringExtException(DomibusMonitoringExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    /**
     * Get Monitoring Details by checking the DB, JMS Broker and Quarter Trigger based on the filters
     *
     * @param filters The filters for which monitoring status are retrieved
     * @return MonitoringInfoDTO with the status of monitoring services specific to the filter
     */
    @Operation(summary = "Check Domibus is Alive ", description = "Shows the accessibility and status of Domibus Database, JMS broker and Quartz Trigger",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(path = "status")
    public MonitoringInfoDTO getMonitoringDetails(@RequestParam(value = "filter", defaultValue = "all") List<String> filters) {
        LOG.debug("Getting Domibus status for the filters [{}]", filters);
        return domibusMonitoringExtService.getMonitoringDetails(filters);
    }
}




