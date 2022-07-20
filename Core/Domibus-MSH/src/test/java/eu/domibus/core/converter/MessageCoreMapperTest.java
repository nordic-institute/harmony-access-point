package eu.domibus.core.converter;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.*;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.web.rest.ro.MessageLogRO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author François Gautier
 * @since 5.0
 */
public class MessageCoreMapperTest extends AbstractMapperTest {

    @Autowired
    private MessageCoreMapper messageCoreMapper;

    @Test
    public void convertMessageAttempt() {
        MessageAttempt toConvert = (MessageAttempt) objectService.createInstance(MessageAttempt.class);
        final MessageAttemptEntity converted = messageCoreMapper.messageAttemptToMessageAttemptEntity(toConvert);
        final MessageAttempt convertedBack = messageCoreMapper.messageAttemptEntityToMessageAttempt(converted);

        convertedBack.setUserMessageEntityId(toConvert.getUserMessageEntityId());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertMessageLog() {
        MessageLogRO toConvert = (MessageLogRO) objectService.createInstance(MessageLogRO.class);
        final MessageLogInfo converted = messageCoreMapper.messageLogROToMessageLogInfo(toConvert);
        final MessageLogRO convertedBack = messageCoreMapper.messageLogInfoToMessageLogRO(converted);
        convertedBack.setCanDownloadMessage(toConvert.getCanDownloadMessage());
        convertedBack.setCanDownloadEnvelope(toConvert.getCanDownloadEnvelope());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertUserMessage() {
        UserMessage toConvert = (UserMessage) objectService.createInstance(UserMessage.class);
        MSHRoleEntity roleE = new MSHRoleEntity();
        roleE.setRole(MSHRole.SENDING);
        toConvert.setMshRole(roleE);
        final eu.domibus.api.usermessage.domain.UserMessage converted = messageCoreMapper.userMessageToUserMessageApi(toConvert);
        final UserMessage convertedBack = messageCoreMapper.userMessageApiToUserMessage(converted);

        convertedBack.setSourceMessage(toConvert.isSourceMessage());
        convertedBack.setMessageFragment(toConvert.isMessageFragment());
        convertedBack.setPartyInfo(toConvert.getPartyInfo());
        convertedBack.setEntityId(toConvert.getEntityId());

        From fromToConvert = (From) getPartyIdProperty(toConvert, "from");
        To toToConvert = (To) getPartyIdProperty(toConvert, "to");

        From fromConvertedBack = (From) getPartyIdProperty(convertedBack, "from");
        To toConvertedBack = (To) getPartyIdProperty(convertedBack, "to");

        fromConvertedBack.getFromPartyId().setEntityId(fromToConvert.getFromPartyId().getEntityId());
        fromConvertedBack.getFromRole().setEntityId(fromToConvert.getFromRole().getEntityId());
        toConvertedBack.getToPartyId().setEntityId(toToConvert.getToPartyId().getEntityId());
        toConvertedBack.getToRole().setEntityId(toToConvert.getToRole().getEntityId());
        convertedBack.getMpc().setEntityId(toConvert.getMpc().getEntityId());

        MessageProperty messagePropertyConvertedBack = getAnyMessageProperty(convertedBack);
        MessageProperty messagePropertyToConvert = getAnyMessageProperty(toConvert);
        messagePropertyConvertedBack.setEntityId(messagePropertyToConvert.getEntityId());

        convertedBack.getService().setEntityId(toConvert.getService().getEntityId());
        convertedBack.getAgreementRef().setEntityId(toConvert.getAgreementRef().getEntityId());
        convertedBack.getAction().setEntityId(toConvert.getAction().getEntityId());
        convertedBack.setTestMessage(toConvert.isTestMessage());

        objectService.assertObjects(convertedBack, toConvert);
    }

    private Object getPartyIdProperty(UserMessage toConvert, String from) {
        return ReflectionTestUtils.getField(toConvert.getPartyInfo(), from);
    }

    private MessageProperty getAnyMessageProperty(UserMessage convertedBack) {
        return convertedBack.getMessageProperties()
                .stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Should not produce an NPE"));
    }

}
