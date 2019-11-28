package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.monitoring.*;
import eu.domibus.ext.domain.*;
import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@Mapper(componentModel = "spring")
@DecoratedWith(MonitoringMapperDecorator.class)
public interface MonitoringMapper {

    @Mapping(target = "services", ignore = true)
    DomibusMonitoringInfoDTO domibusMonitoringInfoToDomibusMonitoringInfoDTO(DomibusMonitoringInfo domibusMonitoringInfo);

    @Mapping(target = "services", ignore = true)
    DomibusMonitoringInfo domibusMonitoringInfoDTOToDomibusMonitoringInfo(DomibusMonitoringInfoDTO domibusMonitoringInfoDTO);

    DataBaseInfo dataBaseInfoDTOToDataBaseInfo(DataBaseInfoDTO dataBaseInfoDTO);

    DataBaseInfoDTO dataBaseInfoToDataBaseInfoDTO(DataBaseInfo dataBaseInfo);

    JmsBrokerInfo jmsBrokerInfoDTOToJmsBrokerInfo(JmsBrokerInfoDTO jmsBrokerInfoDTO);

    JmsBrokerInfoDTO jmsBrokerInfoToJmsBrokerInfoDTO(JmsBrokerInfo jmsBrokerInfo);

    @Mapping(source = "quartzTriggerDetails", target = "quartzInfoDetails")
    QuartzInfo quartzInfoDTOToQuartzInfo(QuartzInfoDTO quartzInfoDTO);

    @InheritInverseConfiguration
    QuartzInfoDTO quartzInfoToQuartzInfoDTO(QuartzInfo quartzInfo);

    QuartzInfoDetails quartzInfoDetailsDTOToQuartzInfoDetails(QuartzInfoDetailsDTO quartzInfoDetailsDTO);

    QuartzInfoDetailsDTO quartzInfoDetailsToQuartzInfoDetailsDTO(QuartzInfoDetails quartzInfoDetails);
}
