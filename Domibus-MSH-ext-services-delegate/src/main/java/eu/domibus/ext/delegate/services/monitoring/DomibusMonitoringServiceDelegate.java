package eu.domibus.ext.delegate.services.monitoring;

import eu.domibus.api.monitoring.DomibusMonitoringService;
import eu.domibus.api.monitoring.domain.MonitoringInfo;
import eu.domibus.ext.delegate.mapper.MonitoringExtMapper;
import eu.domibus.ext.domain.monitoring.MonitoringInfoDTO;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import eu.domibus.ext.services.DomibusMonitoringExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Get Monitoring Details based on the filters
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
@Service
public class DomibusMonitoringServiceDelegate implements DomibusMonitoringExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringServiceDelegate.class);
    final DomibusMonitoringService domibusMonitoringService;

    final MonitoringExtMapper monitoringExtMapper;

    public DomibusMonitoringServiceDelegate(DomibusMonitoringService domibusMonitoringService, MonitoringExtMapper monitoringExtMapper) {
        this.domibusMonitoringService = domibusMonitoringService;
        this.monitoringExtMapper = monitoringExtMapper;
    }

    /**
     * Get Monitoring Details by checking the DB, JMS Broker and Quarter Trigger based on the filters
     *
     * @param filters The filters for which monitoring status are retrieved
     * @return MonitoringInfoDTO with the status of monitoring services specific to the filter
     * @throws DomibusMonitoringExtException Raised in case an exception occurs while trying to get the monitoring details {@link DomibusMonitoringExtException}
     */
    public MonitoringInfoDTO getMonitoringDetails(List<String> filters) throws DomibusMonitoringExtException {
        LOG.debug("Domibus IsAlive Service filtering status of [{}]", filters);
        MonitoringInfo monitoringInfo = domibusMonitoringService.getMonitoringDetails(filters);
        if (monitoringInfo == null) {
            return null;
        }
        LOG.debug("Domibus Monitoring Info [{}]", monitoringInfo);
        return monitoringExtMapper.monitoringInfoToMonitoringInfoDTO(monitoringInfo);
    }
}


