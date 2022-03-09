package eu.domibus;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.spring.DomibusContextRefreshedListener;
import eu.domibus.core.spring.DomibusRootConfiguration;
import eu.domibus.core.user.ui.UserRoleDao;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.test.common.DomibusTestDatasourceConfiguration;
import eu.domibus.web.spring.DomibusWebConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.SocketUtils;
import org.w3c.dom.Document;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.with;

/**
 * Created by feriaad on 02/02/2016.
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = PropertyOverrideContextInitializer.class,
        classes = {DomibusRootConfiguration.class, DomibusWebConfiguration.class,
                DomibusTestDatasourceConfiguration.class, DomibusTestMocksConfiguration.class})
public abstract class AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractIT.class);

    protected static final int SERVICE_PORT = 8892;

    @Autowired
    protected DomibusContextRefreshedListener domibusContextRefreshedListener;

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

    @Autowired
    protected UserRoleDao userRoleDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    private static boolean springContextInitialized = false;

    @BeforeClass
    public static void init() throws IOException {
        if (springContextInitialized) {
            return;
        }
        LOG.info(WarningUtil.warnOutput("Initializing Spring context"));

        FileUtils.deleteDirectory(new File("target/temp"));
        System.setProperty("domibus.config.location", new File("target/test-classes").getAbsolutePath());

        //we are using randomly available port in order to allow run in parallel
        int activeMQConnectorPort = SocketUtils.findAvailableTcpPort(2000, 3100);
        int activeMQBrokerPort = SocketUtils.findAvailableTcpPort(61616, 62690);
        System.setProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_CONNECTOR_PORT, String.valueOf(activeMQConnectorPort));
        System.setProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_TRANSPORT_CONNECTOR_URI, "vm://localhost:" + activeMQBrokerPort + "?broker.persistent=false&create=false");
//        System.setProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_JMXURL, "service:jmx:rmi:///jndi/rmi://localhost:" + activeMQBrokerPort + "/jmxrmi");
        LOG.info("activeMQBrokerPort=[{}]", activeMQBrokerPort);

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "test_user",
                        "test_password",
                        Collections.singleton(new SimpleGrantedAuthority(eu.domibus.api.security.AuthRole.ROLE_ADMIN.name()))));

        springContextInitialized = true;
    }

    @Before
    public void setDomain() {
        domainContextProvider.setCurrentDomain(DomainService.DEFAULT_DOMAIN);
        waitUntilDatabaseIsInitialized();
    }


    protected void uploadPmode(Integer redHttpPort) throws IOException, XmlProcessingException {
        uploadPmode(redHttpPort, null);
    }

    protected void uploadPmode(Integer redHttpPort, Map<String, String> toReplace) throws IOException, XmlProcessingException {
        final InputStream inputStream = new ClassPathResource("dataset/pmode/PModeTemplate.xml").getInputStream();

        String pmodeText = IOUtils.toString(inputStream, UTF_8);
        if (toReplace != null) {
            pmodeText = replace(pmodeText, toReplace);
        }
        if (redHttpPort != null) {
            LOG.info("Using wiremock http port [{}]", redHttpPort);
            pmodeText = pmodeText.replace(String.valueOf(SERVICE_PORT), String.valueOf(redHttpPort));
        }

        final Configuration pModeConfiguration = pModeProvider.getPModeConfiguration(pmodeText.getBytes(UTF_8));
        if (!configurationDAO.configurationExists()) {
            configurationDAO.updateConfiguration(pModeConfiguration);
        }
    }

    protected void uploadPmode() throws IOException, XmlProcessingException {
        uploadPmode(null);
    }

    protected UserMessage getUserMessageTemplate() throws IOException {
        Resource userMessageTemplate = new ClassPathResource("dataset/messages/UserMessageTemplate.json");
        String jsonStr = new String(IOUtils.toByteArray(userMessageTemplate.getInputStream()), UTF_8);
        return new ObjectMapper().readValue(jsonStr, UserMessage.class);

    }


    protected void waitUntilDatabaseIsInitialized() {
        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(120, TimeUnit.SECONDS).until(databaseIsInitialized());
    }

    protected void waitUntilMessageHasStatus(String messageId, MessageStatus messageStatus) {
        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(5, TimeUnit.SECONDS).until(messageHasStatus(messageId, messageStatus));
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

    protected Callable<Boolean> databaseIsInitialized() {
        return () -> {
            try {
                return userRoleDao.listRoles().size() > 0;
            } catch (Exception e) {
                LOG.error("Could not get the roles list", e);
            }
            return false;
        };
    }

    /**
     * Convert the given file to a string
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

    public void prepareSendMessage(String responseFileName) {
        prepareSendMessage(responseFileName, null);
    }

    public void prepareSendMessage(String responseFileName, Map<String, String> toReplace) {
        String body = getAS4Response(responseFileName);
        if (toReplace != null) {
            body = replace(body, toReplace);
        }

        // Mock the response from the recipient MSH
        stubFor(post(urlEqualTo("/domibus/services/msh"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/soap+xml")
                        .withBody(body)));
    }

    protected String replace(String body, Map<String, String> toReplace) {
        for (String key : toReplace.keySet()) {
            body = body.replaceAll(key, toReplace.get(key));
        }
        return body;
    }

}
