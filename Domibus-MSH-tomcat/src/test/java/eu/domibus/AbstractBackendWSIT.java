package eu.domibus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.LargePayloadType;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import eu.domibus.plugin.ws.generated.WebServicePluginInterface;
import eu.domibus.plugin.ws.message.WSMessageLogDao;
import org.apache.commons.codec.binary.Base64;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Assert;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.with;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by draguio on 18/02/2016.
 */
public abstract class AbstractBackendWSIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractBackendWSIT.class);

    public static final String STRING_TYPE = "string";


    protected static final String WS_NOT_QUEUE = "domibus.notification.webservice";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    // TODO: François Gautier 03-02-21 @deprecated to be removed when deprecated endpoint /backend is removed
    @Autowired
    protected BackendInterface backendWebService;

    @Autowired
    protected WebServicePluginInterface webServicePluginInterface;

    @Autowired
    WSMessageLogDao wsMessageLogDao;

    protected void verifySendMessageAck(eu.domibus.plugin.ws.generated.body.SubmitResponse response) {
        final List<String> messageID = response.getMessageID();
        assertNotNull(response);
        assertNotNull(messageID);
        assertEquals(1, messageID.size());
        final String messageId = messageID.iterator().next();

        waitUntilMessageIsAcknowledged(messageId);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(matching(".*"))
                .withHeader("Content-Type", notMatching("application/soap+xml")));

        final MessageStatus messageStatus = userMessageLogDao.getMessageStatus(messageId);
        Assert.assertEquals(MessageStatus.ACKNOWLEDGED, messageStatus);

    }
    /**
     * @deprecated to be removed when deprecated endpoint /backend is removed
     */
    @Deprecated
    protected void verifySendMessageAck(SubmitResponse response) {
        final List<String> messageID = response.getMessageID();
        assertNotNull(response);
        assertNotNull(messageID);
        assertEquals(1, messageID.size());
        final String messageId = messageID.iterator().next();

        waitUntilMessageIsAcknowledged(messageId);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(matching(".*"))
                .withHeader("Content-Type", notMatching("application/soap+xml")));

        final MessageStatus messageStatus = userMessageLogDao.getMessageStatus(messageId);
        Assert.assertEquals(MessageStatus.ACKNOWLEDGED, messageStatus);

    }

    protected eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging createMessageHeaderWs(String payloadHref) {
        return createMessageHeaderWs(payloadHref, "text/xml");
    }
    protected eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging createMessageHeaderWs(String payloadHref, String mimeType) {
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging ebMSHeaderInfo = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging();
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage userMessage = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage();
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageInfo messageInfo = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageInfo();
        messageInfo.setMessageId("IT31-363a-4328-9f81-8d84bf2da59f@domibus.eu");
        userMessage.setMessageInfo(messageInfo);
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.CollaborationInfo collaborationInfo = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.CollaborationInfo();
        collaborationInfo.setAction("TC1Leg1");
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Service service = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Service();
        service.setValue("bdx:noprocess");
        service.setType("tc1");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageProperties messageProperties = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageProperties();
        messageProperties.getProperty().add(createPropertyWs("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
        messageProperties.getProperty().add(createPropertyWs("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4", STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyInfo partyInfo = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyInfo();
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.From from = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.From();
        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyId sender = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyId();
        sender.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        sender.setValue("domibus-blue");
        from.setPartyId(sender);
        partyInfo.setFrom(from);
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.To to = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.To();
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyId receiver = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyId();
        receiver.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        receiver.setValue("domibus-red");
        to.setPartyId(receiver);
        partyInfo.setTo(to);
        userMessage.setPartyInfo(partyInfo);
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PayloadInfo payloadInfo = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PayloadInfo();
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo partInfo = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo();
        partInfo.setHref(payloadHref);
        if (mimeType != null) {
            eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartProperties partProperties = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartProperties();
            partProperties.getProperty().add(createPropertyWs(mimeType, "MimeType", STRING_TYPE));
            partInfo.setPartProperties(partProperties);
        }

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        ebMSHeaderInfo.setUserMessage(userMessage);
        return ebMSHeaderInfo;
    }

    protected eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property createPropertyWs(String name, String value, String type) {
        eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property aProperty = new eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }
    /**
     * @deprecated to be removed when deprecated endpoint /backend is removed
     */
    @Deprecated
    protected Messaging createMessageHeader(String payloadHref) {
        return createMessageHeader(payloadHref, "text/xml");
    }
    /**
     * @deprecated to be removed when deprecated endpoint /backend is removed
     */
    @Deprecated
    protected Messaging createMessageHeader(String payloadHref, String mimeType) {
        Messaging ebMSHeaderInfo = new Messaging();
        UserMessage userMessage = new UserMessage();
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId("IT31-363a-4328-9f81-8d84bf2da59f@domibus.eu");
        userMessage.setMessageInfo(messageInfo);
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("TC1Leg1");
        Service service = new Service();
        service.setValue("bdx:noprocess");
        service.setType("tc1");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getProperty().add(createProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
        messageProperties.getProperty().add(createProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4", STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);
        PartyInfo partyInfo = new PartyInfo();
        From from = new From();
        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        PartyId sender = new PartyId();
        sender.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        sender.setValue("domibus-blue");
        from.setPartyId(sender);
        partyInfo.setFrom(from);
        To to = new To();
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        PartyId receiver = new PartyId();
        receiver.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        receiver.setValue("domibus-red");
        to.setPartyId(receiver);
        partyInfo.setTo(to);
        userMessage.setPartyInfo(partyInfo);
        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();
        partInfo.setHref(payloadHref);
        if (mimeType != null) {
            PartProperties partProperties = new PartProperties();
            partProperties.getProperty().add(createProperty(mimeType, "MimeType", STRING_TYPE));
            partInfo.setPartProperties(partProperties);
        }

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        ebMSHeaderInfo.setUserMessage(userMessage);
        return ebMSHeaderInfo;
    }
    /**
     * @deprecated to be removed when deprecated endpoint /backend is removed
     */
    @Deprecated
    protected Property createProperty(String name, String value, String type) {
        Property aProperty = new Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }

    /**
     * @deprecated to be removed when deprecated endpoint /backend is removed
     */
    @Deprecated
    protected SubmitRequest createSubmitRequest(String payloadHref) {
        final SubmitRequest submitRequest = new SubmitRequest();
        LargePayloadType largePayload = new LargePayloadType();
        final DataHandler messageHandler = new DataHandler(new ByteArrayDataSource(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml"));
        largePayload.setPayloadId(payloadHref);
        largePayload.setContentType("text/xml");
        largePayload.setValue(messageHandler);
        submitRequest.getPayload().add(largePayload);
        return submitRequest;
    }
    protected eu.domibus.plugin.ws.generated.body.SubmitRequest createSubmitRequestWs(String payloadHref) {
        final eu.domibus.plugin.ws.generated.body.SubmitRequest submitRequest = new eu.domibus.plugin.ws.generated.body.SubmitRequest();
        eu.domibus.plugin.ws.generated.body.LargePayloadType largePayload = new eu.domibus.plugin.ws.generated.body.LargePayloadType();
        final DataHandler messageHandler = new DataHandler(new ByteArrayDataSource(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml"));
        largePayload.setPayloadId(payloadHref);
        largePayload.setContentType("text/xml");
        largePayload.setValue(messageHandler);
        submitRequest.getPayload().add(largePayload);
        return submitRequest;
    }

    protected void waitForMessages(int count) {
        with().pollInterval(200, TimeUnit.MILLISECONDS).await().atMost(2, TimeUnit.SECONDS).until(findAllHasCount(count));
    }

    protected Callable<Boolean> findAllHasCount(int count) {
        return () -> wsMessageLogDao.findAll().size() == count;
    }

    protected void waitForMessage(String messageId) {
        try {
            with().pollInterval(200, TimeUnit.MILLISECONDS).await().atMost(2, TimeUnit.SECONDS).until(findByMessageIdReturnMessage(messageId));
        } catch (ConditionTimeoutException e) { //workaround as awaitility has no (yet) implemented a callable for onTimeout()
            LOG.debug("ConditionTimeoutException: ", e);
        }
    }

    protected Callable<Boolean> findByMessageIdReturnMessage(String messageId) {
        return () -> wsMessageLogDao.findByMessageId(messageId) == null;
    }
}
