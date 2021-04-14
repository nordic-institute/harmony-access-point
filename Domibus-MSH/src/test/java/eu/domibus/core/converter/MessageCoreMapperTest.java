package eu.domibus.core.converter;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.*;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.core.replication.UIMessageDiffEntity;
import eu.domibus.core.replication.UIMessageEntity;
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

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertMessageLog() {
        MessageLogRO toConvert = (MessageLogRO) objectService.createInstance(MessageLogRO.class);
        final MessageLogInfo converted = messageCoreMapper.messageLogROToMessageLogInfo(toConvert);
        final MessageLogRO convertedBack = messageCoreMapper.messageLogInfoToMessageLogRO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertUIMessageEntity() {
        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
        final UIMessageDiffEntity converted = messageCoreMapper.uiMessageEntityToUIMessageDiffEntity(toConvert);
        final UIMessageEntity convertedBack = messageCoreMapper.uiMessageDiffEntityToUIMessageEntity(converted);
        convertedBack.setLastModified(toConvert.getLastModified());
        convertedBack.setEntityId(toConvert.getEntityId());
        convertedBack.setAction(toConvert.getAction());
        convertedBack.setServiceType(toConvert.getServiceType());
        convertedBack.setServiceValue(toConvert.getServiceValue());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertUIMessageEntityMessageLog() {
        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
        final MessageLogRO converted = messageCoreMapper.uiMessageEntityToMessageLogRO(toConvert);
        final UIMessageEntity convertedBack = messageCoreMapper.messageLogROToUIMessageEntity(converted);
        convertedBack.setLastModified(toConvert.getLastModified());
        convertedBack.setEntityId(toConvert.getEntityId());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertUserMessage() {
        UserMessage toConvert = (UserMessage) objectService.createInstance(UserMessage.class);
        final eu.domibus.api.usermessage.domain.UserMessage converted = messageCoreMapper.userMessageToUserMessageApi(toConvert);
        final UserMessage convertedBack = messageCoreMapper.userMessageApiToUserMessage(converted);

        convertedBack.setSourceMessage(toConvert.isSourceMessage());
        convertedBack.setMessageFragment(toConvert.isMessageFragment());
        convertedBack.setPartInfoList(toConvert.getPartInfoList());
        convertedBack.setEntityId(toConvert.getEntityId());
        convertedBack.setCreationTime(toConvert.getCreationTime());
        convertedBack.setModificationTime(toConvert.getModificationTime());
        convertedBack.setCreatedBy(toConvert.getCreatedBy());
        convertedBack.setModifiedBy(toConvert.getModifiedBy());

        From fromToConvert = (From) getPartyIdProperty(toConvert, "from");
        To toToConvert = (To) getPartyIdProperty(toConvert, "to");

        From fromConvertedBack = (From) getPartyIdProperty(convertedBack, "from");
        To toConvertedBack = (To) getPartyIdProperty(convertedBack, "to");

        fromConvertedBack.getPartyId().setEntityId(fromToConvert.getPartyId().getEntityId());
        fromConvertedBack.getPartyId().setCreationTime(fromToConvert.getPartyId().getCreationTime());
        fromConvertedBack.getPartyId().setModificationTime(fromToConvert.getPartyId().getModificationTime());
        fromConvertedBack.getPartyId().setCreatedBy(fromToConvert.getPartyId().getCreatedBy());
        fromConvertedBack.getPartyId().setModifiedBy(fromToConvert.getPartyId().getModifiedBy());

        fromConvertedBack.getRole().setEntityId(fromToConvert.getRole().getEntityId());
        fromConvertedBack.getRole().setCreationTime(fromToConvert.getRole().getCreationTime());
        fromConvertedBack.getRole().setModificationTime(fromToConvert.getRole().getModificationTime());
        fromConvertedBack.getRole().setCreatedBy(fromToConvert.getRole().getCreatedBy());
        fromConvertedBack.getRole().setModifiedBy(fromToConvert.getRole().getModifiedBy());

        toConvertedBack.getPartyId().setEntityId(toToConvert.getPartyId().getEntityId());
        toConvertedBack.getPartyId().setCreationTime(toToConvert.getPartyId().getCreationTime());
        toConvertedBack.getPartyId().setModificationTime(toToConvert.getPartyId().getModificationTime());
        toConvertedBack.getPartyId().setCreatedBy(toToConvert.getPartyId().getCreatedBy());
        toConvertedBack.getPartyId().setModifiedBy(toToConvert.getPartyId().getModifiedBy());

        toConvertedBack.getRole().setEntityId(toToConvert.getRole().getEntityId());
        toConvertedBack.getRole().setCreationTime(toToConvert.getRole().getCreationTime());
        toConvertedBack.getRole().setModificationTime(toToConvert.getRole().getModificationTime());
        toConvertedBack.getRole().setCreatedBy(toToConvert.getRole().getCreatedBy());
        toConvertedBack.getRole().setModifiedBy(toToConvert.getRole().getModifiedBy());

        convertedBack.getMpc().setEntityId(toConvert.getMpc().getEntityId());
        convertedBack.getMpc().setCreationTime(toConvert.getMpc().getCreationTime());
        convertedBack.getMpc().setModificationTime(toConvert.getMpc().getModificationTime());
        convertedBack.getMpc().setCreatedBy(toConvert.getMpc().getCreatedBy());
        convertedBack.getMpc().setModifiedBy(toConvert.getMpc().getModifiedBy());

        MessageProperty messagePropertyConvertedBack = getAnyMessageProperty(convertedBack);
        MessageProperty messagePropertyToConvert = getAnyMessageProperty(toConvert);
        messagePropertyConvertedBack.setEntityId(messagePropertyToConvert.getEntityId());
        messagePropertyConvertedBack.setCreationTime(messagePropertyToConvert.getCreationTime());
        messagePropertyConvertedBack.setModificationTime(messagePropertyToConvert.getModificationTime());
        messagePropertyConvertedBack.setCreatedBy(messagePropertyToConvert.getCreatedBy());
        messagePropertyConvertedBack.setModifiedBy(messagePropertyToConvert.getModifiedBy());

        convertedBack.getService().setEntityId(toConvert.getService().getEntityId());
        convertedBack.getService().setCreationTime(toConvert.getService().getCreationTime());
        convertedBack.getService().setModificationTime(toConvert.getService().getModificationTime());
        convertedBack.getService().setCreatedBy(toConvert.getService().getCreatedBy());
        convertedBack.getService().setModifiedBy(toConvert.getService().getModifiedBy());

        convertedBack.getAgreementRef().setEntityId(toConvert.getAgreementRef().getEntityId());
        convertedBack.getAction().setEntityId(toConvert.getAction().getEntityId());

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

    @Test
    public void convertUIMessageEntityMessageLogInfo() {
        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
        final MessageLogInfo converted = messageCoreMapper.uiMessageEntityToMessageLogInfo(toConvert);
        final UIMessageEntity convertedBack = messageCoreMapper.messageLogInfoToUIMessageEntity(converted);
        convertedBack.setLastModified(toConvert.getLastModified());
        convertedBack.setEntityId(toConvert.getEntityId());
        convertedBack.setToScheme(toConvert.getToScheme());
        convertedBack.setFromScheme(toConvert.getFromScheme());

        objectService.assertObjects(convertedBack, toConvert);
    }

//    @Test
//    public void convertUIMessageEntitySignalMessageLog() {
//        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
//        final SignalMessageLog converted = messageCoreMapper.uiMessageEntityToSignalMessageLog(toConvert);
//        final UIMessageEntity convertedBack = messageCoreMapper.signalMessageLogToUIMessageEntity(converted);
//        convertedBack.setConversationId(toConvert.getConversationId());
//        convertedBack.setRefToMessageId(toConvert.getRefToMessageId());
//        convertedBack.setFromId(toConvert.getFromId());
//        convertedBack.setToId(toConvert.getToId());
//        convertedBack.setFromScheme(toConvert.getFromScheme());
//        convertedBack.setToScheme(toConvert.getToScheme());
//        convertedBack.setLastModified(toConvert.getLastModified());
//        convertedBack.setServiceType(toConvert.getServiceType());
//        convertedBack.setServiceValue(toConvert.getServiceValue());
//        convertedBack.setAction(toConvert.getAction());
//        convertedBack.setNotificationStatus(toConvert.getNotificationStatus());
//        convertedBack.setSendAttemptsMax(toConvert.getSendAttemptsMax());
//        convertedBack.setSendAttempts(toConvert.getSendAttempts());
//        convertedBack.setNextAttempt(toConvert.getNextAttempt());
//        convertedBack.setFailed(toConvert.getFailed());
//        convertedBack.setRestored(toConvert.getRestored());
//        convertedBack.setMessageType(toConvert.getMessageType());
//        convertedBack.setMessageSubtype(toConvert.getMessageSubtype());
//        objectService.assertObjects(convertedBack, toConvert);
//    }

//    @Test
//    public void convertUIMessageEntityUserMessageLog() {
//        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
//        final UserMessageLog converted = messageCoreMapper.uiMessageEntityToUserMessageLog(toConvert);
//        final UIMessageEntity convertedBack = messageCoreMapper.userMessageLogToUIMessageEntity(converted);
//        convertedBack.setConversationId(toConvert.getConversationId());
//        convertedBack.setFromId(toConvert.getFromId());
//        convertedBack.setToId(toConvert.getToId());
//        convertedBack.setFromScheme(toConvert.getFromScheme());
//        convertedBack.setToScheme(toConvert.getToScheme());
//        convertedBack.setLastModified(toConvert.getLastModified());
//        convertedBack.setAction(toConvert.getAction());
//        convertedBack.setServiceType(toConvert.getServiceType());
//        convertedBack.setServiceValue(toConvert.getServiceValue());
//        convertedBack.setMessageType(toConvert.getMessageType());
//        objectService.assertObjects(convertedBack, toConvert);
//    }


}