package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.monitoring.domain.*;
import eu.domibus.ext.domain.monitoring.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitoring Mapper Decorator is for the abstract class and override the methods of the monitoring mapper which it decorates.
 *
 * @author Soumya Chandran
 * @since 4.2
 */
public abstract class MonitoringMapperDecorator implements MonitoringMapper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MonitoringMapperDecorator.class);

    @Autowired
    @Qualifier("delegate")
    protected MonitoringMapper delegate;

    public MonitoringMapperDecorator() {
    }

    @Override
    public MonitoringInfoDTO monitoringInfoToMonitoringInfoDTO(MonitoringInfo monitoringInfo) {
        MonitoringInfoDTO monitoringInfoDTO = delegate.monitoringInfoToMonitoringInfoDTO(monitoringInfo);
        List<ServiceInfoDTO> servicesList = new ArrayList<>();
        for (ServiceInfo serviceInfo : monitoringInfo.getServices()) {
            servicesList.add(convert(serviceInfo));
        }
        monitoringInfoDTO.setServices(servicesList);
        return monitoringInfoDTO;
    }

    @Override
    public MonitoringInfo monitoringInfoDTOToMonitoringInfo(MonitoringInfoDTO monitoringInfoDTO) {
        MonitoringInfo monitoringInfo = delegate.monitoringInfoDTOToMonitoringInfo(monitoringInfoDTO);
        List<ServiceInfo> servicesList = new ArrayList<>();
        for (ServiceInfoDTO serviceInfo : monitoringInfoDTO.getServices()) {
            servicesList.add(convert(serviceInfo));
        }
        monitoringInfo.setServices(servicesList);
        return monitoringInfo;
    }

    protected ServiceInfoDTO convert(ServiceInfo serviceInfo) {
        LOG.debug("ServiceInfo convert: [{}]", serviceInfo.getClass());
        if (serviceInfo instanceof DataBaseInfo) {
            return delegate.dataBaseInfoToDataBaseInfoDTO((DataBaseInfo) serviceInfo);
        }
        if (serviceInfo instanceof JmsBrokerInfo) {
            return delegate.jmsBrokerInfoToJmsBrokerInfoDTO((JmsBrokerInfo) serviceInfo);
        }
        if (serviceInfo instanceof QuartzInfo) {
            return delegate.quartzInfoToQuartzInfoDTO((QuartzInfo) serviceInfo);
        }
        LOG.warn("Invalid type for ServiceInfo: [{}]", serviceInfo.getClass());
        return null;
    }

    protected ServiceInfo convert(ServiceInfoDTO serviceInfoDTO) {
        LOG.debug("ServiceInfoDTO convert: [{}]", serviceInfoDTO.getClass());
        if (serviceInfoDTO instanceof DataBaseInfoDTO) {
            return delegate.dataBaseInfoDTOToDataBaseInfo((DataBaseInfoDTO) serviceInfoDTO);
        }
        if (serviceInfoDTO instanceof JmsBrokerInfoDTO) {
            return delegate.jmsBrokerInfoDTOToJmsBrokerInfo((JmsBrokerInfoDTO) serviceInfoDTO);
        }
        if (serviceInfoDTO instanceof QuartzInfoDTO) {
            return delegate.quartzInfoDTOToQuartzInfo((QuartzInfoDTO) serviceInfoDTO);
        }
        LOG.warn("Invalid type for ServiceInfoDTO: [{}]", serviceInfoDTO.getClass());
        return null;
    }
}



