package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.*;
import eu.domibus.api.model.*;
import eu.domibus.core.message.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class Ebms3UserMessageMapperImpl implements Ebms3UserMessageMapper {

    protected MpcDao mpcDao;
    protected ActionDao actionDao;
    protected ServiceDao serviceDao;
    protected PartyIdDao partyIdDao;
    protected AgreementDao agreementDao;
    protected MessagePropertyDao messagePropertyDao;
    protected PartPropertyDao partPropertyDao;
    protected PartyRoleDao partyRoleDao;
    protected MessageDictionaryService messageDictionaryService;

    public Ebms3UserMessageMapperImpl(MpcDao mpcDao, ActionDao actionDao, ServiceDao serviceDao, PartyIdDao partyIdDao, AgreementDao agreementDao, MessagePropertyDao messagePropertyDao, PartPropertyDao partPropertyDao, PartyRoleDao partyRoleDao, MessageDictionaryService messageDictionaryService) {
        this.mpcDao = mpcDao;
        this.actionDao = actionDao;
        this.serviceDao = serviceDao;
        this.partyIdDao = partyIdDao;
        this.agreementDao = agreementDao;
        this.messagePropertyDao = messagePropertyDao;
        this.partPropertyDao = partPropertyDao;
        this.partyRoleDao = partyRoleDao;
        this.messageDictionaryService = messageDictionaryService;
    }

    @Override
    public Ebms3UserMessage userMessageEntityToEbms3(UserMessage userMessage, List<PartInfo> partinfoList) {
        Ebms3UserMessage ebms3UserMessage = new Ebms3UserMessage();
        Ebms3MessageInfo messageInfo = new Ebms3MessageInfo();
        ebms3UserMessage.setMessageInfo(messageInfo);
        messageInfo.setMessageId(userMessage.getMessageId());
        messageInfo.setRefToMessageId(userMessage.getRefToMessageId());
        messageInfo.setTimestamp(userMessage.getTimestamp());

        ebms3UserMessage.setCollaborationInfo(convertCollaborationInfo(userMessage));
        ebms3UserMessage.setMpc(userMessage.getMpcValue());

        Ebms3MessageProperties ebms3MessageProperties = new Ebms3MessageProperties();
        ebms3UserMessage.setMessageProperties(ebms3MessageProperties);
        userMessage.getMessageProperties()
                .stream()
                .forEach(messageProperty -> ebms3MessageProperties.getProperty().add(convertToEbms3Property(messageProperty)));


        final Ebms3PartyInfo ebms3PartyInfo = convertPartyInfo(userMessage.getPartyInfo());
        ebms3UserMessage.setPartyInfo(ebms3PartyInfo);
        Ebms3PayloadInfo payloadInfo = convertPayloadInfo(partinfoList);
        ebms3UserMessage.setPayloadInfo(payloadInfo);

        return ebms3UserMessage;
    }

    @Override
    public UserMessage userMessageEbms3ToEntity(Ebms3UserMessage ebms3UserMessage) {
        UserMessage userMessage = new UserMessage();

        final Ebms3MessageInfo messageInfo = ebms3UserMessage.getMessageInfo();
        userMessage.setMessageId(messageInfo.getMessageId());
        userMessage.setRefToMessageId(messageInfo.getRefToMessageId());
        userMessage.setTimestamp(messageInfo.getTimestamp());

        final Ebms3CollaborationInfo collaborationInfo = ebms3UserMessage.getCollaborationInfo();

        final ActionEntity actionEntity = actionDao.findOrCreateAction(collaborationInfo.getAction());
        userMessage.setAction(actionEntity);

        final Ebms3Service ebms3Service = collaborationInfo.getService();
        final ServiceEntity serviceEntity = serviceDao.findOrCreateService(ebms3Service.getValue(), ebms3Service.getType());
        userMessage.setService(serviceEntity);

        userMessage.setConversationId(collaborationInfo.getConversationId());

        final Ebms3AgreementRef agreementRef = collaborationInfo.getAgreementRef();
        if (agreementRef != null) {
            final AgreementRefEntity agreement = messageDictionaryService.findOrCreateAgreement(agreementRef.getValue(), agreementRef.getType());
            userMessage.setAgreementRef(agreement);
        }

        final MpcEntity mpcEntity = mpcDao.findOrCreateMpc(ebms3UserMessage.getMpc());
        userMessage.setMpc(mpcEntity);

        final Ebms3MessageProperties userMessageMessageProperties = ebms3UserMessage.getMessageProperties();
        if (userMessageMessageProperties != null) {
            final Set<Ebms3Property> properties = userMessageMessageProperties.getProperty();
            userMessage.setMessageProperties(new HashSet<>());
            properties.stream().forEach(ebms3Property -> {
                final MessageProperty messageProperty = convertToMessageProperty(ebms3Property);
                userMessage.getMessageProperties().add(messageProperty);
            });
        }

        final Ebms3PartyInfo ebms3PartyInfo = ebms3UserMessage.getPartyInfo();

        PartyInfo partyInfo = new PartyInfo();
        userMessage.setPartyInfo(partyInfo);

        final From from = ebms3FromToUserMessageFrom(ebms3PartyInfo.getFrom());
        partyInfo.setFrom(from);

        final To to = ebms3FromToUserMessageFrom(ebms3PartyInfo.getTo());
        partyInfo.setTo(to);

        return userMessage;
    }

    @Override
    public List<PartInfo> partInfoEbms3ToEntity(Ebms3UserMessage ebms3UserMessage) {
        final Ebms3PayloadInfo ebms3PayloadInfo = ebms3UserMessage.getPayloadInfo();
        final List<Ebms3PartInfo> partInfoList = ebms3PayloadInfo.getPartInfo();
        List<PartInfo> result = new ArrayList<>();
        partInfoList.stream().forEach(ebms3PartInfo -> {
            final PartInfo partInfo = convertFromEbms3PartInfo(ebms3PartInfo);
            result.add(partInfo);
        });

        return result;
    }

    private From ebms3FromToUserMessageFrom(Ebms3From ebms3From) {
        From from = new From();

        final PartyRole fromPartyRole = partyRoleDao.findOrCreateRole(ebms3From.getRole());
        from.setRole(fromPartyRole);

        final Ebms3PartyId fromEbms3PartyId = ebms3From.getPartyId().iterator().next();
        final PartyId fromPartyId = partyIdDao.findOrCreateParty(fromEbms3PartyId.getValue(), fromEbms3PartyId.getType());
        from.setPartyId(fromPartyId);
        return from;
    }

    private To ebms3FromToUserMessageFrom(Ebms3To ebms3To) {
        To to = new To();

        final PartyRole toPartyRole = partyRoleDao.findOrCreateRole(ebms3To.getRole());
        to.setRole(toPartyRole);

        final Ebms3PartyId toEbms3PartyId = ebms3To.getPartyId().iterator().next();
        final PartyId toPartyId = partyIdDao.findOrCreateParty(toEbms3PartyId.getValue(), toEbms3PartyId.getType());
        to.setPartyId(toPartyId);
        return to;
    }

    private Ebms3PayloadInfo convertPayloadInfo(List<PartInfo> partInfoList) {
        if (CollectionUtils.isEmpty(partInfoList)) {
            return null;
        }

        Ebms3PayloadInfo result = new Ebms3PayloadInfo();
        partInfoList.stream().forEach(partInfo -> result.getPartInfo().add(convertEbms3PartInfo(partInfo)));
        return result;
    }

    protected Ebms3PartInfo convertEbms3PartInfo(PartInfo partInfo) {
        Ebms3PartInfo result = new Ebms3PartInfo();
        result.setHref(partInfo.getHref());
        final Description description = partInfo.getDescription();
        if (description != null) {
            Ebms3Description ebms3Description = new Ebms3Description();
            ebms3Description.setValue(description.getValue());
            ebms3Description.setLang(description.getLang());
            result.setDescription(ebms3Description);
        }

        Ebms3PartProperties ebms3PartProperties = new Ebms3PartProperties();
        result.setPartProperties(ebms3PartProperties);

        final Set<PartProperty> partProperties = partInfo.getPartProperties();
        if (partProperties != null) {
            partProperties.stream().forEach(partProperty -> ebms3PartProperties.getProperties().add(convertToEbms3Property(partProperty)));
        }

        return result;
    }

    protected PartInfo convertFromEbms3PartInfo(Ebms3PartInfo partInfo) {
        PartInfo result = new PartInfo();
        result.setHref(partInfo.getHref());
        final Ebms3Description ebms3Description = partInfo.getDescription();
        if (ebms3Description != null) {
            Description description = new Description();
            description.setValue(description.getValue());
            description.setLang(description.getLang());
            result.setDescription(description);
        }

        final Ebms3PartProperties ebms3PartProperties = partInfo.getPartProperties();
        if (ebms3PartProperties != null) {
            result.setPartProperties(new HashSet<>());
            final Set<Ebms3Property> properties = ebms3PartProperties.getProperties();
            properties.stream().forEach(partProperty -> result.getPartProperties().add(convertToPartProperty(partProperty)));
        }

        return result;
    }

    private Ebms3CollaborationInfo convertCollaborationInfo(UserMessage userMessage) {
        Ebms3CollaborationInfo collaborationInfo = new Ebms3CollaborationInfo();

        collaborationInfo.setAction(userMessage.getActionValue());

        final ServiceEntity service = userMessage.getService();
        if (service != null) {
            Ebms3Service ebms3Service = new Ebms3Service();
            ebms3Service.setValue(service.getValue());
            ebms3Service.setType(service.getType());
            collaborationInfo.setService(ebms3Service);
        }
        collaborationInfo.setConversationId(userMessage.getConversationId());

        final AgreementRefEntity agreementRef = userMessage.getAgreementRef();
        if (agreementRef != null) {
            Ebms3AgreementRef ebms3AgreementRef = new Ebms3AgreementRef();
            collaborationInfo.setAgreementRef(ebms3AgreementRef);
            ebms3AgreementRef.setType(agreementRef.getType());
            ebms3AgreementRef.setValue(agreementRef.getValue());
        }

        return collaborationInfo;
    }

    private Ebms3PartyInfo convertPartyInfo(PartyInfo partyInfo) {
        if (partyInfo == null) {
            return null;
        }
        Ebms3PartyInfo ebms3PartyInfo = new Ebms3PartyInfo();
        ebms3PartyInfo.setFrom(convertPartyFrom(partyInfo.getFrom()));
        ebms3PartyInfo.setTo(convertPartyTo(partyInfo.getTo()));
        return ebms3PartyInfo;
    }

    private Ebms3To convertPartyTo(To to) {
        Ebms3To ebms3To = new Ebms3To();
        ebms3To.setRole(to.getRoleValue());
        Ebms3PartyId ebms3FromPartyId = new Ebms3PartyId();
        ebms3FromPartyId.setValue(to.getPartyId().getValue());
        ebms3FromPartyId.setType(to.getPartyId().getType());
        ebms3To.getPartyId().add(ebms3FromPartyId);
        return ebms3To;
    }

    private Ebms3From convertPartyFrom(final From from) {
        Ebms3From ebms3From = new Ebms3From();
        ebms3From.setRole(from.getRoleValue());
        Ebms3PartyId ebms3FromPartyId = new Ebms3PartyId();
        ebms3FromPartyId.setValue(from.getPartyId().getValue());
        ebms3FromPartyId.setType(from.getPartyId().getType());
        ebms3From.getPartyId().add(ebms3FromPartyId);
        return ebms3From;
    }

    protected Ebms3Property convertToEbms3Property(Property partProperty) {
        Ebms3Property result = new Ebms3Property();
        result.setName(partProperty.getName());
        result.setType(partProperty.getType());
        result.setValue(partProperty.getValue());
        return result;
    }

    protected MessageProperty convertToMessageProperty(Ebms3Property msgProperty) {
        return messagePropertyDao.findOrCreateProperty(msgProperty.getName(), msgProperty.getValue(), msgProperty.getType());
    }

    protected PartProperty convertToPartProperty(Ebms3Property partProperty) {
        return messageDictionaryService.findOrCreatePartProperty(partProperty.getName(), partProperty.getValue(), partProperty.getType());
    }


}
