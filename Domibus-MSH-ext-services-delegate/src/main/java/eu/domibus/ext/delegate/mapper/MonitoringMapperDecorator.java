package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.monitoring.*;
import eu.domibus.ext.domain.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
/**
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
    public DomibusMonitoringInfoDTO domibusMonitoringInfoToDomibusMonitoringInfoDTO(DomibusMonitoringInfo domibusMonitoringInfo) {
        DomibusMonitoringInfoDTO domibusMonitoringInfoDTO = delegate.domibusMonitoringInfoToDomibusMonitoringInfoDTO(domibusMonitoringInfo);
        List<ServiceInfoDTO> servicesList = new ArrayList<>();
        for (ServiceInfo serviceInfo : domibusMonitoringInfo.getServices()) {
            servicesList.add(convert(serviceInfo));
        }
        domibusMonitoringInfoDTO.setServices(servicesList);
        return domibusMonitoringInfoDTO;
    }

    @Override
    public DomibusMonitoringInfo domibusMonitoringInfoDTOToDomibusMonitoringInfo(DomibusMonitoringInfoDTO domibusMonitoringInfoDTO) {
        DomibusMonitoringInfo domibusMonitoringInfo = delegate.domibusMonitoringInfoDTOToDomibusMonitoringInfo(domibusMonitoringInfoDTO);
        List<ServiceInfo> servicesList = new ArrayList<>();
        for (ServiceInfoDTO serviceInfo : domibusMonitoringInfoDTO.getServices()) {
            servicesList.add(convert(serviceInfo));
        }
        domibusMonitoringInfo.setServices(servicesList);
        return domibusMonitoringInfo;
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
            // delegate.quartzInfoDetailsDTOToQuartzInfoDetails()
            return delegate.quartzInfoDTOToQuartzInfo((QuartzInfoDTO) serviceInfoDTO);
        }
        LOG.warn("Invalid type for ServiceInfoDTO: [{}]", serviceInfoDTO.getClass());
        return null;
    }
}



