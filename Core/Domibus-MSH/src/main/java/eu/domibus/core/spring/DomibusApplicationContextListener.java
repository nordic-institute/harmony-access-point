package eu.domibus.core.spring;

import eu.domibus.api.crypto.TLSCertificateManager;
import eu.domibus.api.encryption.EncryptionService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.plugin.BackendConnectorService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.ebms3.receiver.MSHWebserviceConfiguration;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.core.message.dictionary.StaticDictionaryService;
import eu.domibus.core.metrics.JmsQueueCountSetScheduler;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.initializer.PluginInitializerProvider;
import eu.domibus.core.plugin.routing.BackendFilterInitializerService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.property.DomibusPropertyValidatorService;
import eu.domibus.core.property.GatewayConfigurationValidator;
import eu.domibus.core.scheduler.DomibusQuartzStarter;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.initialize.PluginInitializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.xml.ws.Endpoint;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class DomibusApplicationContextListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusApplicationContextListener.class);

    public static final String SYNC_LOCK_KEY = "bootstrap-synchronization.lock";


    protected final EncryptionService encryptionService;


    protected final BackendFilterInitializerService backendFilterInitializerService;


    protected final StaticDictionaryService messageDictionaryService;


    protected final DomibusConfigurationService domibusConfigurationService;


    protected final DomainTaskExecutor domainTaskExecutor;


    protected final GatewayConfigurationValidator gatewayConfigurationValidator;


    protected final MultiDomainCryptoService multiDomainCryptoService;


    protected final TLSCertificateManager tlsCertificateManager;


    protected final UserManagementServiceImpl userManagementService;


    protected final DomibusPropertyValidatorService domibusPropertyValidatorService;


    protected final BackendConnectorService backendConnectorService;

    protected final MessageListenerContainerInitializer messageListenerContainerInitializer;

    protected JmsQueueCountSetScheduler jmsQueueCountSetScheduler;

    protected PayloadFileStorageProvider payloadFileStorageProvider;

    protected RoutingService routingService;

    protected DomibusQuartzStarter domibusQuartzStarter;

    protected EArchiveFileStorageProvider eArchiveFileStorageProvider;

    protected PluginInitializerProvider pluginInitializerProvider;

    protected Endpoint mshEndpoint;

    public DomibusApplicationContextListener(EncryptionService encryptionService,
                                             BackendFilterInitializerService backendFilterInitializerService,
                                             StaticDictionaryService messageDictionaryService,
                                             DomibusConfigurationService domibusConfigurationService,
                                             DomainTaskExecutor domainTaskExecutor,
                                             GatewayConfigurationValidator gatewayConfigurationValidator,
                                             MultiDomainCryptoService multiDomainCryptoService,
                                             TLSCertificateManager tlsCertificateManager,
                                             UserManagementServiceImpl userManagementService,
                                             DomibusPropertyValidatorService domibusPropertyValidatorService,
                                             BackendConnectorService backendConnectorService,
                                             MessageListenerContainerInitializer messageListenerContainerInitializer,
                                             JmsQueueCountSetScheduler jmsQueueCountSetScheduler,
                                             PayloadFileStorageProvider payloadFileStorageProvider,
                                             RoutingService routingService,
                                             DomibusQuartzStarter domibusQuartzStarter,
                                             EArchiveFileStorageProvider eArchiveFileStorageProvider,
                                             PluginInitializerProvider pluginInitializerProvider,
                                             @Qualifier(MSHWebserviceConfiguration.MSH_BEAN_NAME) Endpoint mshEndpoint) {
        this.encryptionService = encryptionService;
        this.backendFilterInitializerService = backendFilterInitializerService;
        this.messageDictionaryService = messageDictionaryService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainTaskExecutor = domainTaskExecutor;
        this.gatewayConfigurationValidator = gatewayConfigurationValidator;
        this.multiDomainCryptoService = multiDomainCryptoService;
        this.tlsCertificateManager = tlsCertificateManager;
        this.userManagementService = userManagementService;
        this.domibusPropertyValidatorService = domibusPropertyValidatorService;
        this.backendConnectorService = backendConnectorService;
        this.messageListenerContainerInitializer = messageListenerContainerInitializer;
        this.jmsQueueCountSetScheduler = jmsQueueCountSetScheduler;
        this.payloadFileStorageProvider = payloadFileStorageProvider;
        this.routingService = routingService;
        this.domibusQuartzStarter = domibusQuartzStarter;
        this.eArchiveFileStorageProvider = eArchiveFileStorageProvider;
        this.pluginInitializerProvider = pluginInitializerProvider;
        this.mshEndpoint = mshEndpoint;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Start processing ContextRefreshedEvent");

        final ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() == null) {
            LOG.info("Skipping event: we are processing only the web application context event");
            return;
        }

        doInitialize();

        LOG.info("Finished processing ContextRefreshedEvent");
    }

    public void doInitialize() {
        executeWithLockIfNeeded(this::executeSynchronized);
        executeNonSynchronized();
    }

    /**
     * Method executed in a serial/sync mode (if in a cluster environment)
     * Add code that needs to be executed with regard to other nodes in the cluster
     */
    protected void executeSynchronized() {
        messageDictionaryService.createStaticDictionaryEntries();
        multiDomainCryptoService.saveStoresFromDBToDisk();
        tlsCertificateManager.saveStoresFromDBToDisk();
        domibusPropertyValidatorService.enforceValidation();
        backendFilterInitializerService.updateMessageFilters();
        encryptionService.handleEncryption();
        userManagementService.createDefaultUserIfApplicable();

        initializePluginsWithLockIfNeeded();
    }

    private void initializePluginsWithLockIfNeeded() {
        final List<PluginInitializer> pluginInitializers = pluginInitializerProvider.getPluginInitializersForEnabledPlugins();
        for (PluginInitializer pluginInitializer : pluginInitializers) {
            try {
                pluginInitializer.initializeWithLockIfNeeded();
            } catch (Exception e) {
                LOG.error("Error executing plugin initializer [{}] with lock", pluginInitializer.getName(), e);
            }
        }
    }

    /**
     * Method executed in a parallel/not sync mode (in any environment)
     * Add code that does not need to be executed with regard to other nodes in the cluster
     */
    protected void executeNonSynchronized() {
        messageListenerContainerInitializer.initialize();
        jmsQueueCountSetScheduler.initialize();
        payloadFileStorageProvider.initialize();
        routingService.initialize();

        eArchiveFileStorageProvider.initialize();

        //this is added on purpose in the non-synchronized area; the initialize method has a more complex logic to decide if it executes in synchronized way
        domibusQuartzStarter.initialize();

        gatewayConfigurationValidator.validateConfiguration();
        backendConnectorService.ensureValidConfiguration();

        initializePluginsNonSynchronized();

        LOG.info("Publishing the /msh endpoint");
        mshEndpoint.publish("/msh");
    }

    private void initializePluginsNonSynchronized() {
        final List<PluginInitializer> pluginInitializers = pluginInitializerProvider.getPluginInitializersForEnabledPlugins();
        for (PluginInitializer pluginInitializer : pluginInitializers) {
            try {
                pluginInitializer.initializeNonSynchronized();
            } catch (Exception e) {
                LOG.error("Error executing plugin initializer [{}]", pluginInitializer.getName(), e);
            }
        }
    }

    // TODO: below code to be moved to a separate service EDELIVERY-7462.
    protected void executeWithLockIfNeeded(Runnable task) {
        LOG.debug("Executing in serial mode");
        if (useLockForExecution()) {
            LOG.debug("Handling execution using db lock.");
            Runnable errorHandler = () -> {
                LOG.warn("An error has occurred while initializing Domibus (executing task [{}]). " +
                        "This does not necessarily mean that Domibus did not start correctly. Please check the Domibus logs for more info.", task);
            };
            domainTaskExecutor.submit(task, errorHandler, SYNC_LOCK_KEY, true, 3L, TimeUnit.MINUTES);
            LOG.debug("Finished handling execution using db lock.");
        } else {
            LOG.debug("Handling execution without db lock.");
            task.run();
        }
    }

    protected boolean useLockForExecution() {
        final boolean clusterDeployment = domibusConfigurationService.isClusterDeployment();
        LOG.debug("Cluster deployment? [{}]", clusterDeployment);
        return clusterDeployment;
    }

}
