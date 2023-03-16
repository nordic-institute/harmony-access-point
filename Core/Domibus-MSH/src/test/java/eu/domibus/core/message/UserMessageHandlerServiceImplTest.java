package eu.domibus.core.message;

import eu.domibus.api.ebms3.model.*;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageHeaderType;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.dictionary.PartPropertyDictionaryService;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.receipt.AS4ReceiptService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.payload.persistence.InvalidPayloadSizeException;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.plugin.validation.SubmissionValidatorService;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.core.util.TimestampDateFormatter;
import eu.domibus.plugin.exception.PluginMessageReceiveException;
import eu.domibus.plugin.validation.SubmissionValidationException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static eu.domibus.core.message.UserMessageContextKeyProvider.BACKEND_FILTER;
import static eu.domibus.core.message.UserMessageContextKeyProvider.USER_MESSAGE;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @author Catalin Enache
 * @since 3.3
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
@RunWith(JMockit.class)
public class UserMessageHandlerServiceImplTest {

    @Injectable
    SubmissionValidatorService submissionValidatorService;

    @Injectable
    XMLUtil xmlUtil;

    @Injectable
    SoapUtil soapUtil;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    RoutingService routingService;

    @Injectable
    protected NonRepudiationService nonRepudiationService;

    @Injectable
    MessagingService messagingService;

    @Injectable
    SignalMessageDao signalMessageDao;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Injectable
    MessageFactory messageFactory;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    UserMessageLogDefaultService userMessageLogService;

    @Injectable
    JAXBContext jaxbContextEBMS;

    @Injectable
    TransformerFactory transformerFactory;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    TimestampDateFormatter timestampDateFormatter;

    @Injectable
    CompressionService compressionService;

    @Injectable
    MessageIdGenerator messageIdGenerator;

    @Injectable
    PayloadProfileValidator payloadProfileValidator;

    @Injectable
    PropertyProfileValidator propertyProfileValidator;

    @Injectable
    CertificateService certificateService;

    @Injectable
    SOAPMessage soapRequestMessage;

    @Injectable
    SOAPMessage soapResponseMessage;

    @Injectable
    UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Injectable
    protected MessageUtil messageUtil;

    @Injectable
    protected MessageGroupDao messageGroupDao;

    @Injectable
    protected UserMessageDefaultService userMessageService;

    @Injectable
    protected AS4ReceiptService as4ReceiptService;

    @Injectable
    DomainTaskExecutor domainTaskExecutor;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    SplitAndJoinService splitAndJoinService;

    @Injectable
    PayloadFileStorageProvider storageProvider;

    @Injectable
    MessagePropertyValidator messagePropertyValidator;

    @Injectable
    PartInfoServiceImpl partInfoService;

    @Injectable
    PartPropertyDictionaryService partPropertyDictionaryService;

    @Injectable
    MshRoleDao mshRoleDao;

    @Injectable
    MessageFragmentDao messageFragmentDao;

    @Tested
    UserMessageHandlerServiceImpl userMessageHandlerService;

    @Injectable
    PModeDefaultService pModeDefaultService;

    @Injectable
    PullMessageService pullMessageService;

    @Injectable
    UserMessagePersistenceService userMessagePersistenceService;

    @Injectable
    protected UserMessageContextKeyProvider userMessageContextKeyProvider;

    @Injectable
    protected LegConfiguration legConfiguration;

    @Injectable
    protected UserMessage userMessage;

    @Injectable
    protected BackendFilter matchingBackendFilter;

    @Injectable
    protected SOAPMessage request;

    @Injectable
    protected UserMessageLog userMessageLog;

    @Injectable
    protected Ebms3MessageFragmentType ebms3MessageFragmentType;

    @Injectable
    protected Ebms3MessageHeaderType ebms3MessageHeaderType;
    @Injectable
    protected MessageGroupEntity messageGroupEntity;

    @Injectable
    protected Reliability reliability;

    @Injectable
    protected Party to;
    String pmodeKey = "pmodeKey";

    private static final String STRING_TYPE = "string";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String FINAL_RECEIPIENT_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";


    protected MessageProperty createMessageProperty(String name, String value) {
        MessageProperty aProperty = new MessageProperty();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(UserMessageHandlerServiceImplTest.STRING_TYPE);
        return aProperty;
    }


