package eu.domibus;

import eu.domibus.api.model.*;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.test.common.MessageTestUtility;
import eu.domibus.test.common.SubmissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class ITTestsService {

    @Autowired
    protected UserMessageLogDao userMessageLogDao;
    @Autowired
    protected SubmissionUtil submissionUtil;

    @Autowired
    protected DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    protected MpcDao mpcDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected PartyIdDao partyIdDao;

    @Autowired
    protected PartyRoleDao partyRoleDao;

    @Autowired
    protected ActionDao actionDao;

    @Autowired
    protected ServiceDao serviceDao;

    @Autowired
    protected AgreementDao agreementDao;

    @Autowired
    protected UserMessageDao userMessageDao;

    @Autowired
    protected MessagePropertyDao messagePropertyDao;

    @Transactional
    public String sendMessageToDelete(MessageStatus endStatus) throws MessagingProcessingException {

        Submission submission = submissionUtil.createSubmission();
        final String messageId = databaseMessageHandler.submit(submission, "mybackend");

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        userMessageLogDao.setMessageStatus(userMessageLog, endStatus);

        return messageId;
    }

    @Transactional
    public UserMessage getUserMessage() {
        final MessageTestUtility messageTestUtility = new MessageTestUtility();
        final UserMessage userMessage = messageTestUtility.createSampleUserMessage();
        final List<PartInfo> partInfoList = messageTestUtility.createPartInfoList(userMessage);

        PartyId senderPartyId = messageTestUtility.createSenderPartyId();
        userMessage.getPartyInfo().getFrom().setFromPartyId(partyIdDao.findOrCreateParty(senderPartyId.getValue(), senderPartyId.getType()));

        userMessage.getPartyInfo().getFrom().setFromRole(partyRoleDao.findOrCreateRole(messageTestUtility.createSenderPartyRole().getValue()));

        final PartyId receiverPartyId = messageTestUtility.createReceiverPartyId();
        userMessage.getPartyInfo().getTo().setToPartyId(partyIdDao.findOrCreateParty(receiverPartyId.getValue(), receiverPartyId.getType()));

        userMessage.getPartyInfo().getTo().setToRole(partyRoleDao.findOrCreateRole(messageTestUtility.createReceiverPartyRole().getValue()));

        userMessage.setAction(actionDao.findOrCreateAction(messageTestUtility.createActionEntity().getValue()));

        final ServiceEntity serviceEntity = messageTestUtility.createServiceEntity();
        userMessage.setService(serviceDao.findOrCreateService(serviceEntity.getValue(), serviceEntity.getType()));

        final AgreementRefEntity agreementRefEntity = messageTestUtility.createAgreementRefEntity();
        userMessage.setAgreementRef(agreementDao.findOrCreateAgreement(agreementRefEntity.getValue(), agreementRefEntity.getType()));

        userMessage.setMpc(mpcDao.findOrCreateMpc(messageTestUtility.createMpcEntity().getValue()));

        HashSet<MessageProperty> messageProperties = new HashSet<>();
        for (MessageProperty messageProperty : userMessage.getMessageProperties()) {
            messageProperties.add(messagePropertyDao.findOrCreateProperty(messageProperty.getName(), messageProperty.getValue(), messageProperty.getType()));
        }

        userMessage.setMessageProperties(messageProperties);

        userMessageDao.create(userMessage);
        return userMessage;
    }
}
