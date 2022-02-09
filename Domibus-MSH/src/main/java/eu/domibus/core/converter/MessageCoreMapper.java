package eu.domibus.core.converter;

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
import org.mapstruct.BeanMapping;
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

    @WithoutAudit
    @Mapping(target = "entityId", source = "id")
    @Mapping(target = "userMessage.messageId", source = "messageId")
    MessageAttemptEntity messageAttemptToMessageAttemptEntity(MessageAttempt messageAttempt);

    @InheritInverseConfiguration
    MessageAttempt messageAttemptEntityToMessageAttempt(MessageAttemptEntity messageAttemptEntity);

    MessageLogInfo messageLogROToMessageLogInfo(MessageLogRO messageLogRO);

    MessageLogRO messageLogInfoToMessageLogRO(MessageLogInfo messageLogInfo);

    UIMessageDiffEntity uiMessageEntityToUIMessageDiffEntity(UIMessageEntity uiMessageEntity);

    @WithoutAuditAndEntityId
    @Mapping(ignore = true, target = "lastModified")
    UIMessageEntity uiMessageDiffEntityToUIMessageEntity(UIMessageDiffEntity uiMessageEntity);

    @Mapping(target = "fromPartyId", source = "fromId")
    @Mapping(target = "toPartyId", source = "toId")
    @Mapping(target = "originalSender", source = "fromScheme")
    @Mapping(target = "finalRecipient", source = "toScheme")
    @Mapping(target = "messageFragment", ignore = true)
    @Mapping(target = "sourceMessage", ignore = true)
    @Mapping(source = "timezoneOffset.nextAttemptTimezoneId", target = "nextAttemptTimezoneId")
    @Mapping(source = "timezoneOffset.nextAttemptOffsetSeconds", target = "nextAttemptOffsetSeconds")
    MessageLogRO uiMessageEntityToMessageLogRO(UIMessageEntity uiMessageEntity);

    @WithoutAuditAndEntityId
    @InheritInverseConfiguration
    @Mapping(target = "lastModified", ignore = true)
    UIMessageEntity messageLogROToUIMessageEntity(MessageLogRO messageLogRO);

    @Mapping(source = "fromId", target = "fromPartyId")
    @Mapping(source = "toId", target = "toPartyId")
    @Mapping(ignore = true, target = "originalSender")
    @Mapping(ignore = true, target = "finalRecipient")
    @Mapping(ignore = true, target = "messageFragment")
    @Mapping(ignore = true, target = "sourceMessage")
    @Mapping(source = "timezoneOffset.nextAttemptTimezoneId", target = "nextAttemptTimezoneId")
    @Mapping(source = "timezoneOffset.nextAttemptOffsetSeconds", target = "nextAttemptOffsetSeconds")
    MessageLogInfo uiMessageEntityToMessageLogInfo(UIMessageEntity uiMessageEntity);

    @WithoutAuditAndEntityId
    @InheritInverseConfiguration
    @Mapping(ignore = true, target = "fromScheme")
    @Mapping(ignore = true, target = "toScheme")
    @Mapping(ignore = true, target = "lastModified")
    UIMessageEntity messageLogInfoToUIMessageEntity(MessageLogInfo uiMessageEntity);

    @InheritInverseConfiguration
    @Mapping(target = "collaborationInfo.agreementRef.pmode", ignore = true)
    @Mapping(target = "payloadInfo", ignore = true)
    @Mapping(target = "collaborationInfo.agreementRef", source = "agreementRef")
    eu.domibus.api.usermessage.domain.UserMessage userMessageToUserMessageApi(UserMessage userMessage);

    @WithoutAuditAndEntityId
    @Mapping(target = "partyInfo.from.fromRole.value", source = "partyInfo.from.role")
    @Mapping(target = "partyInfo.to.toRole.value", source = "partyInfo.to.role")
    @Mapping(target = "partyInfo.from.fromPartyId", source = "partyInfo.from.partyId")
    @Mapping(target = "partyInfo.to.toPartyId", source = "partyInfo.to.partyId")
    @Mapping(target = "mpc.value", source = "mpc")
    @Mapping(target = "messageProperties", source = "messageProperties.property")
    @Mapping(target = "messageId", source = "messageInfo.messageId")
    @Mapping(target = "refToMessageId", source = "messageInfo.refToMessageId")
    @Mapping(target = "timestamp", source = "messageInfo.timestamp")
    @Mapping(target = "conversationId", source = "collaborationInfo.conversationId")
    @Mapping(target = "action", source = "collaborationInfo.action")
    @Mapping(target = "service", source = "collaborationInfo.service")
    @Mapping(target = "agreementRef", source = "collaborationInfo.agreementRef")
    @Mapping(target = "sourceMessage", ignore = true)
    @Mapping(target = "messageFragment", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"partInfoList"})
    UserMessage userMessageApiToUserMessage(eu.domibus.api.usermessage.domain.UserMessage userMessage);

    @WithoutAuditAndEntityId
    @Mapping(target = "binaryData", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "encrypted", ignore = true)
    @Mapping(target = "userMessage", ignore = true)
    @Mapping(target = "length", ignore = true)
    @Mapping(target = "partOrder", ignore = true)
    PartInfo convertPartInfo(eu.domibus.api.usermessage.domain.PartInfo PartInfo);

    @Mapping(target = "schema", ignore = true)
    eu.domibus.api.usermessage.domain.PartInfo convertPartInfo(PartInfo PartInfo);

    @WithoutAuditAndEntityId
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
        } else {
            eu.domibus.api.usermessage.domain.PartyId party = partyIds.stream().findFirst().orElse(null);
            PartyId partyId = new PartyId();
            partyId.setType(party.getType());
            partyId.setValue(party.getValue());
            return partyId;
        }
    }

    @WithoutAuditAndEntityId
    @Mapping(source = ".", target = "value")
    ActionEntity serviceToServiceEntityApi(String action);

    @WithoutAuditAndEntityId
    ServiceEntity serviceToServiceEntityApi(Service service);

    @WithoutAuditAndEntityId
    MessageProperty propertyToMessagePropertyApi(eu.domibus.api.usermessage.domain.Property property);


    List<MessageAttempt> messageAttemptEntityListToMessageAttemptList(List<MessageAttemptEntity> messageAttemptEntityList);

    default NotificationStatus notificationStatus(NotificationStatusEntity s) {
        return s.getStatus();
    }

    default MSHRole mshRole(MSHRoleEntity mshRoleEntity) {
        return mshRoleEntity.getRole();
    }

    default MSHRoleEntity mshRoleEntity(MSHRole notificationStatus) {
        MSHRoleEntity mshRoleEntity = new MSHRoleEntity();
        mshRoleEntity.setRole(notificationStatus);
        return mshRoleEntity;
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
