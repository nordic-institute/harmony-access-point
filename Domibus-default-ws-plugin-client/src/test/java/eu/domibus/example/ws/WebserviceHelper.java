package eu.domibus.example.ws;

import eu.domibus.plugin.ws.generated.body.ErrorResultImpl;
import eu.domibus.plugin.ws.generated.body.ErrorResultImplArray;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author François Gautier
 * @since 5.0
 */
class WebserviceHelper {
    private static final JAXBContext jaxbMessagingContext;
    private static final JAXBContext jaxbWebserviceContext;
    private static final MessageFactory messageFactory = new com.sun.xml.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    static {
        try {
            jaxbMessagingContext = JAXBContext.newInstance("eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704");
            jaxbWebserviceContext = JAXBContext.newInstance("eu.domibus.plugin.ws.generated.body");
        } catch (JAXBException e) {
            throw new RuntimeException("Initialization of Helper class failed.", e);
        }

    }

    public static <E> E parseSendRequestXML(final String uriSendRequestXML, Class<E> requestType) throws Exception {
        return (E) jaxbWebserviceContext.createUnmarshaller().unmarshal(new File(uriSendRequestXML));
    }

    public static Messaging parseMessagingXML(String uriMessagingXML) throws Exception {
        return ((JAXBElement<Messaging>) jaxbMessagingContext.createUnmarshaller().unmarshal(new File(uriMessagingXML))).getValue();
    }

    private static SOAPMessage dispatchMessage(Messaging messaging) throws Exception {
        final QName serviceName = new QName("http://domibus.eu", "msh-dispatch-service");
        final QName portName = new QName("http://domibus.eu", "msh-dispatch");
        final javax.xml.ws.Service service = javax.xml.ws.Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP12HTTP_BINDING, WebserviceClientTest.mshWSLoc);
        final Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);

        SOAPMessage soapMessage = messageFactory.createMessage();
        jaxbMessagingContext.createMarshaller().marshal(new JAXBElement<>(new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", "Messaging"), Messaging.class, messaging), soapMessage.getSOAPHeader());

        AttachmentPart attachment = soapMessage.createAttachmentPart();
        attachment.setContent("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=", "text/xml");
        attachment.setContentId("payload");
        soapMessage.addAttachmentPart(attachment);
        soapMessage.saveChanges();
        return dispatch.invoke(soapMessage);
    }

    private static LocalDateTime getCurrentUTCTime() {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS); // Strip away any fractional seconds.
        // We're doing this because we want the tests to pass regardless of the supported date format configured in Domibus.

        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static void prepareMSHTestMessage(String messageId, String uriMessagingXML) throws Exception {

        //if uriMessagingXML is null, use the SAMPLE_MSH_MESSAGE instead
        if (uriMessagingXML == null) {
            uriMessagingXML = WebserviceClientTest.SAMPLE_MSH_MESSAGE;
        }

        Messaging messaging = WebserviceHelper.parseMessagingXML(uriMessagingXML);
        //set messageId
        messaging.getUserMessage().getMessageInfo().setMessageId(messageId);
        //set timestamp
        messaging.getUserMessage().getMessageInfo().setTimestamp(getCurrentUTCTime());

        SOAPMessage responseFromMSH = WebserviceHelper.dispatchMessage(messaging);

        assertNotNull(responseFromMSH);
        assertNotNull(responseFromMSH.getSOAPBody());
        //response is no SOAPFault
        assertNull(responseFromMSH.getSOAPBody().getFault());
    }

    public static String errorResultAsFormattedString(ErrorResultImplArray errorResultArray) {
        StringBuilder formattedOutput = new StringBuilder();

        for (ErrorResultImpl errorResult : errorResultArray.getItem()) {
            formattedOutput.append(LINE_SEPARATOR);
            formattedOutput.append("==========================================================");
            formattedOutput.append(LINE_SEPARATOR);
            formattedOutput.append("EBMS3 error code: ").append(errorResult.getErrorCode());
            formattedOutput.append(LINE_SEPARATOR);
            formattedOutput.append("Error details: ").append(errorResult.getErrorDetail());
            formattedOutput.append(LINE_SEPARATOR);
            formattedOutput.append("Error is related to message with messageId: ").append(errorResult.getMessageInErrorId());
            formattedOutput.append(LINE_SEPARATOR);
            formattedOutput.append("Role of MSH in context of this message transmission: ").append(errorResult.getMshRole());
            formattedOutput.append(LINE_SEPARATOR);
            formattedOutput.append("Time of notification: ").append(errorResult.getNotified());
            formattedOutput.append(LINE_SEPARATOR);
            formattedOutput.append("Message was sent/received: ").append(errorResult.getTimestamp());
            formattedOutput.append(LINE_SEPARATOR);
            formattedOutput.append("==========================================================");
            formattedOutput.append(LINE_SEPARATOR);
        }

        return formattedOutput.toString();
    }
}
