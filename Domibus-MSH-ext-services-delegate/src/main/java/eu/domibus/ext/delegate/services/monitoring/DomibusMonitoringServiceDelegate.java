package eu.domibus.ext.delegate.services.monitoring;

import eu.domibus.api.monitoring.DomibusMonitoringInfo;
import eu.domibus.api.monitoring.DomibusMonitoringService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.*;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import eu.domibus.ext.services.DomibusMonitoringExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
@Service
public class DomibusMonitoringServiceDelegate implements DomibusMonitoringExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringServiceDelegate.class);
    @Autowired
    DomibusMonitoringService domibusMonitoringService;

    @Autowired
    DomainExtConverter domainConverter;


    public DomibusMonitoringInfoDTO getDomibusStatus(List<String> filters) throws DomibusMonitoringExtException {
        LOG.debug("Domibus IsAlive Service filtering status of [{}]", filters);
        DomibusMonitoringInfo monitoringInfo = domibusMonitoringService.getDomibusStatus(filters);
        if (monitoringInfo == null) {
            return null;
        }
        LOG.debug("Domibus Monitoring Info [{}]", monitoringInfo);
        return domainConverter.convert(monitoringInfo, DomibusMonitoringInfoDTO.class);
    }
}


