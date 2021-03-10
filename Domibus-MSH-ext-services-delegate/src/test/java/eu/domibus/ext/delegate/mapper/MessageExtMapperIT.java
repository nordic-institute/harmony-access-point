package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.domain.MessageAttemptDTO;
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
public class MessageExtMapperIT {

    @Autowired
    private MessageExtMapper messageExtMapper;

    @Autowired
    private ObjectService objectService;

    @Test
    public void MessageAttemptToMessageAttemptDTO() {
        MessageAttemptDTO toConvert = (MessageAttemptDTO) objectService.createInstance(MessageAttemptDTO.class);
        final MessageAttempt converted = messageExtMapper.messageAttemptDTOToMessageAttempt(toConvert);
        final MessageAttemptDTO convertedBack = messageExtMapper.messageAttemptToMessageAttemptDTO(converted);
        
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void MessageAttemptDTOToMessageAttempt() {
        MessageAttempt toConvert = (MessageAttempt) objectService.createInstance(MessageAttempt.class);
        final MessageAttemptDTO converted = messageExtMapper.messageAttemptToMessageAttemptDTO(toConvert);
        final MessageAttempt convertedBack = messageExtMapper.messageAttemptDTOToMessageAttempt(converted);
        // these fields are missing in MessageAttemptDTO, fill them so the assertion works
        convertedBack.setId(toConvert.getId());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void MessageAcknowledgementToMessageAcknowledgementDTO() {
        MessageAcknowledgementDTO toConvert = (MessageAcknowledgementDTO) objectService.createInstance(MessageAcknowledgementDTO.class);
        final MessageAcknowledgement converted = messageExtMapper.messageAcknowledgementDTOToMessageAcknowledgement(toConvert);
        final MessageAcknowledgementDTO convertedBack = messageExtMapper.messageAcknowledgementToMessageAcknowledgementDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void MessageAcknowledgementDTOToMessageAcknowledgement() {
        MessageAcknowledgement toConvert = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);
        final MessageAcknowledgementDTO converted = messageExtMapper.messageAcknowledgementToMessageAcknowledgementDTO(toConvert);
        final MessageAcknowledgement convertedBack = messageExtMapper.messageAcknowledgementDTOToMessageAcknowledgement(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }
}