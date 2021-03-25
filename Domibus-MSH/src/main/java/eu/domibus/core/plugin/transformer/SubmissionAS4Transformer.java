package eu.domibus.core.plugin.transformer;

import eu.domibus.api.model.*;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller
 */
@org.springframework.stereotype.Service
public class SubmissionAS4Transformer {

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    protected MpcDao mpcDao;

    @Autowired
    protected MessagePropertyDao messagePropertyDao;

    @Autowired
    protected ServiceDao serviceDao;

    @Autowired
    protected ActionDao actionDao;

    @Autowired
    protected AgreementDao agreementDao;

    @Autowired
    protected PartyIdDao partyIdDao;

    @Autowired
    protected PartyRoleDao partyRoleDao;

    @Autowired
    protected PartPropertyDao partPropertyDao;

    public UserMessage transformFromSubmission(final Submission submission) throws MessagingProcessingException {
        final UserMessage result = new UserMessage();
        final Mpc mpc = mpcDao.findMpc(submission.getMpc());
        result.setMpc(mpc);
        this.generateMessageInfo(submission, result);
        this.generatePartyInfo(submission, result);
        this.generateCollaborationInfo(submission, result);
        this.generateMessageProperties(submission, result);

        return result;
    }

    private void generateMessageProperties(final Submission submission, final UserMessage result) {
        Set<MessageProperty> messageProperties = new HashSet<>();

        for (Submission.TypedProperty propertyEntry : submission.getMessageProperties()) {
            final Property prop = new Property();
            prop.setName(propertyEntry.getKey());
            prop.setValue(propertyEntry.getValue());
            prop.setType(propertyEntry.getType());

            final MessageProperty propertyByName = messagePropertyDao.findPropertyByName(prop.getName());
            messageProperties.add(propertyByName);
        }

        result.setMessageProperties(messageProperties);
    }

    private void generateCollaborationInfo(final Submission submission, final UserMessage result) {
        // if the conversation id is null, we generate one; otherwise we pass it forward
        String conversationId = submission.getConversationId();
        result.setConversationId(conversationId == null ? this.generateConversationId() : conversationId.trim());

        final Action action = actionDao.findOrCreateAction(submission.getAction());
        result.setAction(action);

        final AgreementRef agreementRef = agreementDao.findOrCreateAgreement(submission.getAgreementRef(), submission.getAgreementRefType());
        result.setAgreementRef(agreementRef);

        final Service service = serviceDao.findOrCreateService(submission.getService(), submission.getServiceType());
        result.setService(service);
    }

    private void generateMessageInfo(final Submission submission, final UserMessage result) {
        result.setMessageId((submission.getMessageId() != null && submission.getMessageId().trim().length() > 0) ? submission.getMessageId() : this.messageIdGenerator.generateMessageId());
        result.setTimestamp(new Date());
        result.setRefToMessageId(submission.getRefToMessageId());
    }

    private void generatePartyInfo(final Submission submission, final UserMessage result) throws MessagingProcessingException {
        final PartyInfo partyInfo = new PartyInfo();
        result.setPartyInfo(partyInfo);

        final From partyFrom = getPartyFrom(submission, partyInfo);
        partyInfo.setFrom(partyFrom);

        final To partyTo = getPartyTo(submission, partyInfo);
        partyInfo.setTo(partyTo);
    }

    private From getPartyFrom(Submission submission, PartyInfo partyInfo) throws MessagingProcessingException {
        final From from = new From();

        final PartyRole fromRole = partyRoleDao.findOrCreateRole(submission.getFromRole());
        from.setRole(fromRole);

        final Set<Submission.Party> fromParties = submission.getFromParties();
        if (fromParties.size() > 1) {
            throw new MessagingProcessingException("Cannot have multiple from parties");
        }
        if (fromParties.size() == 1) {
            final Submission.Party party = fromParties.iterator().next();
            final PartyId fromParty = partyIdDao.findOrCreateParty(party.getPartyId(), party.getPartyIdType());
            from.setPartyId(fromParty);
        }
        return from;
    }

