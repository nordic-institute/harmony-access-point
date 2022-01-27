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

import java.util.Collections;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMapperContextConfiguration.class)
public class MessageExtMapperTest {

    @Autowired
    private MessageExtMapper messageExtMapper;

    @Autowired
    private ObjectService objectService;

    @Test
    public void partiesToPartiesDTO() {
        MessageAcknowledgementDTO toConvert = (MessageAcknowledgementDTO) objectService.createInstance(MessageAcknowledgementDTO.class);
        final MessageAcknowledgement converted = messageExtMapper.messageAcknowledgementDTOToMessageAcknowledgement(toConvert);
        final List<MessageAcknowledgementDTO> convertedBack = messageExtMapper.messageAcknowledgementToMessageAcknowledgementDTO(Collections.singletonList(converted));

        objectService.assertObjects(convertedBack.get(0), toConvert);
    }

    @Test
    public void processListToProcessesDTO() {
        MessageAttemptDTO toConvert = (MessageAttemptDTO) objectService.createInstance(MessageAttemptDTO.class);
        final MessageAttempt converted = messageExtMapper.messageAttemptDTOToMessageAttempt(toConvert);
        final List<MessageAttemptDTO> convertedBack = messageExtMapper.messageAttemptToMessageAttemptDTO(Collections.singletonList(converted));

        objectService.assertObjects(convertedBack.get(0), toConvert);
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