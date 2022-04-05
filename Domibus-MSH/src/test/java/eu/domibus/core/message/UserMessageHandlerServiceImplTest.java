package eu.domibus.core.message;

import eu.domibus.api.ebms3.Ebms3Constants;
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
import eu.domibus.plugin.validation.SubmissionValidationException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static eu.domibus.common.ErrorCode.EBMS_0001;
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
    public void testInvoke_tc1Process_HappyFlow(@Injectable final BackendFilter matchingBackendFilter,
                                                @Injectable Ebms3MessageFragmentType messageFragment,
                                                @Injectable LegConfiguration legConfiguration,
                                                @Injectable UserMessage userMessage) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{

            pModeProvider.checkSelfSending(pmodeKey);
            result = false;

            userMessage.getMessageId();
            result = "messageId";

            routingService.getMatchingBackendFilter(userMessage);
            result = null;

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

            backendNotificationService.notifyMessageReceived(null, userMessage);
            times = 1;

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

        }};
    }

    @Test
    public void testInvoke_tc1Process_SelfSending_HappyFlow(@Injectable final BackendFilter matchingBackendFilter,
                                                            @Injectable Ebms3MessageFragmentType messageFragment,
                                                            @Injectable Reliability reliability,
                                                            @Injectable LegConfiguration legConfiguration,
                                                            @Injectable UserMessage userMessage) throws Exception {

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

        userMessageHandlerService.handleIncomingMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, true, false, false, null);

        new Verifications() {{
            soapUtil.logMessage(soapRequestMessage);

            String capturedId;
            userMessage.setMessageId(capturedId = withCapture());

            Assert.assertEquals("1234" + UserMessageHandlerService.SELF_SENDING_SUFFIX, capturedId);

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
            times = 1;

        }};
    }

    @Test
    public void testInvoke_tc1Process_SelfSending_HappyFlow_withFragment(@Injectable final BackendFilter matchingBackendFilter,
                                                                         @Injectable Ebms3MessageFragmentType messageFragment,
                                                                         @Injectable Reliability reliability,
                                                                         @Injectable LegConfiguration legConfiguration,
                                                                         @Injectable UserMessage userMessage,
                                                                         @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            matchingBackendFilter.getBackendName();
            result = "backEndName";

            userMessage.getMessageId();
            result = "1234";

            partInfoService.checkPartInfoCharset(userMessage, null);
            times = 1;

        }};

        userMessageHandlerService.handleIncomingMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, new Ebms3MessageFragmentType(), null, true, false, false, null);

        new Verifications() {{
            soapUtil.logMessage(soapRequestMessage);

            String capturedId;
            userMessage.setMessageId(capturedId = withCapture());

            Assert.assertEquals("1234" + UserMessageHandlerService.SELF_SENDING_SUFFIX, capturedId);

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
            times = 1;

            splitAndJoinService.incrementReceivedFragments(null, "backEndName");
            times = 1;

        }};
    }

    @Test
    public void testInvoke_TestMessage(@Injectable final BackendFilter matchingBackendFilter,
                                       @Injectable final LegConfiguration legConfiguration,
                                       @Injectable final UserMessage userMessage,
                                       @Injectable Ebms3MessageFragmentType messageFragment)
            throws EbMS3Exception, TransformerException, IOException, SOAPException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        boolean selfSending = false;

        new Expectations(userMessageHandlerService) {{
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

    @Test
    public void test_HandlePayLoads_HappyFlowUsingEmptyCID(@Injectable final Ebms3Messaging ebms3Messaging,
                                                           @Injectable final PartInfo partInfo) throws SOAPException, TransformerException, EbMS3Exception {

        Ebms3PartInfo ebms3PartInfo = new Ebms3PartInfo();
        ebms3PartInfo.setHref(null);
        Ebms3Description value1 = new Ebms3Description();
        value1.setValue("description");
        value1.setLang("en");
        ebms3PartInfo.setDescription(value1);
        Ebms3Schema value = new Ebms3Schema();
        value.setLocation("location");
        value.setNamespace("namespace");
        value.setVersion("version");
        ebms3PartInfo.setSchema(value);

        Ebms3PayloadInfo ebms3PayloadInfo = new Ebms3PayloadInfo();
        ebms3PayloadInfo.getPartInfo().add(ebms3PartInfo);
        new Expectations(userMessageHandlerService) {{
            ebms3Messaging.getUserMessage().getPayloadInfo();
            result = ebms3PayloadInfo;

            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            userMessageHandlerService.convert(ebms3PartInfo);
            result = partInfo;
        }};

        userMessageHandlerService.handlePayloads(soapRequestMessage, ebms3Messaging, null);

        new Verifications() {{
            partInfo.setInBody(true);
            partInfo.setPayloadDatahandler((DataHandler) any);
        }};
    }

    @Test
    public void test_HandlePayLoads_EmptyCIDAndBodyContent(@Injectable final Ebms3Messaging ebms3Messaging,
                                                           @Injectable final Node bodyContent,
                                                           @Injectable final PartInfo partInfo)
            throws SOAPException, TransformerException, EbMS3Exception {

        new Expectations() {{

            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            ebms3Messaging.getUserMessage().getPayloadInfo();
            result = null;

//            partInfo.getHref();
//            result = "";

//            soapRequestMessage.getSOAPBody().hasChildNodes();
//            result = false;

//            soapRequestMessage.getAttachments();
//            result = Collections.emptyIterator();

//            xmlUtil.getTransformerFactory().newTransformer();
//            result = null;
        }};

        userMessageHandlerService.handlePayloads(soapRequestMessage, ebms3Messaging, null);

        new FullVerifications() {{
//            partInfo.setPayloadDatahandler((DataHandler) any);
        }};
    }


    /**
     * A single message having multiple PartInfo's with no or special cid.
     */
    @Test
    public void test_HandlePayLoads_NullCIDMultiplePartInfo(
            @Injectable final Ebms3Messaging ebms3Messaging,
            @Injectable final Node bodyContent1,
            @Injectable final DataHandler dataHandler)
            throws SOAPException, TransformerException {

        Ebms3PartInfo part1 = getPartInfo("MimeType", "text/xml", "");
        Ebms3PartInfo part2 = getPartInfo("MimeType", "text/xml", "#1234");
        List<Ebms3PartInfo> ebms3PartInfos = Arrays.asList(
                part1,
                part2);

        List<Node> bodyContentNodeList = new ArrayList<>();
        bodyContentNodeList.add(bodyContent1);
        final Iterator<Node> bodyContentNodeIterator = bodyContentNodeList.iterator();

        new Expectations(userMessageHandlerService) {{

            soapRequestMessage.getSOAPBody().hasChildNodes();
            result = true;

            soapRequestMessage.getSOAPBody().getChildElements();
            result = bodyContentNodeIterator;

            userMessageHandlerService.getDataHandler((Node) any);
            result = dataHandler;

            soapRequestMessage.getAttachments();
            result = Collections.emptyIterator();

            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            ebms3Messaging.getUserMessage().getPayloadInfo().getPartInfo();
            result = ebms3PartInfos;

            partPropertyDictionaryService.findOrCreatePartProperty(anyString, anyString, anyString);
            result = new PartProperty();
            times = 2;
        }};

        try {
            userMessageHandlerService.handlePayloads(soapRequestMessage, ebms3Messaging, null);
            fail("Expecting error that - More than one Partinfo referencing the soap body found!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getErrorCode());
        }

        new FullVerifications() {
        };
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
    public void testPersistReceivedMessage_ValidationException(@Injectable final LegConfiguration legConfiguration,
                                                               @Injectable final UserMessage userMessage,
                                                               @Injectable final Ebms3Messaging ebms3Messaging,
                                                               @Injectable final UserMessageLog userMessageLog,
                                                               @Injectable Ebms3MessageFragmentType messageFragment)
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
    public void testPersistReceivedMessage_CompressionError(@Injectable final LegConfiguration legConfiguration,
                                                            @Injectable final UserMessage userMessage,
                                                            @Injectable final Party receiverParty,
                                                            @Injectable final UserMessageLog userMessageLog,
                                                            @Injectable Ebms3MessageFragmentType messageFragment)
            throws EbMS3Exception {
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
            userMessageLogDao.create(withAny(userMessageLog));
            times = 0;
        }};
    }

    @Test
    public void test_HandlePayLoads_HappyFlowUsingCID(@Injectable final UserMessage userMessage,
                                                      @Injectable final Ebms3Messaging ebms3Messaging,
                                                      @Injectable final AttachmentPart attachmentPart1,
                                                      @Injectable final AttachmentPart attachmentPart2,
                                                      @Injectable final DataHandler attachmentPart1DH,
                                                      @Injectable final DataHandler attachmentPart2DH) throws SOAPException, TransformerException, EbMS3Exception {

        final Ebms3PartInfo partInfo = new Ebms3PartInfo();
        partInfo.setHref("cid:message");

        Ebms3PartProperties partProperties = new Ebms3PartProperties();
        Ebms3Property property1 = new Ebms3Property();
        property1.setName("MimeType");
        property1.setValue("text/xml");

        partProperties.getProperties().add(property1);
        partInfo.setPartProperties(partProperties);

        List<AttachmentPart> attachmentPartList = new ArrayList<>();
        attachmentPartList.add(attachmentPart1);
        attachmentPartList.add(attachmentPart2);
        final Iterator<AttachmentPart> attachmentPartIterator = attachmentPartList.iterator();

        new Expectations() {{
            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            ebms3Messaging.getUserMessage().getPayloadInfo().getPartInfo();
            result = Arrays.asList(partInfo);

            soapRequestMessage.getAttachments();
            result = attachmentPartIterator;

            attachmentPart1.getContentId();
            result = "AnotherContentID";

            attachmentPart2.getContentId();
            result = "message";

            attachmentPart2.getDataHandler();
            result = attachmentPart2DH;
        }};

        userMessageHandlerService.handlePayloads(soapRequestMessage, ebms3Messaging, null);
//        Assert.assertNotNull(partInfo.getPayloadDatahandler());
//        assertNotNull(partInfo.getPayloadDatahandler());

        new Verifications() {{
//            attachmentPart1.setContentId(anyString);
//            attachmentPart2.setContentId(anyString);
        }};
    }

    @Test
    public void test_HandlePayLoads_NoPayloadFound(
            @Injectable final UserMessage userMessage,
            @Injectable final Ebms3Messaging ebms3Messaging,
            @Injectable final PartInfo partInfo,
            @Injectable final AttachmentPart attachmentPart1,
            @Injectable final AttachmentPart attachmentPart2) throws TransformerException, SOAPException {

        List<AttachmentPart> attachmentPartList = new ArrayList<>();
        attachmentPartList.add(attachmentPart1);
        attachmentPartList.add(attachmentPart2);
        final Iterator<AttachmentPart> attachmentPartIterator = attachmentPartList.iterator();

        new Expectations(userMessageHandlerService) {{
            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            userMessageHandlerService.getPartInfoList(ebms3Messaging);
            result = Arrays.asList(partInfo);

            partInfo.getHref();
            result = "cid:message";

            soapRequestMessage.getAttachments();
            result = attachmentPartIterator;

            attachmentPart1.getContentId();
            result = "AnotherContentID";

            attachmentPart2.getContentId();
            result = "message123";

        }};

        try {
            userMessageHandlerService.handlePayloads(soapRequestMessage, ebms3Messaging, null);
            fail("Expected Ebms3 exception that no matching payload was found!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0011, e.getErrorCode());
        }

        new FullVerifications() {{
            attachmentPart1.setContentId(anyString);
            attachmentPart2.setContentId(anyString);
        }};
    }

    @Test
    public void testCheckTestMessage_false() {
        UserMessage userMessage = createSampleUserMessage();

        Assert.assertFalse("Expecting false for test message as valid data message is supplied ", userMessageHandlerService.checkTestMessage(userMessage));
    }

    @Test
    public void testCheckTestMessage_true() {

        UserMessage userMessage = createSampleUserMessage();
        userMessage.getService().setValue(Ebms3Constants.TEST_SERVICE);
        userMessage.getAction().setValue(Ebms3Constants.TEST_ACTION);

        Assert.assertTrue("Expecting true for Check Test Message with modified data", userMessageHandlerService.checkTestMessage(userMessage));
    }

    @Test
    public void testGetFinalRecipientName() {
        final UserMessage userMessage = createSampleUserMessage();
        Assert.assertEquals(FINAL_RECEIPIENT_VALUE, userMessageHandlerService.getFinalRecipientName(userMessage));
    }

    @Test
    public void testGetFinalRecipientName_noProperties() {
        final UserMessage userMessage = createSampleUserMessage();
        userMessage.setMessageProperties(new HashSet<>());
        Assert.assertNull(userMessageHandlerService.getFinalRecipientName(userMessage));
    }

    @Test
    public void testCheckDuplicate_true(@Injectable final UserMessageLog userMessageLog) {
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
    public void testCheckDuplicate_false(@Injectable final UserMessageLog userMessageLog) {
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
    public void testInvoke_ErrorInNotifyingIncomingMessage(@Injectable final BackendFilter matchingBackendFilter,
                                                           @Injectable final LegConfiguration legConfiguration,
                                                           @Injectable final UserMessage userMessage,
                                                           @Injectable Ebms3MessageFragmentType messageFragment,
                                                           @Injectable Reliability reliability)
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
            result = new SubmissionValidationException("Error while submitting the message!!");
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
    public void testHandleIncomingSourceMessage(@Injectable final LegConfiguration legConfiguration,
                                                @Injectable final SOAPMessage request,
                                                @Injectable final UserMessage userMessage,
                                                @Injectable BackendFilter backendFilter
    ) throws TransformerException, EbMS3Exception, IOException {

        boolean messageExists = false;
        boolean testMessage = false;
        String backendName = "mybackend";

        new Expectations(userMessageHandlerService) {{
            userMessage.getMessageId();
            result = "messageID";

            routingService.getMatchingBackendFilter(userMessage);
            result = backendFilter;

            backendFilter.getBackendName();
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
    public void testPersistReceivedSourceMessage(@Injectable final LegConfiguration legConfiguration,
                                                 @Injectable final SOAPMessage request,
                                                 @Injectable final UserMessage userMessage,
                                                 @Injectable final Ebms3MessageFragmentType ebms3MessageFragmentType) throws EbMS3Exception {
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
    public void testSaveReceivedMessage_exceptionCompressionException(@Injectable final LegConfiguration legConfiguration,
                                                                      @Injectable final SOAPMessage request,
                                                                      @Injectable final Messaging messaging,
                                                                      @Injectable final UserMessage userMessage,
                                                                      @Injectable Party to) throws EbMS3Exception {
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
    public void testSaveReceivedMessage_exceptionInvalidPayloadSizeException_persisted(@Injectable final LegConfiguration legConfiguration,
                                                                                       @Injectable final SOAPMessage request,
                                                                                       @Injectable final Messaging messaging,
                                                                                       @Injectable final UserMessage userMessage,
                                                                                       @Injectable Party to) throws EbMS3Exception {
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
    public void testSaveReceivedMessage_exceptionInvalidPayloadSizeException_NotPersisted(@Injectable final LegConfiguration legConfiguration,
                                                                                          @Injectable final SOAPMessage request,
                                                                                          @Injectable final Messaging messaging,
                                                                                          @Injectable final UserMessage userMessage,
                                                                                          @Injectable Party to) throws EbMS3Exception {
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
    public void testHandleMessageFragmentWithGroupAlreadyExisting(@Injectable UserMessage userMessage,
                                                                  @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                                  @Injectable MessageGroupEntity messageGroupEntity,
                                                                  @Injectable LegConfiguration legConfiguration) throws EbMS3Exception {
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
    public void testHandleMessageFragment_createMessageGroup(@Injectable UserMessage userMessage,
                                                             @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                             @Injectable LegConfiguration legConfiguration,
                                                             @Injectable Ebms3MessageHeaderType ebms3MessageHeaderType) throws EbMS3Exception {
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
    public void testValidateUserMessageFragmentWithNoSplittingConfigured(@Injectable UserMessage userMessage,
                                                                         @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                                         @Injectable MessageGroupEntity messageGroupEntity,
                                                                         @Injectable LegConfiguration legConfiguration) {
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
    public void testValidateUserMessageFragmentWithDatabaseStorage(@Injectable UserMessage userMessage,
                                                                   @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                                   @Injectable MessageGroupEntity messageGroupEntity,
                                                                   @Injectable LegConfiguration legConfiguration) {
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
    public void testValidateUserMessageFragmentWithRejectedGroup(@Injectable UserMessage userMessage,
                                                                 @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                                 @Injectable MessageGroupEntity messageGroupEntity,
                                                                 @Injectable LegConfiguration legConfiguration) {
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
    public void testValidateUserMessageFragmentWithExpired(@Injectable UserMessage userMessage,
                                                           @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                           @Injectable MessageGroupEntity messageGroupEntity,
                                                           @Injectable LegConfiguration legConfiguration) {
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
    public void testValidateUserMessageFragmentNoMessageGroupEntity(@Injectable UserMessage userMessage,
                                                                    @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                                    @Injectable MessageGroupEntity messageGroupEntity,
                                                                    @Injectable LegConfiguration legConfiguration) throws EbMS3Exception {
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
    public void testValidateUserMessageFragment_ok(@Injectable UserMessage userMessage,
                                                   @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                   @Injectable MessageGroupEntity messageGroupEntity,
                                                   @Injectable LegConfiguration legConfiguration)
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
    public void testValidateUserMessageFragmentWithWrongFragmentsCount(@Injectable UserMessage userMessage,
                                                                       @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                                       @Injectable MessageGroupEntity messageGroupEntity,
                                                                       @Injectable LegConfiguration legConfiguration) {

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
    public void handleNewSourceUserMessageTest(@Injectable LegConfiguration legConfiguration,
                                               @Injectable UserMessage userMessage,
                                               @Injectable SOAPMessage request,
                                               @Injectable Messaging messaging) throws IOException, EbMS3Exception, TransformerException {

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

    @Test
    public void checkTestMessageTest(@Injectable LegConfiguration legConfiguration) {

        new Expectations() {{
            legConfiguration.getService().getValue();
            result = Ebms3Constants.TEST_SERVICE;

            legConfiguration.getAction().getValue();
            result = Ebms3Constants.TEST_ACTION;
        }};

        assertTrue(userMessageHandlerService.checkTestMessage(legConfiguration));

        new FullVerifications() {
        };
    }

    @Test
    public void checkTestMessageTest_true(@Injectable LegConfiguration legConfiguration) {

        new Expectations() {{
            legConfiguration.getService().getValue();
            result = "service";

            legConfiguration.getAction().getValue();
            result = "action";
        }};

        assertFalse(userMessageHandlerService.checkTestMessage(legConfiguration));

        new FullVerifications() {
        };
    }

    @Test
    public void checkTestMessageTest_noLeg() {
        assertFalse(userMessageHandlerService.checkTestMessage((LegConfiguration) null));
        new FullVerifications() {
        };
    }

    @Test
    public void createErrorResultTest(@Injectable EbMS3Exception ebm3Exception) {

        new Expectations() {{
            ebm3Exception.getRefToMessageId();
            result = "refToMessageId";

            ebm3Exception.getErrorCodeObject();
            result = EBMS_0001;

            ebm3Exception.getErrorDetail();
            result = "errorDetail";
        }};
        //when
        Assert.assertNotNull(userMessageHandlerService.createErrorResult(ebm3Exception));

        new FullVerifications() {
        };
    }
}
