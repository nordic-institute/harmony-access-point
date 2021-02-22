package eu.domibus.ext.delegate.converter;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.ext.delegate.mapper.MonitoringMapperDecorator;
import eu.domibus.ext.domain.AlertEventDTO;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.domain.PasswordEncryptionResultDTO;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class DomainExtDefaultConverterIT {

    @Configuration
    @ComponentScan(basePackageClasses = {MessageAcknowledgement.class, MessageAcknowledgementDTO.class, DomainExtDefaultConverter.class,  MonitoringMapperDecorator.class})
    @ImportResource({
            "classpath:config/commonsTestContext.xml"
    })
    static class ContextConfiguration {

    }
    @Autowired
    DomainExtConverter domibusDomainConverter;

    @Autowired
    ObjectService objectService;

    @Test
    public void testConvertMessageAcknowledge()  {
        MessageAcknowledgement toConvert = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);
        final MessageAcknowledgementDTO converted = domibusDomainConverter.convert(toConvert, MessageAcknowledgementDTO.class);
        objectService.assertObjects(converted, toConvert);
    }

    @Test
    public void testConvertAlertEvent()  {
        AlertEvent toConvert = (AlertEvent) objectService.createInstance(AlertEvent.class);
        final AlertEventDTO converted = domibusDomainConverter.convert(toConvert, AlertEventDTO.class);
        objectService.assertObjects(converted.getAlertLevelDTO().name(), toConvert.getAlertLevel().name());
        objectService.assertObjects(converted.getProperties(), toConvert.getProperties());
    }

    @Test
    public void testConvertAlertEventDTO()  {
        AlertEventDTO toConvert = (AlertEventDTO) objectService.createInstance(AlertEventDTO.class);
        final AlertEvent converted = domibusDomainConverter.convert(toConvert, AlertEvent.class);
        objectService.assertObjects(converted.getAlertLevel(), toConvert.getAlertLevelDTO());
        objectService.assertObjects(converted.getProperties(), toConvert.getProperties());
    }

    @Test
    public void testConvertMessageAcknowledgeList()  {
        MessageAcknowledgement toConvert1 = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);
        MessageAcknowledgement toConvert2 = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);

        List<MessageAcknowledgement> toConvertList = new ArrayList<>();
        toConvertList.add(toConvert1);
        toConvertList.add(toConvert2);

        final List<MessageAcknowledgementDTO> convertedList = domibusDomainConverter.convert(toConvertList, MessageAcknowledgementDTO.class);
        objectService.assertObjects(convertedList, toConvertList);
    }

    @Test
    public void testConvertMessageAttempt()  {
        MessageAttempt toConvert = (MessageAttempt) objectService.createInstance(MessageAttempt.class);
        final MessageAttemptDTO converted = domibusDomainConverter.convert(toConvert, MessageAttemptDTO.class);
        objectService.assertObjects(converted, toConvert);
    }

    @Test
    public void testPasswordEncryptionResult()  {
        PasswordEncryptionResult toConvert = (PasswordEncryptionResult) objectService.createInstance(PasswordEncryptionResult.class);
        final PasswordEncryptionResultDTO converted = domibusDomainConverter.convert(toConvert, PasswordEncryptionResultDTO.class);
        objectService.assertObjects(converted, toConvert);
    }

    @Test
    public void testtConvertMessageAttemptList()  {
        MessageAttempt toConvert1 = (MessageAttempt) objectService.createInstance(MessageAttempt.class);
        MessageAttempt toConvert2 = (MessageAttempt) objectService.createInstance(MessageAttempt.class);

        List<MessageAttempt> toConvertList = new ArrayList<>();
        toConvertList.add(toConvert1);
        toConvertList.add(toConvert2);


        final List<MessageAttemptDTO> convertedList = domibusDomainConverter.convert(toConvertList, MessageAttemptDTO.class);
        objectService.assertObjects(convertedList, toConvertList);
    }
}