    @Test
    public void testInvoke_tc1Process_HappyFlow() throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations() {{

            pModeProvider.checkSelfSending(pmodeKey);
            result = false;

            userMessage.getMessageId();
            result = "messageId";

            routingService.getMatchingBackendFilter(userMessage);
            result = matchingBackendFilter;

            pModeProvider.checkSelfSending(pmodeKey);
            result = false;

            legConfiguration.getReliability().getReplyPattern();
            result = ReplyPattern.RESPONSE;

            legConfiguration.getReliability().isNonRepudiation();
            result = true;

            as4ReceiptService.generateReceipt(
                    soapRequestMessage,
                    userMessage,
                    ReplyPattern.RESPONSE,
                    true,
                    false,
                    false);
            result = soapResponseMessage;

            as4ReceiptService.generateResponse(soapResponseMessage, false);
            result = new SignalMessageResult();
        }};

        userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, false);

        new Verifications() {{
            soapUtil.logMessage(soapRequestMessage);
            times = 1;

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

            userMessageContextKeyProvider.setObjectOnTheCurrentMessage(BACKEND_FILTER, matchingBackendFilter);
            times = 1;

            userMessageContextKeyProvider.setObjectOnTheCurrentMessage(USER_MESSAGE, userMessage);
            times = 1;

        }};
    }

    @Test
    public void testInvoke_tc1Process_SelfSending_HappyFlow() throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            routingService.getMatchingBackendFilter(userMessage);
            result = matchingBackendFilter;

            matchingBackendFilter.getBackendName();
            result = "backEndName";

            userMessage.getMessageId();
            result = "1234";

            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, "backEndName", null);
            result = "persist";

        }};

        userMessageHandlerService.handleIncomingMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, false, false, null);

        new Verifications() {{
            soapUtil.logMessage(soapRequestMessage);

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

        }};
    }

    @Test
    public void testInvoke_tc1Process_SelfSending_HappyFlow_withFragment() throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations() {{
            matchingBackendFilter.getBackendName();
            result = "backEndName";

            userMessage.getMessageId();
            result = "1234";

            partInfoService.checkPartInfoCharset(userMessage, null);
            times = 1;

        }};

        userMessageHandlerService.handleIncomingMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, new Ebms3MessageFragmentType(), null, false, false, null);

        new Verifications() {{
            soapUtil.logMessage(soapRequestMessage);

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

            splitAndJoinService.incrementReceivedFragments(null, "backEndName");
            times = 1;

        }};
    }

    @Test
    public void testInvoke_TestMessage()
            throws EbMS3Exception, TransformerException, IOException, SOAPException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        boolean selfSending = false;

        new Expectations() {{
            pModeProvider.checkSelfSending(pmodeKey);
            result = false;

            userMessage.getMessageId();
            result = "TestMessage123";

            partInfoService.checkPartInfoCharset(userMessage, null);
            times = 1;

            pModeProvider.checkSelfSending(pmodeKey);
            result = selfSending;
            times = 1;

            legConfiguration.getReliability().getReplyPattern();
            result = ReplyPattern.RESPONSE;

            legConfiguration.getReliability().isNonRepudiation();
            result = true;

            as4ReceiptService.generateReceipt(soapRequestMessage, userMessage, ReplyPattern.RESPONSE, true, false, selfSending);
            result = soapResponseMessage;

            as4ReceiptService.generateResponse(soapResponseMessage, false);
            result = new SignalMessageResult();
        }};

        userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, true);

        new Verifications() {{
            soapUtil.logMessage(soapRequestMessage);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

    public static Ebms3PartInfo getPartInfo(String name, String value, String href) {
        Ebms3PartInfo partInfo = new Ebms3PartInfo();
        Ebms3PartProperties partProperties = new Ebms3PartProperties();
        partProperties.setProperty(new HashSet<>());
        partProperties.getProperty().add(createProperty(name, value));
        partInfo.setPartProperties(partProperties);
        partInfo.setHref(href);
        return partInfo;
    }

    protected static Ebms3Property createProperty(String name, String value) {
        Ebms3Property aProperty = new Ebms3Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(STRING_TYPE);
        return aProperty;
    }


    @Test
    public void testPersistReceivedMessage_ValidationException()
            throws EbMS3Exception {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations() {{
            userMessage.getMessageId();
            result = "messageId";

            compressionService.handleDecompression(userMessage, null, legConfiguration);

            propertyProfileValidator.validate(userMessage, pmodeKey);
            result = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("Property missing exception")
                    .refToMessageId("Message Id")
                    .build();
        }};

        try {
            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, "", null);
            fail();
        } catch (Exception e) {
            Assert.assertTrue("Expecting Ebms3 exception", e instanceof EbMS3Exception);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, ((EbMS3Exception) e).getErrorCode());
        }

        new FullVerifications() {{
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            propertyProfileValidator.validate(userMessage, pmodeKey);
        }};
    }

    @Test
    public void testPersistReceivedMessage_CompressionError() throws EbMS3Exception {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations() {{
            compressionService.handleDecompression(userMessage, null, legConfiguration);

            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, anyString);
            result = new CompressionException("Could not store binary data for message ", null);

            userMessage.getMessageId();
            result = "TestMessageId123";
        }};
        try {
            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, "", null);
            fail("Exception for compression failure expected!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0303, e.getErrorCode());
        }

        new FullVerifications() {{
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            propertyProfileValidator.validate(userMessage, pmodeKey);
            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, anyString);
            userMessageLogDao.create((UserMessageLog) any);
            times = 0;
        }};
    }

    @Test
    public void testCheckDuplicate_true() {
        new Expectations() {{
            userMessageLogDao.findByMessageId(withSubstring("1234"), MSHRole.RECEIVING);
            result = userMessageLog;

        }};
        Messaging messaging1 = new Messaging();
        UserMessage userMessage1 = new UserMessage();
        userMessage1.setMessageId("1234");
        messaging1.setUserMessage(userMessage1);
        Assert.assertTrue("Expecting match in duplicate check", userMessageHandlerService.checkDuplicate(userMessage1));

        new FullVerifications() {
        };
    }

    @Test
    public void testCheckDuplicate_false() {
        new Expectations() {{
            userMessageLogDao.findByMessageId(anyString, MSHRole.RECEIVING);
            result = null;
        }};

        Messaging messaging2 = new Messaging();
        UserMessage userMessage2 = new UserMessage();
        userMessage2.setMessageId("4567");
        messaging2.setUserMessage(userMessage2);
        Assert.assertFalse("Expecting not duplicate result", userMessageHandlerService.checkDuplicate(userMessage2));

        new FullVerifications() {
        };
    }

    @Test
    public void testInvoke_ErrorInNotifyingIncomingMessage()
            throws EbMS3Exception, TransformerException, IOException, SOAPException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{

            pModeProvider.checkSelfSending(pmodeKey);
            result = false;

            legConfiguration.getReliability().getReplyPattern();
            result = ReplyPattern.RESPONSE;

            legConfiguration.getReliability().isNonRepudiation();
            result = true;

            final SOAPMessage responseMessage = as4ReceiptService.generateReceipt(
                    soapRequestMessage,
                    userMessage,
                    ReplyPattern.RESPONSE,
                    true,
                    false,
                    false);

            as4ReceiptService.generateResponse(responseMessage, false);
            result = new SignalMessageResult();
            userMessage.getMessageId();
            result = "TestMessage123";

            routingService.getMatchingBackendFilter(userMessage);
            result = matchingBackendFilter;

            matchingBackendFilter.getBackendName();
            result = "matchingBackendFilter";

            legConfiguration.getReliability();
            result = reliability;

            reliability.getReplyPattern();
            result = ReplyPattern.RESPONSE;

            reliability.isNonRepudiation();
            result = true;

            pModeProvider.checkSelfSending(pmodeKey);
            result = false;

            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
            result = new PluginMessageReceiveException("Error while submitting the message!!", ErrorCode.EbMS3ErrorCode.EBMS_0004);
        }};
        try {
            userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, false);
            fail();
        } catch (EbMS3Exception e) {
            // OK
        }

        new Verifications() {{
            soapUtil.logMessage(soapRequestMessage);
            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

    protected UserMessage createSampleUserMessage() {
        UserMessage userMessage = new UserMessage();

        ServiceEntity service1 = new ServiceEntity();
        service1.setValue("bdx:noprocess");
        service1.setType("tc1");
        userMessage.setService(service1);

        ActionEntity action = new ActionEntity();
        action.setValue("TC1Leg1");
        userMessage.setAction(action);

        AgreementRefEntity agreementRef1 = new AgreementRefEntity();
        agreementRef1.setValue("");
        agreementRef1.setType("");
        userMessage.setAgreementRef(agreementRef1);

        HashSet<MessageProperty> messageProperties = new HashSet<>();
        messageProperties.add(createMessageProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1"));
        messageProperties.add(createMessageProperty("finalRecipient", FINAL_RECEIPIENT_VALUE));
        userMessage.setMessageProperties(messageProperties);
        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        PartyRole role = getPartyRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        from.setFromRole(role);

        PartyId sender = new PartyId();
        sender.setValue(BLUE);
        sender.setType(DEF_PARTY_TYPE);
        from.setFromPartyId(sender);
        partyInfo.setFrom(from);

        To to = new To();
        to.setToRole(getPartyRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder"));

        PartyId receiver = new PartyId();
        receiver.setValue(RED);
        receiver.setType(DEF_PARTY_TYPE);
        to.setToPartyId(receiver);
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

//        PayloadInfo payloadInfo = new PayloadInfo();
//        PartInfo partInfo = new PartInfo();
//        partInfo.setHref("cid:message");
//
//        PartProperties partProperties = new PartProperties();
//        partProperties.getProperty().add(createProperty("text/xml", "MimeType"));
//        HashSet<PartProperty> partProperties1 = new HashSet<>();
//        partProperties1.add(new PartProperty())
//        partInfo.setPartProperties(partProperties1);

//        payloadInfo.getPartInfo().add(partInfo);
//        userMessage.setPayloadInfo(payloadInfo);
        return userMessage;
    }

    private PartyRole getPartyRole(String value) {
        PartyRole role = new PartyRole();
        role.setValue(value);
        return role;
    }

    @Test
    public void testHandleIncomingSourceMessage() throws TransformerException, EbMS3Exception, IOException {

        boolean messageExists = false;
        boolean testMessage = false;
        String backendName = "mybackend";

        new Expectations(userMessageHandlerService) {{
            userMessage.getMessageId();
            result = "messageID";

            routingService.getMatchingBackendFilter(userMessage);
            result = matchingBackendFilter;

            matchingBackendFilter.getBackendName();
            result = backendName;

            userMessageHandlerService.persistReceivedSourceMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, null, null);
            result = "persists";
        }};

        userMessageHandlerService.handleIncomingSourceMessage(legConfiguration, pmodeKey, request, userMessage, null, messageExists, testMessage);

        new Verifications() {{
//            userMessage.setSourceMessage(true);
//
            partInfoService.checkPartInfoCharset(userMessage, null);
//            backendNotificationService.notifyMessageReceived(backendFilter, userMessage, null);
            soapUtil.logMessage(request);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

    @Test
    public void testPersistReceivedSourceMessage() throws EbMS3Exception {
        String backendName = "mybackend";

        new Expectations(userMessageHandlerService) {{

            userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, ebms3MessageFragmentType, backendName, userMessage, null, null);
            result = "received";
        }};

        userMessageHandlerService.persistReceivedSourceMessage(request, legConfiguration, pmodeKey, ebms3MessageFragmentType, backendName, userMessage, null, null);

        new FullVerifications() {{
            userMessage.setSourceMessage(true);
        }};
    }

    @Test
    public void testSaveReceivedMessage_exceptionCompressionException() throws EbMS3Exception {
        String backendName = "mybackend";
        String messageId = "123";

        new Expectations() {{
            userMessage.getMessageId();
            result = messageId;

            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, backendName);
            result = new CompressionException();
        }};

        try {
            userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, null, null);
            fail();
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0303, e.getErrorCode());
        }

        new FullVerifications() {{
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            times = 1;

            propertyProfileValidator.validate(userMessage, pmodeKey);
            times = 1;
        }};

    }

    @Test
    public void testSaveReceivedMessage_exceptionInvalidPayloadSizeException_persisted() throws EbMS3Exception {
        String backendName = "mybackend";
        String messageId = "123";

        new Expectations() {{
            userMessage.getMessageId();
            result = messageId;

            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, backendName);
            result = new InvalidPayloadSizeException("ERROR");

            storageProvider.isPayloadsPersistenceFileSystemConfigured();
            result = true;

            legConfiguration.getPayloadProfile().getMaxSize();
            result = 10L;

            legConfiguration.getPayloadProfile().getName();
            result = "payloadProfileName";
        }};

        try {
            userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, null, null);
            fail();
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, e.getErrorCode());
        }

        new FullVerifications() {{
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            times = 1;

            propertyProfileValidator.validate(userMessage, pmodeKey);
            times = 1;

            partInfoService.clearFileSystemPayloads(null);
            times = 1;

        }};

    }

    @Test
    public void testSaveReceivedMessage_exceptionInvalidPayloadSizeException_NotPersisted() throws EbMS3Exception {
        String backendName = "mybackend";
        String messageId = "123";

        new Expectations() {{
            userMessage.getMessageId();
            result = messageId;

            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, backendName);
            result = new InvalidPayloadSizeException("ERROR");

            storageProvider.isPayloadsPersistenceFileSystemConfigured();
            result = false;

            legConfiguration.getPayloadProfile().getMaxSize();
            result = 10L;

            legConfiguration.getPayloadProfile().getName();
            result = "payloadProfileName";

        }};

        try {
            userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, null, null);
            fail();
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, e.getErrorCode());
        }

        new FullVerifications() {{
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            times = 1;

            propertyProfileValidator.validate(userMessage, pmodeKey);
            times = 1;
        }};

    }


    @Test
    public void testHandleMessageFragmentWithGroupAlreadyExisting() throws EbMS3Exception {
        new Expectations(userMessageHandlerService) {{
            ebms3MessageFragmentType.getGroupId();
            result = "groupId";

            messageGroupDao.findByGroupId("groupId");
            result = messageGroupEntity;
            times = 1;

            userMessageHandlerService.validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);
            times = 1;

            ebms3MessageFragmentType.getFragmentNum();
            result = 41L;

//            userMessageHandlerService.addPartInfoFromFragment(userMessage, ebms3MessageFragmentType);
//            times = 1;
        }};

        userMessageHandlerService.handleMessageFragment(userMessage, ebms3MessageFragmentType, legConfiguration);

        new Verifications() {{

        }};
    }

    @Test
    public void testHandleMessageFragment_createMessageGroup() throws EbMS3Exception {
        new Expectations(userMessageHandlerService) {{
            ebms3MessageFragmentType.getGroupId();
            result = "groupId";

            messageGroupDao.findByGroupId("groupId");
            result = null;
            times = 1;

            ebms3MessageFragmentType.getMessageHeader();
            result = ebms3MessageHeaderType;

            ebms3MessageHeaderType.getStart();
            result = "Start";

            ebms3MessageHeaderType.getBoundary();
            result = "Boundary";

            ebms3MessageFragmentType.getAction();
            result = "action";

            ebms3MessageFragmentType.getCompressionAlgorithm();
            result = "compressionAlgorithm";

            ebms3MessageFragmentType.getMessageSize();
            result = BigInteger.TEN;

            ebms3MessageFragmentType.getCompressedMessageSize();
            result = BigInteger.TEN;

            ebms3MessageFragmentType.getGroupId();
            result = "groupId";

            ebms3MessageFragmentType.getFragmentCount();
            result = 5L;

            userMessage.toString();
            result = "userMessage";

            userMessageHandlerService.validateUserMessageFragment(userMessage, (MessageGroupEntity) any, ebms3MessageFragmentType, legConfiguration);
            times = 1;

            ebms3MessageFragmentType.getFragmentNum();
            result = 41L;

//            userMessageHandlerService.addPartInfoFromFragment(userMessage, ebms3MessageFragmentType);
//            times = 1;
        }};

        userMessageHandlerService.handleMessageFragment(userMessage, ebms3MessageFragmentType, legConfiguration);

        new Verifications() {{
            messageGroupDao.create((MessageGroupEntity) any);
            times = 1;

        }};
    }

    @Test
    public void testValidateUserMessageFragmentWithNoSplittingConfigured() {
        new Expectations() {{
            legConfiguration.getSplitting();
            result = null;

            legConfiguration.getName();
            result = "legName";

            userMessage.getMessageId();
            result = "messageId";
        }};

        try {
            userMessageHandlerService.validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);
            fail("Not possible to use SplitAndJoin without PMode leg configuration");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0002, e.getErrorCode());
        }

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateUserMessageFragmentWithDatabaseStorage() {
        new Expectations() {{
            legConfiguration.getSplitting();
            result = new Splitting();

            storageProvider.isPayloadsPersistenceInDatabaseConfigured();
            result = true;

            userMessage.getMessageId();
            result = "messageId";
        }};

        try {
            userMessageHandlerService.validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);
            fail("Not possible to use SplitAndJoin with database payloads");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0002, e.getErrorCode());
        }

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateUserMessageFragmentWithRejectedGroup() {
        new Expectations() {{
            legConfiguration.getSplitting();
            result = new Splitting();

            storageProvider.isPayloadsPersistenceInDatabaseConfigured();
            result = false;

            ebms3MessageFragmentType.getGroupId();
            result = "groupId";

            messageGroupEntity.getExpired();
            result = false;

            messageGroupEntity.getRejected();
            result = true;

            userMessage.getMessageId();
            result = "messageId";
        }};

        try {
            userMessageHandlerService.validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);
            fail();
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0040, e.getErrorCode());
        }

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateUserMessageFragmentWithExpired() {
        new Expectations() {{
            legConfiguration.getSplitting();
            result = new Splitting();

            storageProvider.isPayloadsPersistenceInDatabaseConfigured();
            result = false;

            ebms3MessageFragmentType.getGroupId();
            result = "groupId";

            messageGroupEntity.getExpired();
            result = true;

            userMessage.getMessageId();
            result = "messageId";
        }};

        try {
            userMessageHandlerService.validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);
            fail();
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0051, e.getErrorCode());
        }

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateUserMessageFragmentNoMessageGroupEntity() throws EbMS3Exception {
        new Expectations() {{
            legConfiguration.getSplitting();
            result = new Splitting();

            storageProvider.isPayloadsPersistenceInDatabaseConfigured();
            result = false;

            ebms3MessageFragmentType.getGroupId();
            result = "groupId";

            userMessage.getMessageId();
            result = "messageId";
        }};


        userMessageHandlerService.validateUserMessageFragment(userMessage, null, ebms3MessageFragmentType, legConfiguration);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateUserMessageFragment_ok()
            throws EbMS3Exception {

        new Expectations() {{
            legConfiguration.getSplitting();
            result = new Splitting();

            storageProvider.isPayloadsPersistenceInDatabaseConfigured();
            result = false;
            ebms3MessageFragmentType.getGroupId();
            result = "groupId";

            messageGroupEntity.getRejected();
            result = false;

            messageGroupEntity.getExpired();
            result = false;

            messageGroupEntity.getFragmentCount();
            result = null;
        }};

        userMessageHandlerService.validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateUserMessageFragmentWithWrongFragmentsCount() {

        long totalFragmentCount = 5;

        new Expectations() {{
            legConfiguration.getSplitting();
            result = new Splitting();

            storageProvider.isPayloadsPersistenceInDatabaseConfigured();
            result = false;

            ebms3MessageFragmentType.getGroupId();
            result = "groupId";

            messageGroupEntity.getRejected();
            result = false;

            messageGroupEntity.getExpired();
            result = false;

            messageGroupEntity.getFragmentCount();
            result = totalFragmentCount;

            ebms3MessageFragmentType.getFragmentCount();
            result = 7;

            userMessage.getMessageId();
            result = "messageId";
        }};

        try {
            userMessageHandlerService.validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);
            fail();
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0048, e.getErrorCode());
        }

        new FullVerifications() {
        };
    }

    @Test
    public void handleNewSourceUserMessageTest() throws IOException, EbMS3Exception, TransformerException {

        String pmodeKey = "pmodeKey";
        boolean testMessage = true;
        boolean messageExists = true;

        new Expectations() {{
            userMessage.getMessageId();
            result = "TestMessageId";

            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = true;

            userMessageLogDao.findByMessageId(userMessage.getMessageId(), MSHRole.RECEIVING);
            result = new UserMessageLog();

        }};

        userMessageHandlerService.handleNewSourceUserMessage(legConfiguration, pmodeKey, request, userMessage, null, testMessage);

        new FullVerifications() {{
            partInfoService.checkPartInfoCharset(userMessage, null);
            times = 1;

            userMessageHandlerService.handleIncomingSourceMessage(legConfiguration, pmodeKey, request, userMessage, null, messageExists, testMessage);
            times = 1;
            soapUtil.logMessage(request);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

}
