package eu.domibus.ext.rest;

import eu.domibus.ext.domain.*;
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
 * Monitoring Domibus is Alive.
 * Checking status of DB, JMS Broker and Quartz Triggers)
 * Created by Soumya Chandran
 */

@RestController
@RequestMapping(value = "/ext/monitoring/application")
public class DomibusMonitoringResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringResource.class);

    @Autowired
    DomibusMonitoringExtService domibusMonitoringExtService;
    /**
     * Rest method to do Domibus monitoring.  Checking Database , JMS Broker accessibility and Quartz Triggers status.
     *
     * @return  DomibusMonitoringInfoDTO
     */
    @ApiOperation(value = "Check Domibus is Alive ", notes = "Show the accessibility and status of Domibus Database, JMS broker and Quartz Trigger",
            authorizations = @Authorization(value = "basicAuth"), tags = "status")
    @RequestMapping(path = "status", method = RequestMethod.GET)
    public DomibusMonitoringInfoDTO getDomibusStatus(@RequestParam(value = "filter", defaultValue = "all") List<String> filters) throws DomibusMonitoringExtException {
        LOG.debug("Getting Domibus status for the filters [{}]", filters);
        return domibusMonitoringExtService.getDomibusStatus(filters);
    }
}




