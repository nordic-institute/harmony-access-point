package eu.domibus.core.message;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageHeaderType;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.usermessage.domain.PartProperties;
import eu.domibus.api.usermessage.domain.PayloadInfo;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
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
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.core.util.TimestampDateFormatter;
import eu.domibus.plugin.validation.SubmissionValidationException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    @Tested
    UserMessageHandlerServiceImpl userMessageHandlerService;

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
    protected UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    protected MessageUtil messageUtil;

    @Injectable
    protected MessageGroupDao messageGroupDao;

    @Injectable
    protected UserMessageService userMessageService;

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
    PartInfoDao partInfoDao;

    @Injectable
    PartPropertyDao partPropertyDao;

    @Injectable
    MshRoleDao mshRoleDao;

    @Injectable
    MessageFragmentDao messageFragmentDao;

    @Injectable
    MessagePropertyDao messagePropertyDao;

    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";
    private static final String STRING_TYPE = "string";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String FINAL_RECEIPIENT_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";

    protected eu.domibus.api.usermessage.domain.Property createProperty(String name, String value) {
        eu.domibus.api.usermessage.domain.Property aProperty = new eu.domibus.api.usermessage.domain.Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(UserMessageHandlerServiceImplTest.STRING_TYPE);
        return aProperty;
    }

    protected MessageProperty createMessageProperty(String name, String value) {
        MessageProperty aProperty = new MessageProperty();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(UserMessageHandlerServiceImplTest.STRING_TYPE);
        return aProperty;
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testCheckCharset_HappyFlow() throws EbMS3Exception {
        final Messaging messaging = new Messaging();
        UserMessage userMessage = new UserMessage();
        PayloadInfo payloadInfo = new PayloadInfo();
        eu.domibus.api.usermessage.domain.PartInfo partInfo = getPartInfo("MimeType", "text/xml");

        payloadInfo.getPartInfo().add(partInfo);
        PartyInfo partyInfo = new PartyInfo();
        userMessage.setPartyInfo(partyInfo);
        messaging.setUserMessage(userMessage);

        userMessageHandlerService.checkPartInfoCharset(userMessage, null);

    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testCheckCharset_InvalidCharset() {
        final Messaging messaging = new Messaging();
        UserMessage userMessage = new UserMessage();
        PayloadInfo payloadInfo = new PayloadInfo();
        eu.domibus.api.usermessage.domain.PartInfo partInfo = getPartInfo("CharacterSet", "!#InvalidCharSet");

        payloadInfo.getPartInfo().add(partInfo);
        PartyInfo partyInfo = new PartyInfo();
        userMessage.setPartyInfo(partyInfo);
        messaging.setUserMessage(userMessage);

        try {
            userMessageHandlerService.checkPartInfoCharset(userMessage, null);
            fail("EBMS3Exception was expected!!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getErrorCode());
        }

        new FullVerifications() {
        };
    }


    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testInvoke_tc1Process_HappyFlow(@Injectable final BackendFilter matchingBackendFilter,
                                                @Injectable Ebms3MessageFragmentType messageFragment,
                                                @Injectable LegConfiguration legConfiguration,
                                                @Injectable UserMessage userMessage,
                                                @Injectable Messaging messaging) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = false;

            messaging.getUserMessage();
            result = userMessage;

            userMessage.getMessageId();
            result = "messageId";

            routingService.getMatchingBackendFilter(messaging.getUserMessage());
            result = null;

            messageUtil.getMessageFragment(soapRequestMessage);
            result = null;

            userMessageHandlerService.checkSelfSending(pmodeKey);
            result = false;

            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, "backend");
            times = 1;

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
        }};

        userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, false);

        new FullVerifications() {{
            soapUtil.logMessage(soapRequestMessage);
            times = 1;

            backendNotificationService.notifyMessageReceived(null, userMessage, null);
            times = 1;

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testInvoke_tc1Process_SelfSending_HappyFlow(@Injectable final BackendFilter matchingBackendFilter,
                                                            @Injectable Ebms3MessageFragmentType messageFragment,
                                                            @Injectable Reliability reliability,
                                                            @Injectable LegConfiguration legConfiguration,
                                                            @Injectable UserMessage userMessage,
                                                            @Injectable Messaging messaging) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            routingService.getMatchingBackendFilter(messaging.getUserMessage());
            result = matchingBackendFilter;

            matchingBackendFilter.getBackendName();
            result = "backEndName";

            userMessage.getMessageId();
            result = "1234";

            userMessageHandlerService.checkPartInfoCharset(userMessage, null);
            times = 1;

            messageUtil.getMessageFragment(soapRequestMessage);
            result = null;

            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, "backEndName");

        }};

        userMessageHandlerService.handleIncomingMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, true, false, false);

        new FullVerifications() {{
            soapUtil.logMessage(soapRequestMessage);

            String capturedId;
            userMessage.setMessageId(capturedId = withCapture());

            Assert.assertEquals("1234" + UserMessageHandlerService.SELF_SENDING_SUFFIX, capturedId);

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage, null);
            times = 1;

        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testInvoke_tc1Process_SelfSending_HappyFlow_withFragment(@Injectable final BackendFilter matchingBackendFilter,
                                                                         @Injectable Ebms3MessageFragmentType messageFragment,
                                                                         @Injectable Reliability reliability,
                                                                         @Injectable LegConfiguration legConfiguration,
                                                                         @Injectable UserMessage userMessage,
                                                                         @Injectable Messaging messaging,
                                                                         @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            routingService.getMatchingBackendFilter(messaging.getUserMessage());
            result = matchingBackendFilter;

            matchingBackendFilter.getBackendName();
            result = "backEndName";

            userMessage.getMessageId();
            result = "1234";

            userMessageHandlerService.checkPartInfoCharset(userMessage, null);
            times = 1;

            messageUtil.getMessageFragment(soapRequestMessage);
            result = ebms3MessageFragmentType;

            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, ebms3MessageFragmentType, "backEndName");

            ebms3MessageFragmentType.getGroupId();
            result = "groupId";
        }};

        userMessageHandlerService.handleIncomingMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, true, false, false);

        new FullVerifications() {{
            soapUtil.logMessage(soapRequestMessage);

            String capturedId;
            userMessage.setMessageId(capturedId = withCapture());

            Assert.assertEquals("1234" + UserMessageHandlerService.SELF_SENDING_SUFFIX, capturedId);

            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
            times = 1;

            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage, null);
            times = 1;

            splitAndJoinService.incrementReceivedFragments("groupId", "backEndName");
            times = 1;

        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testInvoke_TestMessage(@Injectable final BackendFilter matchingBackendFilter,
                                       @Injectable final LegConfiguration legConfiguration,
                                       @Injectable final Messaging messaging,
                                       @Injectable final UserMessage userMessage,
                                       @Injectable Ebms3MessageFragmentType messageFragment)
            throws SOAPException, EbMS3Exception, TransformerException, IOException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        boolean selfSending = false;

        new Expectations(userMessageHandlerService) {{
            userMessage.getMessageId();
            result = "TestMessage123";

            messaging.getUserMessage();
            result = userMessage;

            userMessageHandlerService.checkPartInfoCharset(userMessage, null);
            times = 1;

            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = true;

            userMessageHandlerService.checkDuplicate(withAny(userMessage));
            result = false;
            times = 1;

            userMessageHandlerService.checkSelfSending(pmodeKey);
            result = selfSending;
            times = 1;

            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, null);
            times = 1;

            legConfiguration.getReliability().getReplyPattern();
            result = ReplyPattern.RESPONSE;

            legConfiguration.getReliability().isNonRepudiation();
            result = true;

            as4ReceiptService.generateReceipt(soapRequestMessage, userMessage, ReplyPattern.RESPONSE, true, false, selfSending);
            result = soapResponseMessage;
        }};

        userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, true);

        new FullVerifications() {{
            soapUtil.logMessage(soapRequestMessage);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void test_HandlePayLoads_HappyFlowUsingEmptyCID(@Injectable final UserMessage userMessage,
                                                           @Injectable final Ebms3Messaging ebms3Messaging,
                                                           @Injectable final Node bodyContent,
                                                           @Injectable final PartInfo partInfo,
                                                           @Injectable PartProperty property1) throws SOAPException, TransformerException, EbMS3Exception {

        HashSet<PartProperty> partProperties1 = new HashSet<>();
        partProperties1.add(property1);
        partInfo.setPartProperties(partProperties1);
        List<Node> bodyContentNodeList = new ArrayList<>();
        bodyContentNodeList.add(bodyContent);
        final Iterator<Node> bodyContentNodeIterator = bodyContentNodeList.iterator();

        new Expectations(userMessageHandlerService) {{
            partInfo.getHref();
            result = "";

            userMessage.getPartyInfo();
            result = partInfo;

            soapRequestMessage.getSOAPBody().hasChildNodes();
            result = true;

            soapRequestMessage.getSOAPBody().getChildElements();
            result = bodyContentNodeIterator;

            userMessageHandlerService.getDataHandler((Node) any);
            result = null;

            soapRequestMessage.getAttachments();
            result = Collections.emptyIterator();
        }};

        userMessageHandlerService.handlePayloads(soapRequestMessage, ebms3Messaging, null);

        new FullVerifications() {{
            partInfo.setInBody(true);
            partInfo.setPayloadDatahandler((DataHandler) any);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void test_HandlePayLoads_EmptyCIDAndBodyContent(@Injectable final Ebms3Messaging userMessage,
                                                           @Injectable final Node bodyContent,
                                                           @Injectable final PartInfo partInfo)
            throws SOAPException, TransformerException, EbMS3Exception {

        new Expectations() {{
            partInfo.getHref();
            result = "";

//            userMessage.getPayloadInfo().getPartInfo();
//            result = partInfo;

            soapRequestMessage.getSOAPBody().hasChildNodes();
            result = false;

            soapRequestMessage.getAttachments();
            result = Collections.emptyIterator();
        }};

        userMessageHandlerService.handlePayloads(soapRequestMessage, userMessage, null);

        new Verifications() {{
            xmlUtil.getTransformerFactory().newTransformer();
            partInfo.setInBody(true);
            partInfo.setPayloadDatahandler((DataHandler) any);
        }};
    }

    /**
     * For the Happy Flow the Unit test with full data is happening with the test - testInvoke_tc1Process().
     * This test is using mock objects.
     */
    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testPersistReceivedMessage_HappyFlow(@Injectable final LegConfiguration legConfiguration,
                                                     @Injectable final Messaging messaging,
                                                     @Injectable final UserMessage userMessage,
                                                     @Injectable final Ebms3Messaging ebms3Messaging,
                                                     @Injectable final Party receiverParty,
                                                     @Injectable final UserMessageLog userMessageLog,
                                                     @Injectable Ebms3MessageFragmentType messageFragment)
            throws EbMS3Exception, TransformerException, SOAPException {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        final String messageId = "TestMessageId123";

        new Expectations(userMessageHandlerService) {{
            messaging.getUserMessage();
            result = userMessage;

            userMessage.getPartyInfo();
            result = new ArrayList<>();

            legConfiguration.getErrorHandling();
            result = null;

            compressionService.handleDecompression(userMessage, null, legConfiguration);
            result = true;

            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            userMessage.getMessageId();
            result = messageId;

            userMessage.getMpc();
            result = null;

            receiverParty.getEndpoint();
            result = "endPoint";
            userMessage.getServiceValue();
            result = "service";
            userMessage.getActionValue();
            result = "action";
            userMessage.isSourceMessage();
            result = true;
            userMessage.isMessageFragment();
            result = false;

        }};

        userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, "");

        new FullVerifications() {{
            userMessageHandlerService.handlePayloads(soapRequestMessage, ebms3Messaging, null);
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            propertyProfileValidator.validate(userMessage, pmodeKey);
            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, anyString);
            userMessageLogService.save(
                    userMessage,
                    MessageStatus.RECEIVED.toString(),
                    "NOT_REQUIRED",
                    MSHRole.RECEIVING.toString(),
                    0,
                    Ebms3Constants.DEFAULT_MPC,
                    "",
                    "endPoint",
                    "service",
                    "action",
                    true,
                    false);
            uiReplicationSignalService.userMessageReceived(messageId);
            nonRepudiationService.saveRequest(soapRequestMessage, userMessage);

        }};
    }

    /**
     * A single message having multiple PartInfo's with no or special cid.
     */
    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void test_HandlePayLoads_NullCIDMultiplePartInfo(
            @Injectable final Ebms3Messaging userMessage,
            @Injectable final Node bodyContent1,
            @Injectable final DataHandler dataHandler)
            throws SOAPException, TransformerException {

        final PayloadInfo payloadInfo = new PayloadInfo();
        payloadInfo.getPartInfo().add(getPartInfo("MimeType", "text/xml", ""));
        payloadInfo.getPartInfo().add(getPartInfo("MimeType", "text/xml", "#1234"));

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

            userMessage.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";
        }};

        try {
            userMessageHandlerService.handlePayloads(soapRequestMessage, userMessage, null);
            fail("Expecting error that - More than one Partinfo referencing the soap body found!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getErrorCode());
        }

        new FullVerifications() {
        };
    }

    private eu.domibus.api.usermessage.domain.PartInfo getPartInfo(String name, String value) {
        return getPartInfo(name, value, null);
    }

    private eu.domibus.api.usermessage.domain.PartInfo getPartInfo(String name, String value, String href) {
        eu.domibus.api.usermessage.domain.PartInfo partInfo = new eu.domibus.api.usermessage.domain.PartInfo();
        PartProperties partProperties = new PartProperties();
        partProperties.getProperty().add(createProperty(name, value));
        partInfo.setPartProperties(partProperties);
        partInfo.setHref(href);
        return partInfo;
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testPersistReceivedMessage_ValidationException(@Injectable final LegConfiguration legConfiguration,
                                                               @Injectable final Messaging messaging,
                                                               @Injectable final UserMessage userMessage,
                                                               @Injectable final Ebms3Messaging ebms3Messaging,
                                                               @Injectable final UserMessageLog userMessageLog,
                                                               @Injectable Ebms3MessageFragmentType messageFragment)
            throws EbMS3Exception, TransformerException, SOAPException {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            messaging.getUserMessage();
            result = userMessage;

            userMessage.getMessageId();
            result = "messageId";

            compressionService.handleDecompression(userMessage, null, legConfiguration);
            result = true;

            propertyProfileValidator.validate(userMessage, pmodeKey);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property missing exception", "Message Id", null);
        }};

        try {
            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, "");
            fail();
        } catch (Exception e) {
            Assert.assertTrue("Expecting Ebms3 exception", e instanceof EbMS3Exception);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, ((EbMS3Exception) e).getErrorCode());
        }

        new FullVerifications() {{
            userMessageHandlerService.handlePayloads(soapRequestMessage, ebms3Messaging, null);
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            propertyProfileValidator.validate(userMessage, pmodeKey);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testPersistReceivedMessage_CompressionError(@Injectable final LegConfiguration legConfiguration,
                                                            @Injectable final Messaging messaging,
                                                            @Injectable final UserMessage userMessage,
                                                            @Injectable final Party receiverParty,
                                                            @Injectable final UserMessageLog userMessageLog,
                                                            @Injectable Ebms3MessageFragmentType messageFragment)
            throws EbMS3Exception, TransformerException, SOAPException {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            messaging.getUserMessage();
            result = userMessage;

            compressionService.handleDecompression(userMessage, null, legConfiguration);
            result = true;

            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, anyString);
            result = new CompressionException("Could not store binary data for message ", null);

            userMessage.getMessageId();
            result = "TestMessageId123";
        }};
        try {
            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, null, "");
            fail("Exception for compression failure expected!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0303, e.getErrorCode());
        }

        new FullVerifications() {{
            userMessageHandlerService.handlePayloads(soapRequestMessage, null, null);
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            propertyProfileValidator.validate(userMessage, pmodeKey);
            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, anyString);
            userMessageLogDao.create(withAny(userMessageLog));
            times = 0;
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void test_HandlePayLoads_HappyFlowUsingCID(@Injectable final UserMessage userMessage,
                                                      @Injectable final AttachmentPart attachmentPart1,
                                                      @Injectable final AttachmentPart attachmentPart2,
                                                      @Injectable final DataHandler attachmentPart1DH,
                                                      @Injectable final DataHandler attachmentPart2DH) throws SOAPException, TransformerException, EbMS3Exception {

//        final PartInfo partInfo = new PartInfo();
//        partInfo.setHref("cid:message");
//
//        PartProperties partProperties = new PartProperties();
//        Property property1 = new Property();
//        property1.setName("MimeType");
//        property1.setValue("text/xml");
//
//        partProperties.getProperties().add(property1);
//        partInfo.setPartProperties(partProperties);

        List<AttachmentPart> attachmentPartList = new ArrayList<>();
        attachmentPartList.add(attachmentPart1);
        attachmentPartList.add(attachmentPart2);
        final Iterator<AttachmentPart> attachmentPartIterator = attachmentPartList.iterator();

        new Expectations() {{


            soapRequestMessage.getAttachments();
            result = attachmentPartIterator;

            attachmentPart1.getContentId();
            result = "AnotherContentID";

            attachmentPart2.getContentId();
            result = "message";

            attachmentPart2.getDataHandler();
            result = attachmentPart2DH;
        }};

        userMessageHandlerService.handlePayloads(soapRequestMessage, null, null);
//        Assert.assertNotNull(partInfo.getPayloadDatahandler());
//        assertNotNull(partInfo.getPayloadDatahandler());

        new FullVerifications() {{
            attachmentPart1.setContentId(anyString);
            attachmentPart2.setContentId(anyString);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void test_HandlePayLoads_NoPayloadFound(
            @Injectable final UserMessage userMessage,
            @Injectable final Ebms3Messaging ebms3Messaging,
            @Injectable final AttachmentPart attachmentPart1,
            @Injectable final AttachmentPart attachmentPart2)
            throws TransformerException, SOAPException {

//        final PartInfo partInfo = new PartInfo();
//        partInfo.setHref("cid:message");
//
//        PartProperties partProperties = new PartProperties();
//        Property property1 = new Property();
//        property1.setName("MimeType");
//        property1.setValue("text/xml");
//
//        partProperties.getProperties().add(property1);
//        partInfo.setPartProperties(partProperties);

        List<AttachmentPart> attachmentPartList = new ArrayList<>();
        attachmentPartList.add(attachmentPart1);
        attachmentPartList.add(attachmentPart2);
        final Iterator<AttachmentPart> attachmentPartIterator = attachmentPartList.iterator();

        new Expectations() {{


            soapRequestMessage.getAttachments();
            result = attachmentPartIterator;

            attachmentPart1.getContentId();
            result = "AnotherContentID";

            attachmentPart2.getContentId();
            result = "message123";

            userMessage.getMessageId();
            result = "messageId";
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
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testGetFinalRecipientName_noProperties() {
        final UserMessage userMessage = createSampleUserMessage(false);
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
    public void testInvoke_DuplicateMessage(@Injectable final BackendFilter matchingBackendFilter,
                                            @Injectable final LegConfiguration legConfiguration,
                                            @Injectable final Messaging messaging,
                                            @Injectable final UserMessage userMessage,
                                            @Injectable Ebms3MessageFragmentType messageFragment,
                                            @Injectable Reliability reliability)
            throws SOAPException, EbMS3Exception, TransformerException, IOException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            userMessage.getMessageId();
            result = "TestMessage123";

            userMessageHandlerService.checkPartInfoCharset(userMessage, null);

            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = true;

            userMessageHandlerService.checkDuplicate(withAny(userMessage));
            result = true;

            userMessageHandlerService.checkSelfSending(pmodeKey);
            result = false;

            legConfiguration.getReliability().isNonRepudiation();
            result = false;

            legConfiguration.getReliability().getReplyPattern();
            result = ReplyPattern.RESPONSE;

            as4ReceiptService.generateReceipt(soapRequestMessage, userMessage, ReplyPattern.RESPONSE, false, true, false);
            result = soapResponseMessage;
        }};

        userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, false);

        new FullVerifications() {{
            soapUtil.logMessage(soapRequestMessage);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testInvoke_ErrorInNotifyingIncomingMessage(@Injectable final BackendFilter matchingBackendFilter,
                                                           @Injectable final LegConfiguration legConfiguration,
                                                           @Injectable final Messaging messaging,
                                                           @Injectable final UserMessage userMessage,
                                                           @Injectable Ebms3MessageFragmentType messageFragment,
                                                           @Injectable Reliability reliability)
            throws SOAPException, EbMS3Exception, TransformerException, IOException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(userMessageHandlerService) {{
            userMessage.getMessageId();
            result = "TestMessage123";

            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = true;

            routingService.getMatchingBackendFilter(messaging.getUserMessage());
            result = matchingBackendFilter;

            matchingBackendFilter.getBackendName();
            result = "matchingBackendFilter";

            messageUtil.getMessageFragment(soapRequestMessage);
            result = messageFragment;

            userMessageHandlerService.checkDuplicate(withAny(userMessage));
            result = false;

            userMessageHandlerService.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, userMessage, null, messageFragment, "matchingBackendFilter");
            result = "123";

            userMessageHandlerService.checkSelfSending(pmodeKey);
            result = false;

            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage, null);
            result = new SubmissionValidationException("Error while submitting the message!!");
        }};
        try {
            userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, soapRequestMessage, userMessage, null, null, false);
            fail();
        } catch (Exception e) {
            Assert.assertTrue("Expecting Ebms3exception!", e instanceof EbMS3Exception);
        }

        new FullVerifications() {{
            soapUtil.logMessage(soapRequestMessage);
            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage, null);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

    @Test
    public void test_checkSelfSending_DifferentAPs_False() throws Exception {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final Party senderParty = getPartyFromConfiguration(configuration, BLUE);
        final Party receiverParty = getPartyFromConfiguration(configuration, RED);

        new Expectations() {{
            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            pModeProvider.getSenderParty(pmodeKey);
            result = senderParty;

        }};

        //tested method
        boolean selfSendingFlag = userMessageHandlerService.checkSelfSending(pmodeKey);
        Assert.assertFalse("expected result should be false", selfSendingFlag);

        new FullVerifications() {
        };
    }

    @Test
    public void test_checkSelfSending_SameAPs_True() throws Exception {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final Party senderParty = getPartyFromConfiguration(configuration, BLUE);
        final Party receiverParty = getPartyFromConfiguration(configuration, BLUE);

        new Expectations() {{
            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            pModeProvider.getSenderParty(pmodeKey);
            result = senderParty;

        }};

        //tested method
        boolean selfSendingFlag = userMessageHandlerService.checkSelfSending(pmodeKey);
        Assert.assertTrue("expected result should be true", selfSendingFlag);

        new FullVerifications() {
        };
    }

    @Test
    public void test_checkSelfSending_DifferentAPsSameEndpoint_True() throws Exception {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final Party senderParty = getPartyFromConfiguration(configuration, BLUE);
        final Party receiverParty = getPartyFromConfiguration(configuration, RED);
        receiverParty.setEndpoint(senderParty.getEndpoint().toLowerCase());

        new Expectations() {{
            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            pModeProvider.getSenderParty(pmodeKey);
            result = senderParty;
        }};

        //tested method
        boolean selfSendingFlag = userMessageHandlerService.checkSelfSending(pmodeKey);
        Assert.assertTrue("expected result should be true", selfSendingFlag);

        new FullVerifications() {
        };
    }

    public Configuration loadSamplePModeConfiguration(String samplePModeFileRelativeURI) throws JAXBException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(samplePModeFileRelativeURI);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Configuration configuration = (Configuration) unmarshaller.unmarshal(xmlStream);
        Method m = configuration.getClass().getDeclaredMethod("preparePersist");
        m.setAccessible(true);
        m.invoke(configuration);

        return configuration;
    }

    public LegConfiguration getLegFromConfiguration(Configuration configuration, String legName) {
        LegConfiguration result = null;
        for (LegConfiguration legConfiguration1 : configuration.getBusinessProcesses().getLegConfigurations()) {
            if (StringUtils.equalsIgnoreCase(legName, legConfiguration1.getName())) {
                result = legConfiguration1;
            }
        }
        return result;
    }

    protected UserMessage createSampleUserMessage() {
        return createSampleUserMessage(true);
    }

    protected UserMessage createSampleUserMessage(boolean properties) {
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

    public Party getPartyFromConfiguration(Configuration configuration, String partyName) {
        Party result = null;
        for (Party party : configuration.getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(partyName, party.getName())) {
                result = party;
            }
        }
        return result;
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testHandleIncomingSourceMessage(@Injectable final LegConfiguration legConfiguration,
                                                @Injectable final String pmodeKey,
                                                @Injectable final SOAPMessage request,
                                                @Injectable final Messaging messaging,
                                                @Injectable final UserMessage userMessage,
                                                @Injectable BackendFilter backendFilter
    ) throws TransformerException, EbMS3Exception, IOException {

        boolean selfSending = false;
        boolean messageExists = false;
        boolean testMessage = false;
        String backendName = "mybackend";

        new Expectations(userMessageHandlerService) {{
            userMessage.getMessageId();
            result = "messageID";

            routingService.getMatchingBackendFilter(messaging.getUserMessage());
            result = backendFilter;

            backendFilter.getBackendName();
            result = backendName;

            userMessageHandlerService.persistReceivedSourceMessage(request, legConfiguration, pmodeKey, new Ebms3MessageFragmentType(), backendName, userMessage, null);
        }};

        userMessageHandlerService.handleIncomingSourceMessage(legConfiguration, pmodeKey, request, userMessage, null, selfSending, messageExists, testMessage);

        new FullVerifications() {{
            backendNotificationService.notifyMessageReceived(backendFilter, userMessage, null);
            soapUtil.logMessage(request);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testPersistReceivedSourceMessage(@Injectable final LegConfiguration legConfiguration,
                                                 @Injectable final String pmodeKey,
                                                 @Injectable final SOAPMessage request,
                                                 @Injectable final Messaging messaging,
                                                 @Injectable final UserMessage userMessage,
                                                 @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType) throws EbMS3Exception {
        String backendName = "mybackend";

        new Expectations(userMessageHandlerService) {{
            messaging.getUserMessage();
            result = userMessage;

            userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, ebms3MessageFragmentType, backendName, userMessage, null);
        }};

        userMessageHandlerService.persistReceivedSourceMessage(request, legConfiguration, pmodeKey, ebms3MessageFragmentType, backendName, userMessage, null);

        new FullVerifications() {{

        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSaveReceivedMessage(@Injectable final LegConfiguration legConfiguration,
                                        @Injectable final String pmodeKey,
                                        @Injectable final SOAPMessage request,
                                        @Injectable final Messaging messaging,
                                        @Injectable final UserMessage userMessage,
                                        @Injectable Party to) throws EbMS3Exception {
        String backendName = "mybackend";
        String messageId = "123";
        String service = "myservice";
        String action = "myaction";
        String endpoint = "http://local";

        new Expectations() {{
            userMessage.getMessageId();
            result = messageId;

            userMessage.getServiceValue();
            result = service;

            userMessage.getActionValue();
            result = action;

            userMessage.isSourceMessage();
            result = true;

            userMessage.isMessageFragment();
            result = false;

            pModeProvider.getReceiverParty(pmodeKey);
            result = to;

            to.getEndpoint();
            result = endpoint;

            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;

            userMessage.getMpc();
            result = Ebms3Constants.DEFAULT_MPC;
        }};

        userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, null);

        new FullVerifications() {{
            payloadProfileValidator.validate(userMessage, null, pmodeKey);
            times = 1;

            propertyProfileValidator.validate(userMessage, pmodeKey);
            times = 1;

            uiReplicationSignalService.userMessageReceived(messageId);

            messagingService.storeMessagePayloads(userMessage, null, MSHRole.RECEIVING, legConfiguration, backendName);

            userMessageLogService.save(
                    userMessage,
                    MessageStatus.RECEIVED.toString(),
                    NotificationStatus.REQUIRED.toString(),
                    MSHRole.RECEIVING.toString(),
                    0,
                    Ebms3Constants.DEFAULT_MPC,
                    backendName,
                    endpoint,
                    service,
                    action,
                    true,
                    false);

            nonRepudiationService.saveRequest(request, userMessage);
        }};

    }

    @Test
    public void testSaveReceivedMessage_exceptionCompressionException(@Injectable final LegConfiguration legConfiguration,
                                                                      @Injectable final String pmodeKey,
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
            userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, null);
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
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSaveReceivedMessage_exceptionInvalidPayloadSizeException_persisted(@Injectable final LegConfiguration legConfiguration,
                                                                                       @Injectable final String pmodeKey,
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
            userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, null);
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
    public void testSaveReceivedMessage_exceptionInvalidPayloadSizeException_NotPersisted(@Injectable final LegConfiguration legConfiguration,
                                                                                          @Injectable final String pmodeKey,
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
            userMessageHandlerService.saveReceivedMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, null);
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
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
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

        new FullVerifications() {{

            userMessage.setMessageFragment(true);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testHandleMessageFragment_createMessageGroup(@Injectable UserMessage userMessage,
                                                             @Injectable Ebms3MessageFragmentType ebms3MessageFragmentType,
                                                             @Injectable MessageGroupEntity messageGroupEntity,
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

            userMessageHandlerService.validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);
            times = 1;

            ebms3MessageFragmentType.getFragmentNum();
            result = 41L;

//            userMessageHandlerService.addPartInfoFromFragment(userMessage, ebms3MessageFragmentType);
//            times = 1;
        }};

        userMessageHandlerService.handleMessageFragment(userMessage, ebms3MessageFragmentType, legConfiguration);

        new FullVerifications() {{
            messageGroupDao.create((MessageGroupEntity) any);
            times = 1;


            userMessage.setMessageFragment(true);
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
                                               @Injectable Messaging messaging) throws IOException, EbMS3Exception, SOAPException, JAXBException, TransformerException {

        String pmodeKey = "pmodeKey";
        boolean testMessage = true;
        boolean selfSendingFlag = true;
        boolean messageExists = true;

        new Expectations(userMessageHandlerService) {{
            userMessage.getMessageId();
            result = "TestMessageId";

            userMessageHandlerService.checkSelfSending(pmodeKey);
            result = selfSendingFlag;

            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = true;

            userMessageHandlerService.checkDuplicate(userMessage);
            result = messageExists;

        }};

        userMessageHandlerService.handleNewSourceUserMessage(legConfiguration, pmodeKey, request, userMessage, null, testMessage);

        new FullVerifications() {{
            userMessageHandlerService.handleIncomingSourceMessage(legConfiguration, pmodeKey, request, userMessage, null, selfSendingFlag, messageExists, testMessage);
            times = 1;
            soapUtil.logMessage(request);
            messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);
        }};
    }

    @Test
    public void checkTestMessageTest(@Injectable LegConfiguration legConfiguration) {

        new Expectations(userMessageHandlerService) {{
            legConfiguration.getService().getValue();
            result = "service";

            legConfiguration.getAction().getValue();
            result = "action";

            userMessageHandlerService.checkTestMessage("service", "action");
            result = true;
        }};

        assertTrue(userMessageHandlerService.checkTestMessage(legConfiguration));

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