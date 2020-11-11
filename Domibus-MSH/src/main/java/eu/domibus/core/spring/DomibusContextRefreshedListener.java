package eu.domibus.core.spring;

import eu.domibus.api.encryption.EncryptionService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.plugin.routing.BackendFilterInitializerService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class DomibusContextRefreshedListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusContextRefreshedListener.class);

    public static final String ENCRYPTION_LOCK = "encryption.lock";

    @Autowired
    protected EncryptionService encryptionService;

    @Autowired
    protected BackendFilterInitializerService backendFilterInitializerService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Start processing ContextRefreshedEvent");

        final ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() == null) {
            LOG.info("Skipping event: we are processing only the web application context event");
            return;
        }

        executeWithLockIfNeeded(this::executeSynchronized);

        executeNonSynchronized();

        LOG.info("Finished processing ContextRefreshedEvent");
    }

    /**
     * Method executed in a serial/sync mode (if in a cluster environment)
     * Add code that needs to be executed with regard to other nodes in the cluster
     */
    protected void executeSynchronized() {
        backendFilterInitializerService.updateMessageFilters();
        encryptionService.handleEncryption();
    }

    /**
     * Method executed in a parallel/not sync mode (in any environment)
     * Add code that does not need to be executed with regard to other nodes in the cluster
     */
    protected void executeNonSynchronized() {
    }

    // TODO: below code to be moved to a separate service EDELIVERY-7462.
    protected void executeWithLockIfNeeded(Runnable task) {
        LOG.debug("Executing in serial mode");
        if (useLockForExecution()) {
            LOG.debug("Handling execution using lock file.");
            final File fileLock = getLockFileLocation();
            domainTaskExecutor.submit(task, null, fileLock);
        } else {
            LOG.debug("Handling execution without lock.");
            task.run();
        }
    }

    protected boolean useLockForExecution() {
        final boolean clusterDeployment = domibusConfigurationService.isClusterDeployment();
        LOG.debug("Cluster deployment? [{}]", clusterDeployment);

        return clusterDeployment;
    }

    protected File getLockFileLocation() {
        return new File(domibusConfigurationService.getConfigLocation(), ENCRYPTION_LOCK);
    }
}
