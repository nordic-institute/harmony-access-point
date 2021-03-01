package eu.domibus.example.ws;

import com.sun.xml.messaging.saaj.soap.XmlDataContentHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import eu.domibus.plugin.ws.generated.StatusFault;
import eu.domibus.plugin.ws.generated.WebServicePluginInterface;
import eu.domibus.plugin.ws.generated.body.*;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import static eu.domibus.example.ws.WebserviceClient.DEFAULT_WEBSERVICE_LOCATION;
import static org.junit.Assert.*;

/**
 * Created by muellers on 7/1/16.
 */
public class WebserviceClientTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WebserviceClientTest.class);

    private static final String TEST_SUBMIT_MESSAGE_SUBMITREQUEST = "src/test/resources/eu/domibus/example/ws/submitMessage_submitRequest.xml";

    private static final String TEST_SUBMIT_MESSAGE_MESSAGING = "src/test/resources/eu/domibus/example/ws/submitMessage_messaging.xml";

    static final String SAMPLE_MSH_MESSAGE = "src/test/resources/eu/domibus/example/ws/sampleMSHMessage.xml";

    private static final String CONFIG_PROPERTIES = "config.properties";

    private WebserviceClient webserviceExample;

    private WebServicePluginInterface webServicePluginInterface;

    static String mshWSLoc;

    public WebserviceClientTest() {
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
        webserviceExample = new WebserviceClient(backendWSLoc, logMessages);

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
        ListPendingMessagesResponse listPendingMessagesResponse = webServicePluginInterface.listPendingMessages(new ListPendingMessagesRequest());

        sleep(2000);

        for (String messageIdCurrentMessage : listPendingMessagesResponse.getMessageID()) {
            RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
            retrieveMessageRequest.setMessageID(messageIdCurrentMessage);

            Holder<RetrieveMessageResponse> responseHolder = new Holder<>();
            Holder<Messaging> messagingHolder = new Holder<>();

            webServicePluginInterface.retrieveMessage(retrieveMessageRequest, responseHolder, messagingHolder);
        }
    }

    @Before
    public void prepare() throws Exception {
        sleep(5000);
        LOG.info("Prepare BackendInterface");
        webServicePluginInterface = webserviceExample.getPort();
    }

    @Test
    public void testSubmitMessage_CorrectRequest_NoErrorsExpected() throws Exception {
        SubmitRequest submitRequest = WebserviceHelper.parseSendRequestXML(TEST_SUBMIT_MESSAGE_SUBMITREQUEST, SubmitRequest.class);
        Messaging messaging = WebserviceHelper.parseMessagingXML(TEST_SUBMIT_MESSAGE_MESSAGING);

        SubmitResponse result = webServicePluginInterface.submitMessage(submitRequest, messaging);
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

        Messaging messaging = WebserviceHelper.parseMessagingXML(TEST_SUBMIT_MESSAGE_MESSAGING);

        SubmitResponse result = webServicePluginInterface.submitMessage(submitRequest, messaging);
        assertNotNull(result);
        assertNotNull(result.getMessageID());
        assertNotEquals(0, result.getMessageID().size());
    }

    @Test
    public void testRetrieveMessage_MessageIdProvided_MessageWithMessageIDExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        WebserviceHelper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        sleep(2000);

        //send an additional message that would be the next message instead of the first one
        WebserviceHelper.prepareMSHTestMessage(UUID.randomUUID().toString(), null);

        //wait until the message should be received
        sleep(2000);

        RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
        //the messageId has been set. In this case, only the messageID corresponding to this messageID must be downloaded
        retrieveMessageRequest.setMessageID(messageId);

        //Since this method has two return values the response objects are passed over as method parameters.
        Holder<RetrieveMessageResponse> responseHolder = new Holder<>();
        Holder<Messaging> messagingHolder = new Holder<>();


        webServicePluginInterface.retrieveMessage(retrieveMessageRequest, responseHolder, messagingHolder);

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

        MessageStatus response = webServicePluginInterface.getStatus(statusRequest);
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
            webServicePluginInterface.retrieveMessage(retrieveMessageRequest, responseHolder, messagingHolder);
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
        WebserviceHelper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        sleep(2000);

        ListPendingMessagesResponse listPendingMessagesResponse = webServicePluginInterface.listPendingMessages(new ListPendingMessagesRequest());
        assertEquals(1, listPendingMessagesResponse.getMessageID().size());
        assertEquals(messageId, listPendingMessagesResponse.getMessageID().get(0));
    }

    @Test
    public void testGetStatus_MessageIdProvided_NoErrorsExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        WebserviceHelper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        sleep(2000);

        StatusRequest messageStatusRequest = new StatusRequest();
        //The messageId determines the message for which the status is requested
        messageStatusRequest.setMessageID(messageId);

        MessageStatus response = webServicePluginInterface.getStatus(messageStatusRequest);

        assertEquals(MessageStatus.RECEIVED, response);
    }

    @Test
    public void testGetStatus_MessageIdEmpty_SOAPFaultExpected() {

        StatusRequest messageStatusRequest = new StatusRequest();
        //The messageId determines the message for which the status is requested
        messageStatusRequest.setMessageID("");
        try {
            webServicePluginInterface.getStatus(messageStatusRequest);
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
        WebserviceHelper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        sleep(2000);

        GetErrorsRequest messageErrorsRequest = new GetErrorsRequest();
        //The messageId determines the message for which the list of errors is requested
        messageErrorsRequest.setMessageID(UUID.randomUUID().toString());

        ErrorResultImplArray response = webServicePluginInterface.getMessageErrors(messageErrorsRequest);

        String errorString = WebserviceHelper.errorResultAsFormattedString(response);

        assertNotNull(errorString);
    }


}