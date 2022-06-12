package eu.domibus.ext.services;

import eu.domibus.ext.domain.monitoring.MonitoringInfoDTO;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;

import java.util.List;

/**
 * All Operations related to monitoring
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public interface DomibusMonitoringExtService {
    /**
     * @param filters The filters for which monitoring details are retrieved
     * @return MonitoringInfoDTO with the details of monitoring services specific to the filter
     * @throws DomibusMonitoringExtException Raised in case an exception occurs while trying to get the monitoring details {@link DomibusMonitoringExtException}
     */
    MonitoringInfoDTO getMonitoringDetails(List<String> filters) throws DomibusMonitoringExtException;


}
