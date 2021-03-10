package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.monitoring.domain.*;
import eu.domibus.ext.domain.monitoring.*;
import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper to generate Monitoring Service abstract class conversion methods
 * @author Soumya Chandran
 * @since 4.2
 */
@Mapper(componentModel = "spring")
@DecoratedWith(MonitoringExtMapperDecorator.class)
public interface MonitoringExtMapper {

    @Mapping(target = "services", ignore = true)
    MonitoringInfoDTO monitoringInfoToMonitoringInfoDTO(MonitoringInfo monitoringInfo);

    @Mapping(target = "services", ignore = true)
    MonitoringInfo monitoringInfoDTOToMonitoringInfo(MonitoringInfoDTO monitoringInfoDTO);

    DataBaseInfo dataBaseInfoDTOToDataBaseInfo(DataBaseInfoDTO dataBaseInfoDTO);

    DataBaseInfoDTO dataBaseInfoToDataBaseInfoDTO(DataBaseInfo dataBaseInfo);

    JmsBrokerInfo jmsBrokerInfoDTOToJmsBrokerInfo(JmsBrokerInfoDTO jmsBrokerInfoDTO);

    JmsBrokerInfoDTO jmsBrokerInfoToJmsBrokerInfoDTO(JmsBrokerInfo jmsBrokerInfo);

    @Mapping(source = "quartzTriggerInfos", target = "quartzTriggerDetails")
    QuartzInfo quartzInfoDTOToQuartzInfo(QuartzInfoDTO quartzInfoDTO);

    @InheritInverseConfiguration
    QuartzInfoDTO quartzInfoToQuartzInfoDTO(QuartzInfo quartzInfo);

    QuartzTriggerDetails quartzTriggerDetailsDTOToQuartzTriggerDetails(QuartzTriggerDetailsDTO quartzTriggerDetailsDTO);

    QuartzTriggerDetailsDTO quartzTriggerDetailsToQuartzTriggerDetailsDTO(QuartzTriggerDetails quartzTriggerDetails);
}
