package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.monitoring.domain.*;
import eu.domibus.ext.domain.monitoring.*;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMapperContextConfiguration.class)
public class MonitoringExtMapperIT {

    @Autowired
    private MonitoringExtMapper monitoringExtMapper;

    @Autowired
    private ObjectService objectService;

    @Test
    public void MonitoringInfoToMonitoringInfoDTO() {
        MonitoringInfoDTO toConvert = (MonitoringInfoDTO) objectService.createInstance(MonitoringInfoDTO.class);
        final MonitoringInfo converted = monitoringExtMapper.monitoringInfoDTOToMonitoringInfo(toConvert);
        final MonitoringInfoDTO convertedBack = monitoringExtMapper.monitoringInfoToMonitoringInfoDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void DataBaseInfoToDataBaseInfoDTO() {
        DataBaseInfoDTO toConvert = (DataBaseInfoDTO) objectService.createInstance(DataBaseInfoDTO.class);
        final DataBaseInfo converted = monitoringExtMapper.dataBaseInfoDTOToDataBaseInfo(toConvert);
        final DataBaseInfoDTO convertedBack = monitoringExtMapper.dataBaseInfoToDataBaseInfoDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void JmsBrokerInfoToJmsBrokerInfoDTO() {
        JmsBrokerInfoDTO toConvert = (JmsBrokerInfoDTO) objectService.createInstance(JmsBrokerInfoDTO.class);
        final JmsBrokerInfo converted = monitoringExtMapper.jmsBrokerInfoDTOToJmsBrokerInfo(toConvert);
        final JmsBrokerInfoDTO convertedBack = monitoringExtMapper.jmsBrokerInfoToJmsBrokerInfoDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void QuartzInfoToQuartzInfoDTO() {
        QuartzInfoDTO toConvert = (QuartzInfoDTO) objectService.createInstance(QuartzInfoDTO.class);
        final QuartzInfo converted = monitoringExtMapper.quartzInfoDTOToQuartzInfo(toConvert);
        final QuartzInfoDTO convertedBack = monitoringExtMapper.quartzInfoToQuartzInfoDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void QuartzTriggerDetailsToQuartzTriggerDetailsDTO() {
        QuartzTriggerDetailsDTO toConvert = (QuartzTriggerDetailsDTO) objectService.createInstance(QuartzTriggerDetailsDTO.class);
        final QuartzTriggerDetails converted = monitoringExtMapper.quartzTriggerDetailsDTOToQuartzTriggerDetails(toConvert);
        final QuartzTriggerDetailsDTO convertedBack = monitoringExtMapper.quartzTriggerDetailsToQuartzTriggerDetailsDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

}