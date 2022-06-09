package eu.domibus.plugin.ws.client;

import com.sun.xml.messaging.saaj.soap.XmlDataContentHandler;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ws.generated.WebServicePluginInterface;
import eu.domibus.plugin.ws.generated.body.*;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.Holder;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import static eu.domibus.plugin.ws.client.WebserviceClient.DEFAULT_WEBSERVICE_LOCATION;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author François Gautier
 * @since 5.0
 *
 * Run a domibus locally on the port 9080 (see PORT) and send a big file (see PATH_TO_FILE)
 */
public class SubmitLargeFileLocal {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(SubmitLargeFileLocal.class);

    private static final String TEST_SUBMIT_MESSAGE_MESSAGING = "src/test/resources/eu/domibus/plugin/ws/client/submitMessage_messaging.xml";

    private static final String CONFIG_PROPERTIES = "config.properties";
    public static final String PORT = "9080";
    public static final String PATH_TO_FILE = "C:/DEV/1_2GB.zip";

    private WebserviceClient webserviceExample;

    private WebServicePluginInterface webServicePluginInterface;

    static String mshWSLoc;

    public static void main(String[] args) throws Exception {
        initialize();
        new SubmitLargeFileLocal().testSubmitMessageWithLargeFiles();
    }

    public SubmitLargeFileLocal() {
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

        mshWSLoc = properties.getProperty("msh.webservice.location", "http://localhost:" + PORT + "/domibus/services/msh");

        if (backendWSLoc.toLowerCase().startsWith("https") || mshWSLoc.toLowerCase().startsWith("https")) {
            System.setProperty("javax.net.ssl.trustStore", properties.getProperty("webservice.location.truststore.location"));
            System.setProperty("javax.net.ssl.trustStorePassword", properties.getProperty("webservice.location.truststore.password"));
        }
    }


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


    public void prepare() throws Exception {
        sleep(5000);
        LOG.info("Prepare BackendInterface");
        webServicePluginInterface = webserviceExample.getPort();
    }


    public void testSubmitMessageWithLargeFiles() throws Exception {
        prepare();

        SubmitRequest submitRequest = new SubmitRequest();
        LargePayloadType largepayload = new LargePayloadType();
        largepayload.setPayloadId("cid:payload");
        largepayload.setContentType("application/octet-stream");
        final DataHandler dataHandler = new DataHandler(new FileDataSource(PATH_TO_FILE));
        largepayload.setValue(dataHandler);
        submitRequest.getPayload().add(largepayload);

        Messaging messaging = WebserviceHelper.parseMessagingXML(TEST_SUBMIT_MESSAGE_MESSAGING);

        SubmitResponse result = webServicePluginInterface.submitMessage(submitRequest, messaging);
        assertNotNull(result);
        assertNotNull(result.getMessageID());
        assertNotEquals(0, result.getMessageID().size());

        cleanUp();
    }


    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            LOG.error("Thread.sleep in error", e);
        }
    }

}