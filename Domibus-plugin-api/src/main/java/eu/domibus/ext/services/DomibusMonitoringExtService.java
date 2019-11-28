package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomibusMonitoringInfoDTO;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;

import java.util.List;
/**
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public interface DomibusMonitoringExtService {

     DomibusMonitoringInfoDTO getDomibusStatus(List<String> filters) throws DomibusMonitoringExtException;


}
