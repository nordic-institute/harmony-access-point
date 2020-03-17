package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Splitting;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.error.ErrorService;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.receipt.AS4ReceiptService;
import eu.domibus.core.message.retention.MessageRetentionService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.core.ebms3.ws.attachment.AttachmentCleanupService;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.ebms3.receiver.handler.IncomingSourceMessageHandler;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.MSHDispatcher;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.message.MessageImpl;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Cosmin Baciu, Soumya
 * @since 4.1
 */
@RunWith(JMockit.class)
public class SplitAndJoinDefaultServiceTest {

    @Tested
    SplitAndJoinDefaultService splitAndJoinDefaultService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected MessagingDao messagingDao;

    @Injectable
    protected MessageGroupDao messageGroupDao;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected SoapUtil soapUtil;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected PayloadFileStorageProvider storageProvider;

    @Injectable
    protected MessageUtil messageUtil;

    @Injectable
    protected UserMessageDefaultService userMessageDefaultService;

    @Injectable
    protected UserMessageLogDao userMessageLogDao;

    @Injectable
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    protected AttachmentCleanupService attachmentCleanupService;

    @Injectable
    protected UserMessageHandlerService userMessageHandlerService;

    @Injectable
    protected MessagingService messagingService;

    @Injectable
    protected UserMessageService userMessageService;

    @Injectable
    protected IncomingSourceMessageHandler incomingSourceMessageHandler;

    @Injectable
    protected PolicyService policyService;

    @Injectable
    protected MSHDispatcher mshDispatcher;

    @Injectable
    protected AS4ReceiptService as4ReceiptService;

    @Injectable
    protected EbMS3MessageBuilder messageBuilder;

    @Injectable
    protected MessageRetentionService messageRetentionService;

    @Injectable
    protected MessageGroupService messageGroupService;

    @Injectable
    protected ErrorService errorService;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void createUserFragmentsFromSourceFile(@Injectable SOAPMessage sourceMessageRequest,
                                                  @Injectable UserMessage userMessage,
                                                  @Injectable MessageExchangeConfiguration userMessageExchangeConfiguration,
                                                  @Injectable LegConfiguration legConfiguration,
                                                  @Mocked File file) throws EbMS3Exception, IOException {
        String sourceMessageFileName = "invoice.pdf";
        long sourceMessageFileLength = 23L;
        String contentTypeString = "application/pdf";
        boolean compression = false;
        String pModeKey = "mykey";
        String sourceMessageId = "123";
        String groupId = sourceMessageId;


        List<String> fragmentFiles = new ArrayList<>();
        fragmentFiles.add("fragment1");
        fragmentFiles.add("fragment2");

        new Expectations(splitAndJoinDefaultService) {{
            userMessage.getMessageInfo().getMessageId();
            result = groupId;

            userMessage.getMessageInfo().getMessageId();
            result = sourceMessageId;

            new File(sourceMessageFileName);
            result = file;

            file.delete();
            result = true;

            file.length();
            result = sourceMessageFileLength;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = userMessageExchangeConfiguration;

            userMessageExchangeConfiguration.getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;


            splitAndJoinDefaultService.splitSourceMessage((File) any, anyInt);
            result = fragmentFiles;
        }};

        splitAndJoinDefaultService.createUserFragmentsFromSourceFile(sourceMessageFileName, sourceMessageRequest, userMessage, contentTypeString, compression);

        new Verifications() {{
            MessageGroupEntity messageGroupEntity = null;
            userMessageDefaultService.createMessageFragments(userMessage, messageGroupEntity = withCapture(), fragmentFiles);

            Assert.assertEquals(messageGroupEntity.getFragmentCount().longValue(), 2L);
            Assert.assertEquals(messageGroupEntity.getSourceMessageId(), sourceMessageId);
            Assert.assertEquals(messageGroupEntity.getGroupId(), groupId);
            Assert.assertEquals(messageGroupEntity.getMessageSize(), BigInteger.valueOf(sourceMessageFileLength));

            attachmentCleanupService.cleanAttachments(sourceMessageRequest);
        }};
    }

