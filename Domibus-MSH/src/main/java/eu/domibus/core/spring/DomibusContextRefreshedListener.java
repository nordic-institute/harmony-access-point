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
 * @since 4.1
 */
@Component
public class DomibusContextRefreshedListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusContextRefreshedListener.class);

    @Autowired
    protected EncryptionService encryptionService;

    @Autowired
    protected BackendFilterInitializerService backendFilterInitializerService;


    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Start processing ContextRefreshedEvent");

        final ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() == null) {
            LOG.info("Skipping event: we are processing only the web application context event");
            return;
        }

        executeOnSingleNode(() -> {
            backendFilterInitializerService.updateMessageFilters();
            encryptionService.handleEncryption();
        });

        LOG.info("Finished processing ContextRefreshedEvent");
    }


    // move to a dedicated service
    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    public static final String ENCRYPTION_LOCK = "encryption.lock";

    public void executeOnSingleNode(Runnable task) {
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
