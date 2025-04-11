package eu.domibus.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.PropertyOverrideContextInitializer;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.proxy.DomibusProxyService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.user.User;
import eu.domibus.api.user.UserState;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.ConfigurationRaw;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.ConfigurationRawDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.spring.DomibusApplicationContextListener;
import eu.domibus.core.spring.DomibusRootConfiguration;
import eu.domibus.core.spring.lock.LockDao;
import eu.domibus.core.user.multitenancy.SuperUserManagementServiceImpl;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.core.user.ui.UserRoleDao;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.test.common.DomibusTestDatasourceConfiguration;
import eu.domibus.web.spring.DomibusWebConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.with;
import static org.junit.Assert.fail;

/**
 * Created by feriaad on 02/02/2016.
 */
@EnableMethodSecurity
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = PropertyOverrideContextInitializer.class,
        classes = {DomibusRootConfiguration.class, DomibusWebConfiguration.class,
                DomibusTestDatasourceConfiguration.class, DomibusTestMocksConfiguration.class})
@TestPropertySource(properties = {"domibus.deployment.clustered=false"})
public abstract class AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractIT.class);

    protected static final int SERVICE_PORT = 8892;

    public static final String TEST_ADMIN_USERNAME = "admin";

    public static final String TEST_ADMIN_PASSWORD = "123456";

    public static final String TEST_PLUGIN_USERNAME = "admin";

    public static final String TEST_PLUGIN_PASSWORD = "123456";

    public static final String TEST_ROLE_USER_USERNAME = "user";
    public static final String TEST_ROLE_USER_PASSWORD = "123456";

    public static final String TEST_SUPER_USERNAME = "super";
    public static final String TEST_SUPER_PASSWORD = "123456";
    public static final String DOMIBUS_CONFIG_LOCATION = "target/test-classes";

    public ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected ITTestsService itTestsService;

    @Autowired
    protected AuthUtils authUtils;

    @Autowired
    SuperUserManagementServiceImpl superUserManagementService;

    @Autowired
    UserManagementServiceImpl userManagementService;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    protected DomibusApplicationContextListener domibusApplicationContextListener;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected ConfigurationDAO configurationDAO;

    @Autowired
    LockDao lockDao;

    @Autowired
    protected ConfigurationRawDAO configurationRawDAO;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusProxyService domibusProxyService;

    @Autowired
    protected TruststoreDao truststoreDao;

    @Autowired
    protected UserRoleDao userRoleDao;

    @Autowired
    DomainService domainService;

    @Autowired
    public DomibusPropertyProvider domibusPropertyProvider;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    public static boolean springContextInitialized = false;

    @BeforeClass
    public static void init() throws IOException {
        springContextInitialized = false;
        LOG.info(WarningUtil.warnOutput("Initializing Spring context"));

        FileUtils.deleteDirectory(new File("target/temp"));
        final File domibusConfigLocation = new File(DOMIBUS_CONFIG_LOCATION);
        String absolutePath = domibusConfigLocation.getAbsolutePath();
        System.setProperty("domibus.config.location", absolutePath);

        copyActiveMQFile(domibusConfigLocation);
        copyKeystores(domibusConfigLocation);
        copyPolicies(domibusConfigLocation);
        copyDomibusProperties(domibusConfigLocation);
        copyDomainResourceFolders(domibusConfigLocation);

        //we are using randomly available port in order to allow run in parallel
        String activeMQBrokerName = "localhost" + SocketUtils.findAvailableTcpPort(61616, 62690);
        System.setProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_TRANSPORT_CONNECTOR_URI, "vm://" + activeMQBrokerName + "?broker.persistent=false&create=false");
        System.setProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_BROKER_NAME, activeMQBrokerName);
        LOG.info("activeMQBrokerName=[{}]", activeMQBrokerName);
    }

    @Before
    public void initInstance() {
        setAuth();

        if (!springContextInitialized) {
            //the code below must run for general schema(without a domain)
            domainContextProvider.clearCurrentDomain();

            LOG.info("Executing the ApplicationContextListener initialization");
            try {
                int secondsToSleep = 5;
                LOG.info("Waiting for database initialization: sleeping for [{}] seconds", secondsToSleep);
                //we need to wait a bit until all schemas and tables are completely initialized; the code below which waits for the records to be created in each schema is not enough for the database to be fully initialized
                Thread.sleep(secondsToSleep * 1000L);
                LOG.info("Finished waiting for database initialization");

                domibusApplicationContextListener.initializeForTests();

            } catch (Exception ex) {
                LOG.warn("Domibus Application Context initialization failed", ex);
            } finally {
                springContextInitialized = true;
            }
        }
        //set the default domain as the current domain
        domainContextProvider.setCurrentDomain(DomainService.DEFAULT_DOMAIN);
    }

    protected static void copyActiveMQFile(File domibusConfigLocation) throws IOException {
        DomibusResourcesUtil.copyActiveMQFile(domibusConfigLocation);
    }

    protected static void copyKeystores(File domibusConfigLocation) throws IOException {
        LOG.info("Copying keystores");
        DomibusResourcesUtil.copyResourceToDisk(domibusConfigLocation, "abstractIT/keystores", "keystores");
    }

    protected static void copyPolicies(File domibusConfigLocation) throws IOException {
        LOG.info("Copying policies");
        DomibusResourcesUtil.copyResourceToDisk(domibusConfigLocation, "abstractIT/policies", "policies");
    }

    protected static void copyDomibusProperties(File domibusConfigLocation) throws IOException {
        DomibusResourcesUtil.copyDomibusProperties(domibusConfigLocation);
    }

    protected static void copyDomainResourceFolders(File domibusConfigLocation) throws IOException {
        final String[] folders = {"domains",
                "domains/default", "domains/default/encrypt", "domains/default/keystores",
                "domains/red", "domains/red/encrypt", "domains/red/keystores"};

        for (String folder : folders) {
            DomibusResourcesUtil.copyResourceToDisk(domibusConfigLocation, "abstractIT/" + folder, folder);
        }
    }

    protected void createSuperUser() {
        final List<User> usersWithFilters = superUserManagementService.findUsersWithFilters(AuthRole.ROLE_AP_ADMIN, TEST_SUPER_USERNAME, null, 0, 10);
        if(usersWithFilters.size() > 0) {
            return;
        }

        final ArrayList<User> superUsers = new ArrayList<>();
        final User superUser = new User();
        superUser.setStatus(UserState.NEW.name());
        superUser.setUserName(TEST_SUPER_USERNAME);
        superUser.setPassword(TEST_SUPER_PASSWORD);
        superUser.setActive(true);
        superUser.setAuthorities(Arrays.asList(AuthRole.ROLE_AP_ADMIN.name()));
        superUsers.add(superUser);
        superUserManagementService.updateUsers(superUsers);
    }

    protected void createUser() {
        final List<User> usersWithFilters = userManagementService.findUsersWithFilters(AuthRole.ROLE_USER, TEST_ROLE_USER_USERNAME, null, 0, 10);
        if(usersWithFilters.size() > 0) {
            return;
        }

        final ArrayList<User> superUsers = new ArrayList<>();
        final User superUser = new User();
        superUser.setStatus(UserState.NEW.name());
        superUser.setUserName(TEST_ROLE_USER_USERNAME);
        superUser.setPassword(TEST_ROLE_USER_PASSWORD);
        superUser.setActive(true);
        superUser.setAuthorities(Arrays.asList(AuthRole.ROLE_USER.name()));
        superUsers.add(superUser);
        userManagementService.updateUsers(superUsers);
    }


    protected void setGlobalProperty(String name, String value) {
        final Domain currentDomainSafely = domainContextProvider.getCurrentDomainSafely();
        domainContextProvider.clearCurrentDomain();
        domibusPropertyProvider.setProperty(name, value);

        domainContextProvider.setCurrentDomain(currentDomainSafely);
    }

    protected void setClusteredProperty(boolean value) {
        setGlobalProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DEPLOYMENT_CLUSTERED, value + "");
    }

    protected void setAuth() { // TODO IB
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "test_user",
                        "test_password",
                        Collections.singleton(new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name()))));
    }

    protected void authWithSuper() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "super",
                        "123456",
                        Collections.singleton(new SimpleGrantedAuthority(AuthRole.ROLE_AP_ADMIN.name()))));
    }

    protected void uploadPmode(Integer redHttpPort) throws IOException, XmlProcessingException {
        uploadPmode(redHttpPort, null);
    }

    protected void uploadPmode(Integer redHttpPort, Map<String, String> toReplace) throws IOException, XmlProcessingException {
        uploadPmode(redHttpPort, "dataset/pmode/PModeTemplate.xml", toReplace);
    }

    protected void uploadPmode(Integer redHttpPort, String pModeFilepath, Map<String, String> toReplace) throws IOException, XmlProcessingException {
        final InputStream inputStream = new ClassPathResource(pModeFilepath).getInputStream();

        String pmodeText = IOUtils.toString(inputStream, UTF_8);
        if (toReplace != null) {
            pmodeText = replace(pmodeText, toReplace);
        }
        if (redHttpPort != null) {
            LOG.info("Using wiremock http port [{}]", redHttpPort);
            pmodeText = pmodeText.replace(String.valueOf(SERVICE_PORT), String.valueOf(redHttpPort));
        }

        byte[] bytes = pmodeText.getBytes(UTF_8);
        final Configuration pModeConfiguration = pModeProvider.getPModeConfiguration(bytes);
        configurationDAO.updateConfiguration(pModeConfiguration);
        final ConfigurationRaw configurationRaw = new ConfigurationRaw();
        configurationRaw.setConfigurationDate(Calendar.getInstance().getTime());
        configurationRaw.setXml(bytes);
        configurationRaw.setDescription("upload Pmode for testing on port: " + redHttpPort);
        configurationRawDAO.create(configurationRaw);
        pModeProvider.refresh();
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
        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(10, TimeUnit.SECONDS).until(databaseIsInitialized());
    }

    protected void waitUntilMessageHasStatus(String messageId, MSHRole mshRole, MessageStatus messageStatus) {
        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(5, TimeUnit.SECONDS)
                .until(messageHasStatus(messageId, mshRole, messageStatus));
    }

    protected void waitUntilMessageIsAcknowledged(String messageId) {
        waitUntilMessageHasStatus(messageId, MSHRole.SENDING, MessageStatus.ACKNOWLEDGED);
    }

    protected void waitUntilMessageIsReceived(String messageId) {
        waitUntilMessageHasStatus(messageId, MSHRole.RECEIVING, MessageStatus.RECEIVED);
    }

    protected void waitUntilMessageIsInWaitingForRetry(String messageId) {
        waitUntilMessageHasStatus(messageId, MSHRole.SENDING, MessageStatus.WAITING_FOR_RETRY);
    }

    protected Callable<Boolean> messageHasStatus(String messageId, MSHRole mshRole, MessageStatus messageStatus) {
        return () -> {
            domainContextProvider.setCurrentDomain(DomainService.DEFAULT_DOMAIN);
            return messageStatus == userMessageLogDao.getMessageStatus(messageId, mshRole);
        };
    }

    protected Callable<Boolean> databaseIsInitialized() {
        return () -> {
            try {
                final int size = lockDao.findAll().size();
                final boolean databaseInitialized = size > 0;
                return databaseInitialized;
            } catch (Exception e) {
                LOG.warn("Could not get the lock entries");
                return false;
            }
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
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (Exception exc) {
            fail(exc.getMessage());
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

    protected void createStore(String storeName, String filePath) {
        if (truststoreDao.existsWithName(storeName)) {
            LOG.info("truststore already created");
            return;
        }
        LOG.info("create truststore [{}]", storeName);
        try {
            TruststoreEntity domibusTruststoreEntity = new TruststoreEntity();
            domibusTruststoreEntity.setName(storeName);
            domibusTruststoreEntity.setType("JKS");
            domibusTruststoreEntity.setPassword("test123");
            try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
                if (resourceAsStream == null) {
                    throw new IllegalStateException("File not found :" + filePath);
                }
                byte[] trustStoreBytes = IOUtils.toByteArray(resourceAsStream);
                domibusTruststoreEntity.setContent(trustStoreBytes);
                truststoreDao.create(domibusTruststoreEntity);
            }
        } catch (Exception ex) {
            LOG.info("Error creating store entity [{}]", storeName, ex);
        }
    }

    public static MockMultipartFile getMultiPartFile(String originalFilename, InputStream resourceAsStream) throws IOException {
        return new MockMultipartFile("file", originalFilename, "octetstream", IOUtils.toByteArray(resourceAsStream));
    }

    public String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllMessages(String... messageIds) {
        itTestsService.deleteAllMessages(messageIds);
    }

    protected void deleteAllMessages() {
        final List<UserMessage> userMessages = userMessageDao.findAll();
        if (CollectionUtils.isEmpty(userMessages)) {
            return;
        }
        final List<String> userMessageIds = userMessages.stream().map(userMessage -> userMessage.getMessageId()).collect(Collectors.toList());
        deleteAllMessages(userMessageIds.toArray(new String[0]));
    }

    protected byte[] getKeystoreContentForDomainFromClasspath(String resourceName, Domain domain) throws IOException {
        String fullClasspath = "keystores/" + resourceName;
        return getResourceFromClasspath(fullClasspath);
    }

    protected byte[] getResourceFromClasspath(String resourceClasspathLocation) throws IOException {
        final InputStream contentInputStream = this.getClass().getClassLoader().getResourceAsStream(resourceClasspathLocation);
        if (contentInputStream == null) {
            throw new RuntimeException("Could not get resource from classpath [" + resourceClasspathLocation + "]");
        }
        return IOUtils.toByteArray(contentInputStream);
    }
}