    @Test
    public void rejoinSourceMessage(@Injectable final SOAPMessage sourceRequest,
                                    @Injectable final Messaging messaging,
                                    @Injectable MessageExchangeConfiguration userMessageExchangeConfiguration,
                                    @Injectable LegConfiguration legConfiguration
    ) throws EbMS3Exception, TransformerException, SOAPException {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;
        String sourceMessageFile = "invoice.pdf";
        String backendName = "mybackend";
        String pModeKey = "mykey";
        String reversePModeKey = "reversemykey";

        new Expectations(splitAndJoinDefaultService) {{
            splitAndJoinDefaultService.rejoinSourceMessage(groupId, (File) any);
            result = sourceRequest;

            messageUtil.getMessage(sourceRequest);
            result = messaging;

            pModeProvider.findUserMessageExchangeContext(messaging.getUserMessage(), MSHRole.RECEIVING);
            result = userMessageExchangeConfiguration;

            userMessageExchangeConfiguration.getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = sourceMessageId;

            userMessageExchangeConfiguration.getReversePmodeKey();
            result = reversePModeKey;
        }};

        splitAndJoinDefaultService.rejoinSourceMessage(groupId, sourceMessageFile, backendName);

        new Verifications() {{
            userMessageHandlerService.handlePayloads(sourceRequest, messaging.getUserMessage());
            messagingService.storePayloads(messaging, MSHRole.RECEIVING, legConfiguration, backendName);
            messageGroupService.setSourceMessageId(sourceMessageId, groupId);
            incomingSourceMessageHandler.processMessage(sourceRequest, messaging);
            userMessageService.scheduleSourceMessageReceipt(sourceMessageId, reversePModeKey);
        }};
    }

    @Test
    public void rejoinSourceMessage1(@Injectable File sourceMessageFile,
                                     @Injectable MessageGroupEntity messageGroupEntity) {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;
        String contentType = "application/xml";


        new Expectations(splitAndJoinDefaultService) {{
            messageGroupDao.findByGroupId(groupId);
            result = messageGroupEntity;

            splitAndJoinDefaultService.createContentType(anyString, anyString);
            result = contentType;

            splitAndJoinDefaultService.getUserMessage(sourceMessageFile, contentType);
        }};

        splitAndJoinDefaultService.rejoinSourceMessage(groupId, sourceMessageFile);

        new Verifications() {{
            messageGroupDao.findByGroupId(groupId);
            times = 1;

            splitAndJoinDefaultService.getUserMessage(sourceMessageFile, contentType);
        }};
    }

    @Test
    public void sendSourceMessageReceipt(@Injectable final SOAPMessage sourceRequest) throws EbMS3Exception {
        String sourceMessageId = "123";
        String pModeKey = "mykey";

        new Expectations(splitAndJoinDefaultService) {{
            as4ReceiptService.generateReceipt(sourceMessageId, false);
            result = sourceRequest;

            splitAndJoinDefaultService.sendSignalMessage(sourceRequest, pModeKey);
        }};

        splitAndJoinDefaultService.sendSourceMessageReceipt(sourceMessageId, pModeKey);

        new Verifications() {{
            splitAndJoinDefaultService.sendSignalMessage(sourceRequest, pModeKey);
            times = 1;
        }};
    }

    @Test
    public void sendSignalError(@Injectable SOAPMessage soapMessage) throws EbMS3Exception {
        String messageId = "123";
        String pModeKey = "mykey";
        String ebMS3ErrorCode = ErrorCode.EbMS3ErrorCode.EBMS_0004.getCode().getErrorCode().getErrorCodeName();
        String errorDetail = "Split and Joing error";

        new Expectations() {{
            messageBuilder.buildSOAPFaultMessage((Error) any);
            result = soapMessage;
        }};

        splitAndJoinDefaultService.sendSignalError(messageId, ebMS3ErrorCode, errorDetail, pModeKey);

        new Verifications() {{
            Error error = null;
            messageBuilder.buildSOAPFaultMessage(error = withCapture());

            Assert.assertEquals(error.getErrorCode(), ebMS3ErrorCode);
            Assert.assertEquals(error.getErrorDetail(), errorDetail);

            splitAndJoinDefaultService.sendSignalMessage(soapMessage, pModeKey);
        }};
    }

