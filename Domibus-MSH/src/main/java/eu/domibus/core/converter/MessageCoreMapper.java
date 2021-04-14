package eu.domibus.core.converter;

import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.*;
import eu.domibus.api.usermessage.domain.PartProperties;
import eu.domibus.api.usermessage.domain.Service;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.core.replication.UIMessageDiffEntity;
import eu.domibus.core.replication.UIMessageEntity;
import eu.domibus.web.rest.ro.MessageLogRO;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface MessageCoreMapper {

    @WithoutMetadata
    @Mapping(target = "entityId", source = "id")
    @Mapping(target = "userMessage.messageId", source = "messageId")
    MessageAttemptEntity messageAttemptToMessageAttemptEntity(MessageAttempt messageAttempt);

    @InheritInverseConfiguration
    MessageAttempt messageAttemptEntityToMessageAttempt(MessageAttemptEntity messageAttemptEntity);

    MessageLogInfo messageLogROToMessageLogInfo(MessageLogRO messageLogRO);

    MessageLogRO messageLogInfoToMessageLogRO(MessageLogInfo messageLogInfo);

    UIMessageDiffEntity uiMessageEntityToUIMessageDiffEntity(UIMessageEntity uiMessageEntity);

    @WithoutAllMetadata
    @Mapping(ignore = true, target = "lastModified")
    UIMessageEntity uiMessageDiffEntityToUIMessageEntity(UIMessageDiffEntity uiMessageEntity);

    @Mapping(target = "fromPartyId", source = "fromId")
    @Mapping(target = "toPartyId", source = "toId")
    @Mapping(target = "originalSender", source = "fromScheme")
    @Mapping(target = "finalRecipient", source = "toScheme")
    @Mapping(target = "messageFragment", ignore = true)
    @Mapping(target = "sourceMessage", ignore = true)
    MessageLogRO uiMessageEntityToMessageLogRO(UIMessageEntity uiMessageEntity);

    @WithoutAllMetadata
    @InheritInverseConfiguration
//    @Mapping(target = "fromId", source = "fromPartyId")
//    @Mapping(target = "toId", source = "toPartyId")
    @Mapping(target = "lastModified", ignore = true)
    UIMessageEntity messageLogROToUIMessageEntity(MessageLogRO messageLogRO);

    @Mapping(source = "fromId", target = "fromPartyId")
    @Mapping(source = "toId", target = "toPartyId")
    @Mapping(ignore = true, target = "originalSender")
    @Mapping(ignore = true, target = "finalRecipient")
    @Mapping(ignore = true, target = "messageFragment")
    @Mapping(ignore = true, target = "sourceMessage")
    MessageLogInfo uiMessageEntityToMessageLogInfo(UIMessageEntity uiMessageEntity);

    @WithoutAllMetadata
    @InheritInverseConfiguration
    @Mapping(ignore = true, target = "fromScheme")
    @Mapping(ignore = true, target = "toScheme")
    @Mapping(ignore = true, target = "lastModified")
    UIMessageEntity messageLogInfoToUIMessageEntity(MessageLogInfo uiMessageEntity);

//    @Mapping(source = "mshRole", target = "mshRole.role")
//    @Mapping(source = "messageStatus", target = "messageStatus.messageStatus")
//    @InheritInverseConfiguration
//    SignalMessageLog uiMessageEntityToSignalMessageLog(UIMessageEntity uiMessageEntity);
//
//    @WithoutMetadata
//    @Mapping(target = "messageId", source = "signalMessage.userMessage.messageId")
//    @Mapping(target = "refToMessageId", source = "signalMessage.userMessage.refToMessageId")
//    @Mapping(target = "conversationId", source = "signalMessage.userMessage.conversationId")
//    @Mapping(target = "fromId", source = "signalMessage.userMessage.partyInfo.from.partyId.value")
//    @Mapping(target = "toId", source = "signalMessage.userMessage.partyInfo.to.partyId.value")
//    @Mapping(target = "action", source = "signalMessage.userMessage.action.value")
//    @Mapping(target = "serviceType", source = "signalMessage.userMessage.service.type")
//    @Mapping(target = "serviceValue", source = "signalMessage.userMessage.service.value")
//    @Mapping(target = "notificationStatus", ignore = true)
//    @Mapping(target = "messageType", ignore = true)
//    @Mapping(target = "messageSubtype", ignore = true)
//    @Mapping(target = "restored", ignore = true)
//    @Mapping(target = "failed", ignore = true)
//    @Mapping(target = "sendAttempts", ignore = true)
//    @Mapping(target = "sendAttemptsMax", ignore = true)
//    @Mapping(target = "nextAttempt", ignore = true)
//    @Mapping(target = "fromScheme", ignore = true)
//    @Mapping(target = "toScheme", ignore = true)
//    @Mapping(target = "lastModified", ignore = true)
//    UIMessageEntity signalMessageLogToUIMessageEntity(SignalMessageLog uiMessageEntity);

//    @Mapping(target = "backend", ignore = true)
//    @Mapping(target = "downloaded", ignore = true)
//    @Mapping(target = "scheduled", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @InheritInverseConfiguration
//    UserMessageLog uiMessageEntityToUserMessageLog(UIMessageEntity uiMessageEntity);
//
//    @WithoutMetadata
//    @Mapping(target = "messageId", source = "userMessage.messageId")
//    @Mapping(target = "conversationId", source = "userMessage.conversationId")
//    @Mapping(target = "refToMessageId", source = "userMessage.refToMessageId")
//    @Mapping(target = "fromId", source = "userMessage.partyInfo.from.partyId.value")
//    @Mapping(target = "toId", source = "userMessage.partyInfo.to.partyId.value")
//    @Mapping(target = "action", source = "userMessage.action.value")
//    @Mapping(target = "serviceType", source = "userMessage.service.type")
//    @Mapping(target = "serviceValue", source = "userMessage.service.value")
//    @Mapping(target = "messageType", ignore = true)
//    @Mapping(target = "fromScheme", ignore = true)
//    @Mapping(target = "toScheme", ignore = true)
//    @Mapping(target = "lastModified", ignore = true)
//    UIMessageEntity userMessageLogToUIMessageEntity(UserMessageLog uiMessageEntity);

    @InheritInverseConfiguration
    @Mapping(target = "collaborationInfo.agreementRef.pmode", ignore = true)
    @Mapping(target = "collaborationInfo.agreementRef", source = "agreementRef")
    eu.domibus.api.usermessage.domain.UserMessage userMessageToUserMessageApi(UserMessage userMessage);

    @WithoutAllMetadata
    @Mapping(target = "partyInfo.from.role.value", source = "partyInfo.from.role")
    @Mapping(target = "partyInfo.to.role.value", source = "partyInfo.to.role")
    @Mapping(target = "mpc.value", source = "mpc")
    @Mapping(target = "messageProperties", source = "messageProperties.property")
    @Mapping(target = "messageId", source = "messageInfo.messageId")
    @Mapping(target = "refToMessageId", source = "messageInfo.refToMessageId")
    @Mapping(target = "timestamp", source = "messageInfo.timestamp")
    @Mapping(target = "conversationId", source = "collaborationInfo.conversationId")
    @Mapping(target = "action", source = "collaborationInfo.action")
    @Mapping(target = "service", source = "collaborationInfo.service")
    @Mapping(target = "agreementRef", source = "collaborationInfo.agreementRef")
    @Mapping(target = "partInfoList", source = "payloadInfo.partInfo")
    @Mapping(target = "sourceMessage", ignore = true)
    @Mapping(target = "messageFragment", ignore = true)
    UserMessage userMessageApiToUserMessage(eu.domibus.api.usermessage.domain.UserMessage userMessage);

    @WithoutAllMetadata
    @Mapping(target = "binaryData", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "encrypted", ignore = true)
    @Mapping(target = "userMessage", ignore = true)
    @Mapping(target = "length", ignore = true)
    @Mapping(target = "partOrder", ignore = true)
    PartInfo convertPartInfo(eu.domibus.api.usermessage.domain.PartInfo PartInfo);

    @Mapping(target = "schema", ignore = true)
    eu.domibus.api.usermessage.domain.PartInfo convertPartInfo(PartInfo PartInfo);

    @WithoutAllMetadata
    PartProperty convertToPartProperty(eu.domibus.api.usermessage.domain.Property property);

    eu.domibus.api.usermessage.domain.Property convertToProperty(PartProperty property);

    default PartProperties convertPartProperties(Set<PartProperty> value) {
        PartProperties partProperties = new PartProperties();
        partProperties.setProperty(value.stream().map(this::convertToProperty).collect(Collectors.toSet()));
        return partProperties;
    }

    default Set<PartProperty> convertPartProperties(PartProperties value) {
        return value.getProperty().stream().map(this::convertToPartProperty).collect(Collectors.toSet());
    }

    default String action(ActionEntity actionEntity) {
        return actionEntity.getValue();
    }

    default Set<eu.domibus.api.usermessage.domain.PartyId> partyInfoToPartyInfoApi(PartyId partyInfo) {
        HashSet<eu.domibus.api.usermessage.domain.PartyId> result = new HashSet<>();

        if (partyInfo == null) {
            return result;
        }

        eu.domibus.api.usermessage.domain.PartyId e = new eu.domibus.api.usermessage.domain.PartyId();
        e.setType(partyInfo.getType());
        e.setValue(partyInfo.getValue());
        result.add(e);
        return result;
    }

    default PartyId partyInfoToPartyInfoApi(Set<eu.domibus.api.usermessage.domain.PartyId> partyIds) {
        if (partyIds == null || CollectionUtils.isEmpty(partyIds) || !partyIds.stream().findFirst().isPresent()) {
            return null;
        }
        eu.domibus.api.usermessage.domain.PartyId party = partyIds.stream().findFirst().get();
        PartyId partyId = new PartyId();
        partyId.setType(party.getType());
        partyId.setValue(party.getValue());
        return partyId;
    }


    @WithoutAllMetadata
    @Mapping(source = ".", target = "value")
    ActionEntity serviceToServiceEntityApi(String action);

    @WithoutAllMetadata
    AgreementRef serviceToServiceEntityApi(eu.domibus.api.usermessage.domain.AgreementRef service);

    @WithoutAllMetadata
    ServiceEntity serviceToServiceEntityApi(Service service);

    @WithoutAllMetadata
    MessageProperty propertyToMessagePropertyApi(eu.domibus.api.usermessage.domain.Property property);


    List<MessageAttempt> messageAttemptEntityListToMessageAttemptList(List<MessageAttemptEntity> messageAttemptEntityList);

    default NotificationStatus notificationStatus(NotificationStatusEntity s) {
        return s.getStatus();
    }

//    default NotificationStatusEntity notificationStatusEntity(NotificationStatus notificationStatus) {
//        NotificationStatusEntity notificationStatusEntity = new NotificationStatusEntity();
//        notificationStatusEntity.setStatus(notificationStatus);
//        return notificationStatusEntity;
//    }

    default MSHRole mshRole(MSHRoleEntity mshRoleEntity) {
        return mshRoleEntity.getRole();
    }

    default MSHRoleEntity mshRoleEntity(MSHRole notificationStatus) {
        MSHRoleEntity mshRoleEntity = new MSHRoleEntity();
        mshRoleEntity.setRole(notificationStatus);
        return mshRoleEntity;
    }

    default MessageSubtype mshRole(MessageSubtypeEntity messageSubtypeEntity) {
        return messageSubtypeEntity.getMessageSubtype();
    }

    default MessageSubtypeEntity mshRoleEntity(MessageSubtype notificationStatus) {
        MessageSubtypeEntity messageSubtypeEntity = new MessageSubtypeEntity();
        messageSubtypeEntity.setMessageSubtype(notificationStatus);
        return messageSubtypeEntity;
    }

    default MessageStatus apiMessageStatus(MessageStatusEntity messageStatusEntity) {
        return messageStatusEntity.getMessageStatus();
    }

    default MessageStatusEntity apiMessageStatus(MessageStatus messageStatus) {
        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(messageStatus);
        return messageStatusEntity;
    }

    default eu.domibus.common.MessageStatus messageStatus(MessageStatusEntity messageStatusEntity) {
        return eu.domibus.common.MessageStatus.valueOf(messageStatusEntity.getMessageStatus().name());
    }

    default MessageStatusEntity messageStatus(eu.domibus.common.MessageStatus messageStatus) {
        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(MessageStatus.valueOf(messageStatus.name()));
        return messageStatusEntity;
    }

}
