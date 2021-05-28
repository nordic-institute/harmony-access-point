package eu.domibus.test;

import com.github.tomakehurst.wiremock.client.WireMock;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.spring.DomibusContextRefreshedListener;
import eu.domibus.core.spring.DomibusRootConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.common.DomibusTestDatasourceConfiguration;
import eu.domibus.web.spring.DomibusWebConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.SocketUtils;
import org.w3c.dom.Document;

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

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = PropertyOverrideContextInitializer.class,
        classes = {DomibusRootConfiguration.class, DomibusWebConfiguration.class,
                DomibusTestDatasourceConfiguration.class, DomibusTestMocksConfiguration.class})
public abstract class AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractIT.class);



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
    DomibusContextRefreshedListener domibusContextRefreshedListener;

    @Autowired
    protected DomibusConditionUtil domibusConditionUtil;

    private static boolean springContextInitialized = false;

    @BeforeClass
    public static void init() throws IOException {
        if(springContextInitialized) {
            return;
        }

        final File domibusConfigLocation = new File("target/test-classes");
        System.setProperty("domibus.config.location", domibusConfigLocation.getAbsolutePath());

        final File projectRoot = new File("").getAbsoluteFile().getParentFile();

        copyActiveMQFile(domibusConfigLocation, projectRoot);
        copyKeystores(domibusConfigLocation, projectRoot);
        copyPolicies(domibusConfigLocation, projectRoot);
        copyDomibusProperties(domibusConfigLocation, projectRoot);

        FileUtils.deleteDirectory(new File("target/temp"));

        //we are using randomly available port in order to allow run in parallel
        int activeMQConnectorPort = SocketUtils.findAvailableTcpPort(2000, 3100);
        int activeMQBrokerPort = SocketUtils.findAvailableTcpPort(61616, 62690);
        System.setProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_CONNECTOR_PORT, String.valueOf(activeMQConnectorPort));
        System.setProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_TRANSPORT_CONNECTOR_URI, "vm://localhost:" + activeMQBrokerPort + "?broker.persistent=false&create=false");
        System.setProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_PERSISTENT, "false");
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
        domibusConditionUtil.waitUntilDatabaseIsInitialized();

        domibusContextRefreshedListener.doInitialize();
    }

    private static void copyPolicies(File domibusConfigLocation, File projectRoot) throws IOException {
        final File policiesDirectory = new File(projectRoot, "Domibus-MSH/src/main/conf/domibus/policies");
        final File destPoliciesDirectory = new File(domibusConfigLocation, "policies");
        FileUtils.forceMkdir(destPoliciesDirectory);
        FileUtils.copyDirectory(policiesDirectory, destPoliciesDirectory);
    }

    private static void copyDomibusProperties(File domibusConfigLocation, File projectRoot) throws IOException {
        final File domibusPropertiesFile = new File(projectRoot, "Domibus-MSH-test/src/main/conf/domibus.properties");
        final File destDomibusPropertiesFile = new File(domibusConfigLocation, "domibus.properties");
        FileUtils.copyFile(domibusPropertiesFile, destDomibusPropertiesFile);
    }

    private static void copyKeystores(File domibusConfigLocation, File projectRoot) throws IOException {
        final File keystoresDirectory = new File(projectRoot, "Domibus-MSH-tomcat/src/test/resources/keystores");
        final File destKeystoresDirectory = new File(domibusConfigLocation, "keystores");
        FileUtils.forceMkdir(destKeystoresDirectory);
        FileUtils.copyDirectory(keystoresDirectory, destKeystoresDirectory);
    }

    private static void copyActiveMQFile(File domibusConfigLocation, File projectRoot) throws IOException {
        final File activeMQFile = new File(projectRoot, "Domibus-MSH-tomcat/src/main/conf/domibus/internal/activemq.xml");
        final File internalDirectory = new File(domibusConfigLocation, "internal");
        FileUtils.forceMkdir(internalDirectory);
        final File destActiveMQ = new File(internalDirectory, "activemq.xml");
        FileUtils.copyFile(activeMQFile, destActiveMQ);
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


    public void prepareSendMessage(String responseFileName) {
        /* Initialize the mock objects */
        String body = getAS4Response(responseFileName);

        // Mock the response from the recipient MSH
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/domibus/services/msh"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/soap+xml")
                        .withBody(body)));
    }
}
