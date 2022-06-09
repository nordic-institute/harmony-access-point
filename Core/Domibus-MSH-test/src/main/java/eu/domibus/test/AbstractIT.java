package eu.domibus.test;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.spring.DomibusContextRefreshedListener;
import eu.domibus.core.spring.DomibusRootConfiguration;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.common.DomibusTestDatasourceConfiguration;
import eu.domibus.web.spring.DomibusWebConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;
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

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractIT.class);

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
    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

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
        final File policiesDirectory = new File(projectRoot, "../Core/Domibus-MSH/src/main/conf/domibus/policies");
        final File destPoliciesDirectory = new File(domibusConfigLocation, "policies");
        FileUtils.forceMkdir(destPoliciesDirectory);
        FileUtils.copyDirectory(policiesDirectory, destPoliciesDirectory);
    }

    private static void copyDomibusProperties(File domibusConfigLocation, File projectRoot) throws IOException {
        final File domibusPropertiesFile = new File(projectRoot, "../Core/Domibus-MSH-test/src/main/conf/domibus.properties");
        final File destDomibusPropertiesFile = new File(domibusConfigLocation, "domibus.properties");
        FileUtils.copyFile(domibusPropertiesFile, destDomibusPropertiesFile);
    }

    private static void copyKeystores(File domibusConfigLocation, File projectRoot) throws IOException {
        final File keystoresDirectory = new File(projectRoot, "../Tomcat/Domibus-MSH-tomcat/src/test/resources/keystores");
        final File destKeystoresDirectory = new File(domibusConfigLocation, "keystores");
        FileUtils.forceMkdir(destKeystoresDirectory);
        FileUtils.copyDirectory(keystoresDirectory, destKeystoresDirectory);
    }

    private static void copyActiveMQFile(File domibusConfigLocation, File projectRoot) throws IOException {
        final File activeMQFile = new File(projectRoot, "../Tomcat/Domibus-MSH-tomcat/src/main/conf/domibus/internal/activemq.xml");
        final File internalDirectory = new File(domibusConfigLocation, "internal");
        FileUtils.forceMkdir(internalDirectory);
        final File destActiveMQ = new File(internalDirectory, "activemq.xml");
        FileUtils.copyFile(activeMQFile, destActiveMQ);
    }
}
