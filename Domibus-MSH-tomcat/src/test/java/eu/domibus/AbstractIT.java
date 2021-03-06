package eu.domibus;

import com.thoughtworks.xstream.XStream;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationType;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.spring.DomibusRootConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.spring.DomibusWebConfiguration;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.SocketUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.jms.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_CONNECTOR_PORT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_TRANSPORT_CONNECTOR_URI;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;
import static org.awaitility.Awaitility.with;

/**
 * Created by feriaad on 02/02/2016.
 */
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = PropertyOverrideContextInitializer.class,
        classes = {DomibusRootConfiguration.class, DomibusWebConfiguration.class,
                DomibusTestDatasourceConfiguration.class, DomibusTestTransactionConfiguration.class, DomibusTestMocksConfiguration.class})
@DirtiesContext
@Rollback
public abstract class AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractIT.class);

    protected static final int SERVICE_PORT = 8892;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected ConfigurationDAO configurationDAO;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusProxyService domibusProxyService;

    @BeforeClass
    public static void init() throws IOException {
        deleteTransactionLock();

        FileUtils.deleteDirectory(new File("target/temp"));
        System.setProperty("domibus.config.location", new File("target/test-classes").getAbsolutePath());

        //we are using randomly available port in order to allow run in parallel
        int activeMQConnectorPort = SocketUtils.findAvailableTcpPort(2000, 2100);
        int activeMQBrokerPort = SocketUtils.findAvailableTcpPort(61616, 61690);
        System.setProperty(ACTIVE_MQ_CONNECTOR_PORT, String.valueOf(activeMQConnectorPort));
        System.setProperty(ACTIVE_MQ_TRANSPORT_CONNECTOR_URI, "vm://localhost:" + activeMQBrokerPort + "?broker.persistent=false");
        LOG.info("activeMQ.connectorPort=[{}]", activeMQConnectorPort);
        LOG.info("activeMQBrokerPort=[{}]", activeMQBrokerPort);

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "test_user",
                        "test_password",
                        Collections.singleton(new SimpleGrantedAuthority(eu.domibus.api.security.AuthRole.ROLE_ADMIN.name()))));
    }

    @Before
    public void setDomain() {
        domainContextProvider.setCurrentDomain(DomainService.DEFAULT_DOMAIN);
    }

    @After
    public void cleanTransactionsLog() {
        deleteTransactionLock();
    }

    public static void deleteTransactionLock() {
        try {
            FileUtils.forceDelete(new File("target/test-classes/work/transactions/log/tmlog.lck"));
        } catch (IOException exc) {
            LOG.info("No tmlog.lck to delete");
        }
    }

    protected void uploadPmode(Integer redHttpPort) throws IOException, XmlProcessingException {
        final InputStream inputStream = new ClassPathResource("dataset/pmode/PModeTemplate.xml").getInputStream();
        String pmodeText = IOUtils.toString(inputStream, "UTF-8");
        if (redHttpPort != null) {
            LOG.info("Using wiremock http port [{}]", redHttpPort);
            pmodeText = pmodeText.replace(String.valueOf(SERVICE_PORT), String.valueOf(redHttpPort));
        }

        final Configuration pModeConfiguration = pModeProvider.getPModeConfiguration(pmodeText.getBytes("UTF-8"));
        configurationDAO.updateConfiguration(pModeConfiguration);
    }

    protected void uploadPmode() throws IOException, XmlProcessingException {
        uploadPmode(null);
    }

    protected UserMessage getUserMessageTemplate() throws IOException {
        XStream xStream = new XStream();
        return (UserMessage) xStream.fromXML(new ClassPathResource("dataset/messages/UserMessageTemplate.xml").getInputStream());
    }


    protected void waitUntilMessageHasStatus(String messageId, MessageStatus messageStatus) {
        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(15, TimeUnit.SECONDS).until(messageHasStatus(messageId, messageStatus));
    }

    protected void waitUntilMessageIsAcknowledged(String messageId) {
        waitUntilMessageHasStatus(messageId, MessageStatus.ACKNOWLEDGED);
    }

    protected void waitUntilMessageIsReceived(String messageId) {
        waitUntilMessageHasStatus(messageId, MessageStatus.RECEIVED);
    }

    protected void waitUntilMessageIsInWaitingForRetry(String messageId) {
        waitUntilMessageHasStatus(messageId, MessageStatus.WAITING_FOR_RETRY);
    }

    protected Callable<Boolean> messageHasStatus(String messageId, MessageStatus messageStatus) {
        return () -> messageStatus == userMessageLogDao.getMessageStatus(messageId);
    }

    /**
     * Convert the given file to a string
     *
     * @param file
     * @return
     */
    protected String getAS4Response(String file) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/as4/" + file);
            Document doc = db.parse(is);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = null;
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (Exception exc) {
            Assert.fail(exc.getMessage());
            exc.printStackTrace();
        }
        return null;
    }


    /**
     * The connection must be started and stopped before and after the method call.
     *
     * @param connection
     * @param queueName
     * @return
     * @throws Exception
     */
    protected void pushQueueMessage(String messageId, javax.jms.Connection connection, String queueName) throws Exception {

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(destination);
        // Creates the Message using Spring MessageCreator
//        NotifyMessageCreator messageCreator = new NotifyMessageCreator(messageId, NotificationType.MESSAGE_RECEIVED);
        Message msg = session.createTextMessage();
        msg.setStringProperty(MessageConstants.DOMAIN, DomainService.DEFAULT_DOMAIN.getCode());
        msg.setStringProperty(MessageConstants.MESSAGE_ID, messageId);
        msg.setObjectProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        msg.setStringProperty(MessageConstants.ENDPOINT, "backendInterfaceEndpoint");
        msg.setStringProperty(MessageConstants.FINAL_RECIPIENT, "testRecipient");
        producer.send(msg);
        System.out.println("Message with ID [" + messageId + "] sent in queue!");
        producer.close();
        session.close();

    }

    /**
     * The connection must be started and stopped before and after the method call.
     *
     * @param connection
     * @param queueName
     * @param mSecs
     * @return Message
     * @throws Exception
     */
    protected Message popQueueMessageWithTimeout(javax.jms.Connection connection, String queueName, long mSecs) throws Exception {

        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination destination = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(destination);
        Message message = consumer.receive(mSecs);
        if (message != null) {
            System.out.println("Message with ID [:" + message.getStringProperty(MESSAGE_ID) + "] consumed from queue [" + message.getJMSDestination() + "]");
        }
        consumer.close();
        session.close();
        return message;
    }

    //TODO move this method into a class in the domibus-MSH-test module in order to be reused
    public SOAPMessage createSOAPMessage(String dataset) throws SOAPException, IOException, ParserConfigurationException, SAXException {

        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document document = builder.parse(getClass().getClassLoader().getResourceAsStream("dataset/as4/" + dataset));
        DOMSource domSource = new DOMSource(document);
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(domSource);

        AttachmentPart attachment = message.createAttachmentPart();
        attachment.setContent(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml");
        attachment.setContentId("cid:message");
        message.addAttachmentPart(attachment);

        String pModeKey = composePModeKey("blue_gw", "red_gw", "testService1", "tc1Action", "", "pushTestcase1tc1Action");

        message.setProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pModeKey);
        message.setProperty(DomainContextProvider.HEADER_DOMIBUS_DOMAIN, DomainService.DEFAULT_DOMAIN.getCode());

        return message;
    }

    protected SoapMessage createSoapMessage(String dataset) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/as4/" + dataset);

        SoapMessage sm = new SoapMessage(new MessageImpl());
        sm.setContent(InputStream.class, is);
        InterceptorChain ic = new PhaseInterceptorChain((new PhaseManagerImpl()).getOutPhases());
        sm.setInterceptorChain(ic);
        ExchangeImpl exchange = new ExchangeImpl();
        Bus bus = new ExtensionManagerBus();
        bus.setExtension(new PolicyBuilderImpl(bus), PolicyBuilder.class);
        exchange.put(Bus.class, bus);
        sm.setExchange(exchange);

        return sm;
    }

    public void prepareSendMessage(String responseFileName) {
        /* Initialize the mock objects */
//        MockitoAnnotations.initMocks(this);

        String body = getAS4Response(responseFileName);

        // Mock the response from the recipient MSH
        stubFor(post(urlEqualTo("/domibus/services/msh"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/soap+xml")
                        .withBody(body)));
    }


    public String composePModeKey(final String senderParty, final String receiverParty, final String service,
                                  final String action, final String agreement, final String legName) {
        return StringUtils.joinWith(MessageExchangeConfiguration.PMODEKEY_SEPARATOR, senderParty,
                receiverParty, service, action, agreement, legName);
    }
}