    @Test
    public void sendSignalMessage(@Injectable SOAPMessage soapMessage,
                                  @Injectable LegConfiguration legConfiguration,
                                  @Injectable Party receiverParty,
                                  @Injectable Policy policy
    ) throws EbMS3Exception {
        String pModeKey = "mykey";
        String endpoint = "http://localhost/msh";

        new Expectations() {{
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getEndpoint();
            result = endpoint;

            policyService.getPolicy(legConfiguration);
            result = policy;
        }};

        splitAndJoinDefaultService.sendSignalMessage(soapMessage, pModeKey);

        new Verifications() {{
            mshDispatcher.dispatch(soapMessage, endpoint, policy, legConfiguration, pModeKey);
        }};
    }

    @Test
    public void mayUseSplitAndJoin(@Injectable LegConfiguration legConfiguration,
                                   @Injectable Splitting splitting) {
        new Expectations() {{
            legConfiguration.getSplitting();
            result = splitting;
        }};

        Assert.assertTrue(splitAndJoinDefaultService.mayUseSplitAndJoin(legConfiguration));
    }

    @Test
    public void generateSourceFileName(@Mocked UUID uuid) {
        String directory = "/home/temp";
        String uuidValue = "123";

        new Expectations() {{
            UUID.randomUUID().toString();
            result = uuidValue;
        }};

        final String generateSourceFileName = splitAndJoinDefaultService.generateSourceFileName(directory);

        Assert.assertEquals(generateSourceFileName, directory + "/" + uuidValue);
        ;
    }

    @Test
    public void rejoinMessageFragments(@Injectable MessageGroupEntity messageGroupEntity,
                                       @Mocked UserMessage userMessage1,
                                       @Injectable PartInfo partInfo

    ) {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;
        String fileName = "invoice.pdf";

        List<UserMessage> userMessageFragments = new ArrayList<>();
        userMessageFragments.add(userMessage1);

        List<PartInfo> partInfoList = new ArrayList<>();
        partInfoList.add(partInfo);

        new Expectations(splitAndJoinDefaultService) {{
            messageGroupDao.findByGroupId(groupId);
            result = messageGroupEntity;

            messagingDao.findUserMessageByGroupId(groupId);
            result = userMessageFragments;

            messageGroupEntity.getFragmentCount();
            result = 1;

            userMessage1.getPayloadInfo().getPartInfo();
            result = partInfoList;

            partInfo.getFileName();
            result = fileName;

            splitAndJoinDefaultService.mergeSourceFile((List<File>) any, messageGroupEntity);
        }};

        splitAndJoinDefaultService.rejoinMessageFragments(groupId);

        new Verifications() {{
            List<File> fragmentFilesInOrder = null;

            splitAndJoinDefaultService.mergeSourceFile(fragmentFilesInOrder = withCapture(), messageGroupEntity);

            Assert.assertEquals(fragmentFilesInOrder.size(), 1);
        }};
    }


    @Test
    public void setSourceMessageAsFailed(@Injectable UserMessage userMessage,
                                         @Injectable UserMessageLog messageLog) {
        String messageId = "123";

        new Expectations() {{
            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageIdSafely(messageId);
            result = messageLog;
        }};

        splitAndJoinDefaultService.setSourceMessageAsFailed(userMessage);

        new Verifications() {{
            userMessageLogDao.findByMessageIdSafely(messageId);

            updateRetryLoggingService.messageFailed(userMessage, messageLog);
            times = 1;
        }};
    }

    @Test
    public void setUserMessageFragmentAsFailedSendEnqueued(@Injectable UserMessage userMessage,
                                                           @Injectable UserMessageLog messageLog) {
        setUserMessageFragmentAsFailed(userMessage, messageLog, MessageStatus.SEND_ENQUEUED);
    }