    private To getPartyTo(Submission submission, PartyInfo partyInfo) throws MessagingProcessingException {
        final To to = new To();

        final PartyRole toRole = partyRoleDao.findOrCreateRole(submission.getToRole());
        to.setRole(toRole);

        final Set<Submission.Party> toParties = submission.getToParties();
        if (toParties.size() > 1) {
            throw new MessagingProcessingException("Cannot have multiple to parties");
        }
        if (toParties.size() == 1) {
            final Submission.Party party = toParties.iterator().next();
            final PartyId toParty = partyIdDao.findOrCreateParty(party.getPartyId(), party.getPartyIdType());
            to.setPartyId(toParty);
        }
        return to;
    }


    public List<PartInfo> generatePartInfoList(final Submission submission) {
        List<PartInfo> result = new ArrayList<>();

        for (final Submission.Payload payload : submission.getPayloads()) {
            final PartInfo partInfo = new PartInfo();
            partInfo.setInBody(payload.isInBody());
            partInfo.setPayloadDatahandler(payload.getPayloadDatahandler());
            partInfo.setHref(payload.getContentId());
            partInfo.setLength(payload.getPayloadSize());
            partInfo.setFileName(payload.getFilepath());

            Set<PartProperty> properties = new HashSet<>();
            partInfo.setPartProperties(properties);
            for (final Submission.TypedProperty entry : payload.getPayloadProperties()) {
                final PartProperty property = partPropertyDao.findOrCreateProperty(entry.getKey(), entry.getValue(), entry.getType());
                properties.add(property);
            }
            result.add(partInfo);
        }

        return result;

    }

    public Submission transformFromMessaging(final UserMessage messaging) {
        final Submission result = new Submission();

        if (messaging == null) {
            return result;
        }

        result.setMpc(messaging.getMpc());
        final CollaborationInfo collaborationInfo = messaging.getCollaborationInfo();
        result.setAction(collaborationInfo.getAction());
        result.setService(messaging.getCollaborationInfo().getService().getValue());
        result.setServiceType(messaging.getCollaborationInfo().getService().getType());
        if (collaborationInfo.getAgreementRef() != null) {
            result.setAgreementRef(collaborationInfo.getAgreementRef().getValue());
            result.setAgreementRefType(collaborationInfo.getAgreementRef().getType());
        }
        result.setConversationId(collaborationInfo.getConversationId());

        result.setMessageId(messaging.getMessageInfo().getMessageId());
        result.setRefToMessageId(messaging.getMessageInfo().getRefToMessageId());

        if (messaging.getPayloadInfo() != null) {
            for (final PartInfo partInfo : messaging.getPayloadInfo().getPartInfo()) {
                addPayload(result, partInfo);
            }
        }
        result.setFromRole(messaging.getPartyInfo().getFrom().getRole());
        result.setToRole(messaging.getPartyInfo().getTo().getRole());

        for (final PartyId partyId : messaging.getPartyInfo().getFrom().getPartyId()) {
            result.addFromParty(partyId.getValue(), partyId.getType());
        }

        for (final PartyId partyId : messaging.getPartyInfo().getTo().getPartyId()) {
            result.addToParty(partyId.getValue(), partyId.getType());
        }

        if (messaging.getMessageProperties() != null) {
            for (final Property property : messaging.getMessageProperties().getProperty()) {
                result.addMessageProperty(property.getName(), property.getValue(), property.getType());
            }
        }
        return result;
    }

    protected void addPayload(Submission result, PartInfo partInfo) {
        final Collection<Submission.TypedProperty> properties = new ArrayList<>();
        if (partInfo.getPartProperties() != null) {
            for (final Property property : partInfo.getPartProperties().getProperties()) {
                properties.add(new Submission.TypedProperty(property.getName(), property.getValue(), property.getType()));
            }
        }
        final Submission.Payload payload = new Submission.Payload(partInfo.getHref(), partInfo.getPayloadDatahandler(), properties, partInfo.isInBody(), null, null);
        if (partInfo.getFileName() != null) {
            final String fileNameWithoutPath = FilenameUtils.getName(partInfo.getFileName());
            properties.add(new Submission.TypedProperty("FileName", fileNameWithoutPath, null));

            payload.setFilepath(partInfo.getFileName());
        }
        result.addPayload(payload);
    }

    private String generateConversationId() {
        return this.messageIdGenerator.generateMessageId();
    }
}
