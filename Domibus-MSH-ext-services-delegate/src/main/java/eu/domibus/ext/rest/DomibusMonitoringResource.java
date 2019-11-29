package eu.domibus.ext.rest;

import eu.domibus.ext.domain.monitoring.MonitoringInfoDTO;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import eu.domibus.ext.services.DomibusMonitoringExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Monitoring Domibus.
 * Checking status of DB, JMS Broker and Quartz Triggers)
 * @author Soumya Chandran
 */
@RestController
@RequestMapping(value = "/ext/monitoring/application")
public class DomibusMonitoringResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringResource.class);

    @Autowired
    DomibusMonitoringExtService domibusMonitoringExtService;

    /**
     * Get Monitoring Details by checking the DB, JMS Broker and Quarter Trigger based on the filters
     *
     * @param filters The filters for which monitoring status are retrieved
     * @return MonitoringInfoDTO with the status of monitoring services specific to the filter
     * @throws DomibusMonitoringExtException Raised in case an exception occurs while trying to get the monitoring details {@link DomibusMonitoringExtException}
     */
    @ApiOperation(value = "Check Domibus is Alive ", notes = "Show the accessibility and status of Domibus Database, JMS broker and Quartz Trigger",
            authorizations = @Authorization(value = "basicAuth"), tags = "status")
    @RequestMapping(path = "status", method = RequestMethod.GET)
    public MonitoringInfoDTO getMonitoringDetails(@RequestParam(value = "filter", defaultValue = "all") List<String> filters) throws DomibusMonitoringExtException {
        LOG.debug("Getting Domibus status for the filters [{}]", filters);
        return domibusMonitoringExtService.getMonitoringDetails(filters);
    }
}