    @Test
    public void setUserMessageFragmentAsFailedWaitingForRetry(@Injectable UserMessage userMessage,
                                                              @Injectable UserMessageLog messageLog) {
        setUserMessageFragmentAsFailed(userMessage, messageLog, MessageStatus.WAITING_FOR_RETRY);
    }

    @Test
    public void setUserMessageFragmentAsFailedAcknowledged(@Injectable UserMessage userMessage,
                                                           @Injectable UserMessageLog messageLog) {
        String messageId = "123";
        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            userMessageLogDao.findByMessageIdSafely(messageId);
            result = messageLog;

            messageLog.getMessageStatus();
            result = MessageStatus.ACKNOWLEDGED;
            ;
        }};

        splitAndJoinDefaultService.setUserMessageFragmentAsFailed(messageId);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, messageLog);
            times = 0;
        }};
    }

    protected void setUserMessageFragmentAsFailed(@Injectable UserMessage userMessage,
                                                  @Injectable UserMessageLog messageLog, MessageStatus messageStatus) {
        String messageId = "123";
        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            userMessageLogDao.findByMessageIdSafely(messageId);
            result = messageLog;

            messageLog.getMessageStatus();
            result = messageStatus;
            ;
        }};

        splitAndJoinDefaultService.setUserMessageFragmentAsFailed(messageId);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, messageLog);
            times = 1;
        }};
    }

    @Test
    public void setUserMessageFragmentAsFailedWithOtherStatus(@Injectable UserMessage userMessage,
                                                              @Injectable UserMessageLog messageLog) {
        String messageId = "123";
        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            userMessageLogDao.findByMessageIdSafely(messageId);
            result = messageLog;

            messageLog.getMessageStatus();
            result = MessageStatus.ACKNOWLEDGED;
        }};

        splitAndJoinDefaultService.setUserMessageFragmentAsFailed(messageId);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, messageLog);
            times = 0;

        }};
    }

    @Test
    public void handleExpiredReceivedGroups(@Injectable MessageGroupEntity group1) {
        List<MessageGroupEntity> messageGroupEntities = new ArrayList<>();
        messageGroupEntities.add(group1);

        List<MessageGroupEntity> expiredGroups = new ArrayList<>();
        expiredGroups.add(group1);

        new Expectations(splitAndJoinDefaultService) {{
            messageGroupDao.findOngoingReceivedNonExpiredOrRejected();
            result = messageGroupEntities;

            splitAndJoinDefaultService.getReceivedExpiredGroups(messageGroupEntities);
            result = expiredGroups;

            splitAndJoinDefaultService.setReceivedGroupAsExpired(group1);
        }};

        splitAndJoinDefaultService.handleExpiredReceivedGroups();

        new Verifications() {{
            splitAndJoinDefaultService.setReceivedGroupAsExpired(group1);
            times = 1;

        }};
    }

    @Test
    public void handleExpiredSendGroups(@Injectable MessageGroupEntity group1) {
        List<MessageGroupEntity> messageGroupEntities = new ArrayList<>();
        messageGroupEntities.add(group1);

        List<MessageGroupEntity> expiredGroups = new ArrayList<>();
        expiredGroups.add(group1);

        new Expectations(splitAndJoinDefaultService) {{
            messageGroupDao.findOngoingSendNonExpiredOrRejected();
            result = messageGroupEntities;

            splitAndJoinDefaultService.getSendExpiredGroups(messageGroupEntities);
            result = expiredGroups;

            splitAndJoinDefaultService.setSendGroupAsExpired(group1);
        }};

        splitAndJoinDefaultService.handleExpiredSendGroups();

        new Verifications() {{
            splitAndJoinDefaultService.setSendGroupAsExpired(group1);
            times = 1;

        }};
    }

    @Test
    public void setReceivedGroupAsExpired(@Injectable MessageGroupEntity group1) {
        String groupId = "123";

        new Expectations() {{
            group1.getGroupId();
            result = groupId;

            group1.getSourceMessageId();
            result = groupId;
        }};

        splitAndJoinDefaultService.setReceivedGroupAsExpired(group1);

        new Verifications() {{
            group1.setExpired(true);
            messageGroupDao.update(group1);

            userMessageService.scheduleSplitAndJoinReceiveFailed(groupId, groupId, ErrorCode.EbMS3ErrorCode.EBMS_0051.getCode().getErrorCode().getErrorCodeName(), SplitAndJoinDefaultService.ERROR_MESSAGE_GROUP_HAS_EXPIRED);
        }};
    }

    @Test
    public void setSendGroupAsExpired(@Injectable MessageGroupEntity group1) {
        String groupId = "123";

        new Expectations() {{
            group1.getGroupId();
            result = groupId;
        }};

        splitAndJoinDefaultService.setSendGroupAsExpired(group1);

        new Verifications() {{
            group1.setExpired(true);
            messageGroupDao.update(group1);

            userMessageService.scheduleSplitAndJoinSendFailed(groupId, anyString);
        }};
    }

    @Test
    public void getReceivedExpiredGroups(@Injectable MessageGroupEntity group1) {
        List<MessageGroupEntity> messageGroupEntities = new ArrayList<>();
        messageGroupEntities.add(group1);

        new Expectations(splitAndJoinDefaultService) {{
            splitAndJoinDefaultService.isReceivedGroupExpired(group1);
            result = true;
        }};

        final List<MessageGroupEntity> expiredGroups = splitAndJoinDefaultService.getReceivedExpiredGroups(messageGroupEntities);
        assertNotNull(expiredGroups);
        assertEquals(expiredGroups.size(), 1);
        assertEquals(expiredGroups.iterator().next(), group1);


    }

    @Test
    public void isReceivedGroupExpired(@Injectable MessageGroupEntity group,
                                       @Injectable UserMessage userMessageFragment) {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;
        String firstFragmentMessageId = "456";

        final List<UserMessage> fragments = new ArrayList<>();
        fragments.add(userMessageFragment);

        new Expectations(splitAndJoinDefaultService) {{
            group.getGroupId();
            result = groupId;

            messagingDao.findUserMessageByGroupId(groupId);
            result = fragments;

            splitAndJoinDefaultService.isGroupExpired((UserMessage) any, anyString);
            result = true;
        }};

        final boolean groupExpired = splitAndJoinDefaultService.isReceivedGroupExpired(group);
        Assert.assertTrue(groupExpired);

        new Verifications() {{
            splitAndJoinDefaultService.isGroupExpired(userMessageFragment, groupId);
        }};
    }

    @Test
    public void isSendGroupExpired(@Injectable MessageGroupEntity group,
                                   @Injectable final UserMessage sourceUserMessage) throws EbMS3Exception {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;

        new Expectations(splitAndJoinDefaultService) {{
            group.getGroupId();
            result = groupId;

            group.getSourceMessageId();
            result = sourceMessageId;

            messagingDao.findUserMessageByMessageId(sourceMessageId);
            result = sourceUserMessage;

            splitAndJoinDefaultService.isGroupExpired((UserMessage) any, anyString);
            result = true;
        }};

        final boolean groupExpired = splitAndJoinDefaultService.isSendGroupExpired(group);
        Assert.assertTrue(groupExpired);

        new Verifications() {{
            splitAndJoinDefaultService.isGroupExpired(sourceUserMessage, groupId);
        }};


    }

    @Test
    public void isGroupExpired(@Injectable final UserMessage userMessage,
                               @Injectable MessageExchangeConfiguration userMessageExchangeContext,
                               @Injectable LegConfiguration legConfiguration,
                               @Mocked Timestamp timestamp,
                               @Injectable UserMessageLog userMessageLog) throws EbMS3Exception {
        String userMessageId = "123";
        String groupId = userMessageId;
        String pmodeKey = "pModeKey";


        final LocalDateTime now = LocalDateTime.of(2019, 01, 01, 12, 10);
        final LocalDateTime messageTime = LocalDateTime.of(2019, 01, 01, 12, 5);

        new Expectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;

            userMessage.getMessageInfo().getMessageId();
            result = userMessageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = userMessageExchangeContext;

            userMessageExchangeContext.getPmodeKey();
            result = pmodeKey;

            pModeProvider.getLegConfiguration(pmodeKey);
            result = legConfiguration;

            legConfiguration.getSplitting().getJoinInterval();
            result = 1;

            userMessageLogDao.findByMessageId(userMessageId);
            result = userMessageLog;

            new Timestamp(userMessageLog.getReceived().getTime());
            result = timestamp;

            timestamp.toLocalDateTime();
            result = messageTime;
        }};

        final boolean groupExpired = splitAndJoinDefaultService.isGroupExpired(userMessage, groupId);
        Assert.assertTrue(groupExpired);

    }

    @Test
    public void messageFragmentSendFailed(@Injectable UserMessage userMessage) {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;

        final List<UserMessage> fragments = new ArrayList<>();
        fragments.add(userMessage);

        new Expectations(splitAndJoinDefaultService) {{
            splitAndJoinDefaultService.sendSplitAndJoinFailed(groupId);

            messagingDao.findUserMessageByGroupId(groupId);
            result = fragments;
        }};

        splitAndJoinDefaultService.splitAndJoinSendFailed(groupId, "Send failed");

        new Verifications() {{
            userMessageService.scheduleSetUserMessageFragmentAsFailed(userMessage.getMessageInfo().getMessageId());
        }};
    }

    @Test
    public void sendSplitAndJoinFailed(@Injectable UserMessage userMessage,
                                       @Injectable MessageGroupEntity messageGroupEntity) {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;

        new Expectations() {{
            messageGroupDao.findByGroupId(groupId);
            result = messageGroupEntity;

            messagingDao.findUserMessageByMessageId(messageGroupEntity.getSourceMessageId());
            result = userMessage;

            splitAndJoinDefaultService.setSourceMessageAsFailed(userMessage);
        }};

        splitAndJoinDefaultService.sendSplitAndJoinFailed(groupId);

        new Verifications() {{
            messageGroupEntity.setRejected(true);
            messageGroupDao.update(messageGroupEntity);

            splitAndJoinDefaultService.setSourceMessageAsFailed(userMessage);
            times = 1;
        }};
    }

    @Test
    public void splitAndJoinReceiveFailed(@Injectable UserMessage fragment,
                                          @Injectable MessageGroupEntity messageGroupEntity,
                                          @Injectable MessageExchangeConfiguration userMessageExchangeContext,
                                          @Injectable LegConfiguration legConfiguration) throws EbMS3Exception {
        String fragmentId = "456";
        String groupId = "123";
        final String ebMS3ErrorCode = "004";
        final String errorDetail = "Random error";
        String reversePmodeKey = "reverseKey";

        final List<UserMessage> fragments = new ArrayList<>();
        fragments.add(fragment);

        new Expectations() {{
            messageGroupDao.findByGroupId(groupId);
            result = messageGroupEntity;

            messagingDao.findUserMessageByGroupId(groupId);
            result = fragments;

            fragment.getMessageInfo().getMessageId();
            result = fragmentId;

            pModeProvider.findUserMessageExchangeContext(fragment, MSHRole.RECEIVING);
            result = userMessageExchangeContext;

            userMessageExchangeContext.getReversePmodeKey();
            result = reversePmodeKey;
        }};


        splitAndJoinDefaultService.splitAndJoinReceiveFailed(groupId, groupId, ebMS3ErrorCode, errorDetail);

        new Verifications() {{
            messageGroupEntity.setRejected(true);
            messageGroupDao.update(messageGroupEntity);

            List<String> messageIds = null;
            messageRetentionService.scheduleDeleteMessages(messageIds = withCapture());
            Assert.assertTrue(messageIds.contains(fragmentId));

            userMessageDefaultService.scheduleSendingSignalError(groupId, ebMS3ErrorCode, errorDetail, reversePmodeKey);
        }};
    }

    @Test
    public void incrementSentFragments(@Injectable MessageGroupEntity messageGroupEntity) {
        String groupId = "123";

        new Expectations() {{
            messageGroupDao.findByGroupId(groupId);
            result = messageGroupEntity;
        }};

        splitAndJoinDefaultService.incrementSentFragments(groupId);

        new Verifications() {{
            messageGroupDao.update(messageGroupEntity);
            messageGroupEntity.incrementSentFragments();

        }};
    }

    @Test
    public void incrementReceivedFragments(@Injectable MessageGroupEntity messageGroupEntity) {
        String groupId = "123";
        String backendName = "mybackend";

        new Expectations() {{
            messageGroupEntity.getGroupId();
            result = groupId;

            messageGroupDao.findByGroupId(groupId);
            result = messageGroupEntity;

            messageGroupEntity.getReceivedFragments();
            result = 2;

            messageGroupEntity.getFragmentCount();
            result = 2;
        }};

        splitAndJoinDefaultService.incrementReceivedFragments(groupId, backendName);

        new Verifications() {{
            messageGroupEntity.incrementReceivedFragments();
            messageGroupDao.update(messageGroupEntity);
            userMessageService.scheduleSourceMessageRejoinFile(groupId, backendName);

        }};
    }

    @Test
    // Note for running this test on Mac OS with JDK 8: before trying to fix this test or marking it as @Ignored,
    // ensure that you have the ".mime.types" file in your user home folder (please check the JDK implementation
    // for Mac OS sun.nio.fs.MacOSXFileSystemProvider); this file is used to determine the MIME type of files from
    // their extensions when the call to Files#probeContentType(Path) is made below.
    //
    // You can also append the mapping for the ZIP extension used below with the following command:
    // $ echo "application/zip					zip" >> ~/.mime.types
    public void compressAndDecompressSourceMessage() throws IOException {
        File sourceFile = testFolder.newFile("file.txt");
        FileUtils.writeStringToFile(sourceFile, "mycontent", Charset.defaultCharset());

        final File file = splitAndJoinDefaultService.compressSourceMessage(sourceFile.getAbsolutePath());
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.getAbsolutePath().endsWith(".zip"));
        Assert.assertTrue(Files.probeContentType(file.toPath()).contains("zip"));
    }

    @Test
    public void splitSourceMessage() throws IOException {
        File tempFile = testFolder.newFile("file.txt");
        final File storageDirectory = testFolder.getRoot();

        new Expectations(splitAndJoinDefaultService) {{
            splitAndJoinDefaultService.getFragmentStorageDirectory();
            result = storageDirectory;
        }};


        byte[] b = new byte[2058576];
        new Random().nextBytes(b);
        FileUtils.writeByteArrayToFile(tempFile, b);

        final List<String> fragmentFiles = splitAndJoinDefaultService.splitSourceMessage(tempFile, 1);
        Assert.assertEquals(fragmentFiles.size(), 2);
        Assert.assertTrue(fragmentFiles.stream().anyMatch(s -> s.contains("file.txt_1")));
        Assert.assertTrue(fragmentFiles.stream().anyMatch(s -> s.contains("file.txt_2")));
    }

    @Test
    public void createContentType() {
        String boundary = "myboundary";
        String start = "mystart";

        final String contentType = splitAndJoinDefaultService.createContentType(boundary, start);
        Assert.assertEquals("multipart/related; type=\"application/soap+xml\"; boundary=" + boundary + "; start=" + start + "; start-info=\"application/soap+xml\"", contentType);

    }

    @Test
    public void mergeSourceFile(@Injectable MessageGroupEntity messageGroupEntity,
                                @Injectable Domain domain) throws IOException {
        List<File> fragmentFilesInOrder = new ArrayList<>();
        final File file1 = testFolder.newFile("file1.txt");
        FileUtils.writeStringToFile(file1, "text1", Charset.defaultCharset());

        final File file2 = testFolder.newFile("file2.txt");
        FileUtils.writeStringToFile(file2, "text2", Charset.defaultCharset());

        fragmentFilesInOrder.add(file1);
        fragmentFilesInOrder.add(file2);
        final File temporaryDirectoryLocation = testFolder.getRoot();

        final File sourceFile = testFolder.newFile("sourceFile.txt");
        String sourceFileName = sourceFile.getAbsolutePath();

        new Expectations(splitAndJoinDefaultService) {{
            domibusPropertyProvider.getProperty(PayloadFileStorage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
            result = temporaryDirectoryLocation.getAbsolutePath();

            splitAndJoinDefaultService.generateSourceFileName(temporaryDirectoryLocation.getAbsolutePath());
            result = sourceFileName;

            splitAndJoinDefaultService.isSourceMessageCompressed(messageGroupEntity);
            result = false;

            splitAndJoinDefaultService.mergeFiles(fragmentFilesInOrder, (OutputStream) any);

        }};

        final File result = splitAndJoinDefaultService.mergeSourceFile(fragmentFilesInOrder, messageGroupEntity);
        new Verifications() {{
            OutputStream outputStream = null;
            splitAndJoinDefaultService.mergeFiles(fragmentFilesInOrder, outputStream = withCapture());
            Assert.assertTrue(outputStream instanceof FileOutputStream);
        }};
    }

    @Test
    public void isSourceMessageCompressed(@Injectable MessageGroupEntity messageGroupEntity) {
        new Expectations() {{
            messageGroupEntity.getCompressionAlgorithm();
            result = "application/zip";
        }};

        final boolean sourceMessageCompressed = splitAndJoinDefaultService.isSourceMessageCompressed(messageGroupEntity);
        Assert.assertTrue(sourceMessageCompressed);
    }

    @Test
    public void decompressGzip() throws IOException {
        final File file1 = testFolder.newFile("file1.txt");
        final String text1 = "text1";
        FileUtils.writeStringToFile(file1, text1, Charset.defaultCharset());
        final File compressSourceMessage = splitAndJoinDefaultService.compressSourceMessage(file1.getAbsolutePath());

        final File decompressed = testFolder.newFile("file1_decompressed.txt");
        splitAndJoinDefaultService.decompressGzip(compressSourceMessage, decompressed);
        Assert.assertEquals(text1, FileUtils.readFileToString(decompressed, Charset.defaultCharset()));
    }

    @Test
    public void getUserMessage(@Injectable FileInputStream fileInputStream,
                               @Injectable InputStream inputStream,
                               @Injectable MessageImpl messageImpl,
                               @Injectable MessageGroupEntity messageGroupEntity,
                               @Injectable final SOAPMessage soapMessage) throws IOException, SAXException, ParserConfigurationException, SOAPException, TransformerException {
        File sourceMessageFileName = testFolder.newFile("file1.txt");
        final String text1 = "text1";
        FileUtils.writeStringToFile(sourceMessageFileName, text1, Charset.defaultCharset());
        String contentTypeString = "application/xml";
        final File temporaryDirectoryLocation = testFolder.getRoot();

        new Expectations(splitAndJoinDefaultService) {{
            domibusPropertyProvider.getProperty(PayloadFileStorage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
            result = temporaryDirectoryLocation.getAbsolutePath();
        }};

        Assert.assertNotNull(splitAndJoinDefaultService.getUserMessage(sourceMessageFileName, contentTypeString));
    }


    @Test
    public void mergeFilesTest(@Mocked File file1,
                               @Mocked File file2,
                               @Injectable OutputStream mergingStream,
                               @Injectable Files files,
                               @Injectable Path path) throws IOException {
        List<File> filesList = new ArrayList<>();
        filesList.add(file1);
        filesList.add(file2);
        new Expectations(splitAndJoinDefaultService) {
            {
                file1.toPath();
                result = path;
            }
        };
        splitAndJoinDefaultService.mergeFiles(filesList, mergingStream);
        new Verifications() {{
            files.copy(path, mergingStream);
            times = 2;
            mergingStream.flush();
            times = 2;
        }};

    }
}