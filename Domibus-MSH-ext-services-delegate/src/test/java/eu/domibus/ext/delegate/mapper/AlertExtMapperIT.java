package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.ext.domain.AlertEventDTO;
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
@ContextConfiguration(classes = MapperContextConfiguration.class)
public class AlertExtMapperIT {

    @Autowired
    private AlertExtMapper alertExtMapper;

    @Autowired
    private ObjectService objectService;

    @Test
    public void alertEventToAlertEventDTO() {
        AlertEventDTO toConvert = (AlertEventDTO) objectService.createInstance(AlertEventDTO.class);
        final AlertEvent converted = alertExtMapper.alertEventDTOToAlertEvent(toConvert);
        final AlertEventDTO convertedBack = alertExtMapper.alertEventToAlertEventDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void alertEventDTOToAlertEvent() {
        AlertEvent toConvert = (AlertEvent) objectService.createInstance(AlertEvent.class);
        final AlertEventDTO converted = alertExtMapper.alertEventToAlertEventDTO(toConvert);
        final AlertEvent convertedBack = alertExtMapper.alertEventDTOToAlertEvent(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }
}