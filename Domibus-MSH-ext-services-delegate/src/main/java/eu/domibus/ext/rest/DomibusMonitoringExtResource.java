package eu.domibus.ext.rest;

import eu.domibus.ext.domain.monitoring.MonitoringInfoDTO;
import eu.domibus.ext.services.DomibusMonitoringExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
public class DomibusMonitoringExtResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringExtResource.class);

    @Autowired
    DomibusMonitoringExtService domibusMonitoringExtService;

    /**
     * Get Monitoring Details by checking the DB, JMS Broker and Quarter Trigger based on the filters
     *
     * @param filters The filters for which monitoring status are retrieved
     * @return MonitoringInfoDTO with the status of monitoring services specific to the filter
     */
    @ApiOperation(value = "Check Domibus is Alive ", notes = "Shows the accessibility and status of Domibus Database, JMS broker and Quartz Trigger",
            authorizations = @Authorization(value = "basicAuth"), tags = "status")
    @GetMapping(path = "status")
    public MonitoringInfoDTO getMonitoringDetails(@RequestParam(value = "filter", defaultValue = "all") List<String> filters) {
        LOG.debug("Getting Domibus status for the filters [{}]", filters);
        return domibusMonitoringExtService.getMonitoringDetails(filters);
    }
}




