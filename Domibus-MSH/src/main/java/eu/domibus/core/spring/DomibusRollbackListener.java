package eu.domibus.core.spring;

import eu.domibus.api.encryption.EncryptionService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.plugin.BackendConnectorService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import eu.domibus.core.message.dictionary.StaticDictionaryService;
import eu.domibus.core.plugin.routing.BackendFilterInitializerService;
import eu.domibus.core.property.DomibusPropertyValidatorService;
import eu.domibus.core.property.GatewayConfigurationValidator;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.plugin.DownloadEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.TimeUnit;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class DomibusRollbackListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusRollbackListener.class);

    public static final String SYNC_LOCK_KEY = "bootstrap-synchronization.lock";


    protected final EncryptionService encryptionService;


    protected final BackendFilterInitializerService backendFilterInitializerService;


    protected final StaticDictionaryService messageDictionaryService;


    protected final DomibusConfigurationService domibusConfigurationService;


    protected final DomainTaskExecutor domainTaskExecutor;


    final GatewayConfigurationValidator gatewayConfigurationValidator;


    final MultiDomainCryptoService multiDomainCryptoService;


    final TLSCertificateManager tlsCertificateManager;


    final UserManagementServiceImpl userManagementService;


    final DomibusPropertyValidatorService domibusPropertyValidatorService;


    final protected BackendConnectorService backendConnectorService;

    public DomibusRollbackListener(EncryptionService encryptionService, BackendFilterInitializerService backendFilterInitializerService,
                                   StaticDictionaryService messageDictionaryService, DomibusConfigurationService domibusConfigurationService,
                                   DomainTaskExecutor domainTaskExecutor, GatewayConfigurationValidator gatewayConfigurationValidator,
                                   MultiDomainCryptoService multiDomainCryptoService, TLSCertificateManager tlsCertificateManager,
                                   UserManagementServiceImpl userManagementService, DomibusPropertyValidatorService domibusPropertyValidatorService,
                                   BackendConnectorService backendConnectorService) {
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
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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

    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleEvent(DownloadEvent downloadEvent) {
        final String messageId = downloadEvent.getMessageId();
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_ROLLBACK, "USER_MESSAGE", MessageStatus.RECEIVED);
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
        multiDomainCryptoService.persistTruststoresIfApplicable();
        tlsCertificateManager.persistTruststoresIfApplicable();
        domibusPropertyValidatorService.enforceValidation();
        backendFilterInitializerService.updateMessageFilters();
        encryptionService.handleEncryption();
        userManagementService.createDefaultUserIfApplicable();
    }

    /**
     * Method executed in a parallel/not sync mode (in any environment)
     * Add code that does not need to be executed with regard to other nodes in the cluster
     */
    protected void executeNonSynchronized() {
        gatewayConfigurationValidator.validateConfiguration();
        backendConnectorService.ensureValidConfiguration();
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
