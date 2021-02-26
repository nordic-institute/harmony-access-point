package eu.domibus.example.ws;

import com.sun.xml.messaging.saaj.soap.XmlDataContentHandler;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.generated.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.UUID;

import static eu.domibus.example.ws.WebserviceExample.DEFAULT_WEBSERVICE_LOCATION;
import static org.junit.Assert.*;

/**
 * Created by muellers on 7/1/16.
 */
public class WebserviceExampleTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WebserviceExampleTest.class);

    private static final String TEST_SUBMIT_MESSAGE_SUBMITREQUEST = "src/test/resources/eu/domibus/example/ws/submitMessage_submitRequest.xml";

    private static final String TEST_SUBMIT_MESSAGE_MESSAGING = "src/test/resources/eu/domibus/example/ws/submitMessage_messaging.xml";

    private static final String SAMPLE_MSH_MESSAGE = "src/test/resources/eu/domibus/example/ws/sampleMSHMessage.xml";

    private static final String CONFIG_PROPERTIES = "config.properties";

    private WebserviceExample webserviceExample;

    private BackendInterface backendInterface;

    private static String mshWSLoc;

    public WebserviceExampleTest() {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + CONFIG_PROPERTIES + "' not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println("NO PROPERTIES configured due to exception: " + e);
        }

        String backendWSLoc = properties.getProperty("backend.webservice.location", DEFAULT_WEBSERVICE_LOCATION);
        boolean logMessages = Boolean.parseBoolean(properties.getProperty("message.logging.enabled", "false"));
        webserviceExample = new WebserviceExample(backendWSLoc, logMessages);

        mshWSLoc = properties.getProperty("msh.webservice.location", "http://localhost:9080/domibus/services/msh");

        if (backendWSLoc.toLowerCase().startsWith("https") || mshWSLoc.toLowerCase().startsWith("https")) {
            System.setProperty("javax.net.ssl.trustStore", properties.getProperty("webservice.location.truststore.location"));
            System.setProperty("javax.net.ssl.trustStorePassword", properties.getProperty("webservice.location.truststore.password"));
        }
    }

    @BeforeClass
    public static void initialize() {
        DataHandler.setDataContentHandlerFactory(stream -> {
            try {
                return new XmlDataContentHandler();
            } catch (ClassNotFoundException e) {
                System.out.println("Could not initialize DataContentHandler due to exception: " + e);
                return null;
            }
        });
    }

    @After
    public void cleanUp() throws Exception {
        ListPendingMessagesResponse listPendingMessagesResponse = backendInterface.listPendingMessages("");

        sleep(2000);

        for (String messageIdCurrentMessage : listPendingMessagesResponse.getMessageID()) {
            RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
            retrieveMessageRequest.setMessageID(messageIdCurrentMessage);

            Holder<RetrieveMessageResponse> responseHolder = new Holder<>();
            Holder<Messaging> messagingHolder = new Holder<>();

            backendInterface.retrieveMessage(retrieveMessageRequest, responseHolder, messagingHolder);
        }
    }

    @Before
    public void prepare() throws Exception {
        sleep(5000);
        backendInterface = webserviceExample.getPort();
    }

    @Test
    public void testSubmitMessage_CorrectRequest_NoErrorsExpected() throws Exception {
        SubmitRequest submitRequest = Helper.parseSendRequestXML(TEST_SUBMIT_MESSAGE_SUBMITREQUEST, SubmitRequest.class);
        Messaging messaging = Helper.parseMessagingXML(TEST_SUBMIT_MESSAGE_MESSAGING);

        SubmitResponse result = backendInterface.submitMessage(submitRequest, messaging);
        assertNotNull(result);
        assertNotNull(result.getMessageID());
        assertNotEquals(0, result.getMessageID().size());
    }


    //@Test
    public void testSubmitMessageWithLargeFiles() throws Exception {
        SubmitRequest submitRequest = new SubmitRequest();
        LargePayloadType largepayload = new LargePayloadType();
        largepayload.setPayloadId("cid:payload");
        largepayload.setContentType("application/octet-stream");
        final DataHandler dataHandler = new DataHandler(new FileDataSource("C:/DEV/1_2GB.zip"));
        largepayload.setValue(dataHandler);
        submitRequest.getPayload().add(largepayload);

        Messaging messaging = Helper.parseMessagingXML(TEST_SUBMIT_MESSAGE_MESSAGING);

        SubmitResponse result = backendInterface.submitMessage(submitRequest, messaging);
        assertNotNull(result);
        assertNotNull(result.getMessageID());
        assertNotEquals(0, result.getMessageID().size());
    }


    @Test
    public void testRetrieveMessage_MessageIdProvided_MessageWithMessageIDExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        Helper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        sleep(2000);

        //send an additional message that would be the next message instead of the first one
        Helper.prepareMSHTestMessage(UUID.randomUUID().toString(), null);

        //wait until the message should be received
        sleep(2000);

        RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
        //the messageId has been set. In this case, only the messageID corresponding to this messageID must be downloaded
        retrieveMessageRequest.setMessageID(messageId);

        //Since this method has two return values the response objects are passed over as method parameters.
        Holder<RetrieveMessageResponse> responseHolder = new Holder<>();
        Holder<Messaging> messagingHolder = new Holder<>();


        backendInterface.retrieveMessage(retrieveMessageRequest, responseHolder, messagingHolder);

        assertNotNull(responseHolder);
        assertNotNull(messagingHolder);

        Messaging ebMSHeaderResponse = messagingHolder.value;

        //Since the only message that should be available for download is the message we have sent at the beginning
        //of this test, the messageId of the downloaded message must be the same as the messageId of the message initially
        //sent to the MSH
        assertEquals(messageId, ebMSHeaderResponse.getUserMessage().getMessageInfo().getMessageId());

        //test DOWNLOADED status
        StatusRequest statusRequest = new StatusRequest();
        //The messageId determines the message for which the status is requested
        statusRequest.setMessageID(messageId);

        MessageStatus response = backendInterface.getStatus(statusRequest);
        assertEquals(MessageStatus.DOWNLOADED, response);

    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            LOG.error("Thread.sleep in error", e);
        }
    }

    @Test
    public void testRetrieveMessage_MessageIdEmpty_SOAPFaultExpected() {
        RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
        //the messageId has been set. In this case, only the messageID corresponding to this messageID must be downloaded
        retrieveMessageRequest.setMessageID("");

        //Since this method has two return values the response objects are passed over as method parameters.
        Holder<RetrieveMessageResponse> responseHolder = new Holder<>();
        Holder<Messaging> messagingHolder = new Holder<>();
        try {
            backendInterface.retrieveMessage(retrieveMessageRequest, responseHolder, messagingHolder);
            fail("One of the following exceptions was expected: SOAPFaultException for XSD validation enabled or RetrieveMessageFault when the XSD validation is disabled");
        } catch (RetrieveMessageFault retrieveMessageFault) {
            assertEquals("Message ID is empty", retrieveMessageFault.getMessage());
        } catch (SOAPFaultException ssfe) {
            assertTrue(ssfe.getMessage().contains("Unmarshalling Error: cvc-minLength-valid:"));
        }

    }

    @Test
    public void testListPendingMessages_CorrectRequest_NoErrorsExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        Helper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        sleep(2000);

        ListPendingMessagesResponse listPendingMessagesResponse = backendInterface.listPendingMessages("");
        assertEquals(1, listPendingMessagesResponse.getMessageID().size());
        assertEquals(messageId, listPendingMessagesResponse.getMessageID().get(0));
    }

    @Test
    public void testGetStatus_MessageIdProvided_NoErrorsExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        Helper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        sleep(2000);

        StatusRequest messageStatusRequest = new StatusRequest();
        //The messageId determines the message for which the status is requested
        messageStatusRequest.setMessageID(messageId);

        MessageStatus response = backendInterface.getStatus(messageStatusRequest);

        assertEquals(MessageStatus.RECEIVED, response);
    }

    @Test
    public void testGetStatus_MessageIdEmpty_SOAPFaultExpected() {

        StatusRequest messageStatusRequest = new StatusRequest();
        //The messageId determines the message for which the status is requested
        messageStatusRequest.setMessageID("");
        try {
            backendInterface.getStatus(messageStatusRequest);
            fail("One of the following exceptions was expected: SOAPFaultException for XSD validation enabled or StatusFault when the XSD validation is disabled");
        } catch (StatusFault statusFault) {
            assertEquals("Message ID is empty", statusFault.getMessage());
        } catch (SOAPFaultException ssfe) {
            assertTrue(ssfe.getMessage().contains("Unmarshalling Error: cvc-minLength-valid:"));
        }

    }

    @Test
    public void testGetMessageErrors_MessageIdProvided_ErrorForMessageExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        Helper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        sleep(2000);

        GetErrorsRequest messageErrorsRequest = new GetErrorsRequest();
        //The messageId determines the message for which the list of errors is requested
        messageErrorsRequest.setMessageID(UUID.randomUUID().toString());

        ErrorResultImplArray response = backendInterface.getMessageErrors(messageErrorsRequest);

        String errorString = Helper.errorResultAsFormattedString(response);

        assertNotNull(errorString);
    }


    private static class Helper {
        private static final JAXBContext jaxbMessagingContext;
        private static final JAXBContext jaxbWebserviceContext;
        private static final MessageFactory messageFactory = new com.sun.xml.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl();
        private static final String LINE_SEPARATOR = System.getProperty("line.separator");

        static {
            try {
                jaxbMessagingContext = JAXBContext.newInstance("eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704");
                jaxbWebserviceContext = JAXBContext.newInstance("eu.domibus.plugin.webService.generated");
            } catch (JAXBException e) {
                throw new RuntimeException("Initialization of Helper class failed.");
            }

        }

        private static <E> E parseSendRequestXML(final String uriSendRequestXML, Class<E> requestType) throws Exception {
            return (E) jaxbWebserviceContext.createUnmarshaller().unmarshal(new File(uriSendRequestXML));
        }

        private static Messaging parseMessagingXML(String uriMessagingXML) throws Exception {
            return ((JAXBElement<Messaging>) jaxbMessagingContext.createUnmarshaller().unmarshal(new File(uriMessagingXML))).getValue();
        }

        private static SOAPMessage dispatchMessage(Messaging messaging) throws Exception {
            final QName serviceName = new QName("http://domibus.eu", "msh-dispatch-service");
            final QName portName = new QName("http://domibus.eu", "msh-dispatch");
            final javax.xml.ws.Service service = javax.xml.ws.Service.create(serviceName);
            service.addPort(portName, SOAPBinding.SOAP12HTTP_BINDING, mshWSLoc);
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

        private static void prepareMSHTestMessage(String messageId, String uriMessagingXML) throws Exception {

            //if uriMessagingXML is null, use the SAMPLE_MSH_MESSAGE instead
            if (uriMessagingXML == null) {
                uriMessagingXML = SAMPLE_MSH_MESSAGE;
            }

            Messaging messaging = Helper.parseMessagingXML(uriMessagingXML);
            //set messageId
            messaging.getUserMessage().getMessageInfo().setMessageId(messageId);
            //set timestamp
            messaging.getUserMessage().getMessageInfo().setTimestamp(getCurrentUTCTime());

            SOAPMessage responseFromMSH = Helper.dispatchMessage(messaging);

            assertNotNull(responseFromMSH);
            assertNotNull(responseFromMSH.getSOAPBody());
            //response is no SOAPFault
            assertNull(responseFromMSH.getSOAPBody().getFault());
        }

        private static String errorResultAsFormattedString(ErrorResultImplArray errorResultArray) {
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


}