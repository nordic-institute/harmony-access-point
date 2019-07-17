package eu.domibus.spring;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.PasswordEncryptionService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Component
public class DomibusContextRefreshedListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusContextRefreshedListener.class);
    public static final String ENCRYPTION_LOCK = "encryption.lock";

    @Autowired
    protected PayloadEncryptionService encryptionService;

    @Autowired
    protected PasswordEncryptionService passwordEncryptionService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainService domainService;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Start processing ContextRefreshedEvent");

        final ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() == null) {
            LOG.info("Skipping event: we are processing only the web application context event");
            return;
        }

        if (useLockForEncryption()) {
            LOG.debug("Handling encryption using lock file");

            final File fileLock = getLockFile();
            domainTaskExecutor.submit(() -> handleEncryption(), null, fileLock);
        } else {
            LOG.debug("Handling encryption");
            handleEncryption();
        }

        LOG.info("Finished processing ContextRefreshedEvent");

    }

    protected boolean useLockForEncryption() {
        final boolean clusterDeployment = domibusConfigurationService.isClusterDeployment();
        LOG.debug("Cluster deployment? [{}]", clusterDeployment);

        final boolean anyEncryptionActive = isAnyEncryptionActive();
        LOG.debug("isAnyEncryptionActive? [{}]", anyEncryptionActive);

        return clusterDeployment && isAnyEncryptionActive();
    }

    protected boolean isAnyEncryptionActive() {
        final boolean generalPasswordEncryptionActive = domibusConfigurationService.isPasswordEncryptionActive();
        if (generalPasswordEncryptionActive) {
            LOG.debug("General password encryption is active");
            return true;
        }

        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            final Boolean payloadEncryptionActive = domibusConfigurationService.isPayloadEncryptionActive(domain);
            if (payloadEncryptionActive) {
                LOG.debug("Payload encryption is active for domain [{}]", domain);
                return true;
            }

            final boolean passwordEncryptionActive = domibusConfigurationService.isPasswordEncryptionActive(domain);
            if (passwordEncryptionActive) {
                LOG.debug("Password encryption is active for domain [{}]", domain);
                return true;
            }
        }

        return false;
    }

    protected void handleEncryption() {
        encryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
        passwordEncryptionService.encryptPasswords();

        //signal to plugins that's ok to encrypt passwords
        //the operation must be idempotent
    }

    protected File getLockFile() {
        return new File(domibusConfigurationService.getConfigLocation(), ENCRYPTION_LOCK);
    }

}
