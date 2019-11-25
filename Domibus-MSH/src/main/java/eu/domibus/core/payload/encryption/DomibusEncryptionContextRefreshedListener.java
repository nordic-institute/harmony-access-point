package eu.domibus.core.payload.encryption;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Component
public class DomibusEncryptionContextRefreshedListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusEncryptionContextRefreshedListener.class);


    @Autowired
    protected PayloadEncryptionService encryptionService;

    @Autowired
    protected PasswordEncryptionService passwordEncryptionService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Start processing ContextRefreshedEvent");

        final ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() == null) {
            LOG.info("Skipping event: we are processing only the web application context event");
            return;
        }

        if (encryptionService.useLockForEncryption()) {
            LOG.debug("Handling encryption using lock file");

            final File fileLock = encryptionService.getLockFileLocation();
            domainTaskExecutor.submit(() -> handleEncryption(), null, fileLock);
        } else {
            LOG.debug("Handling encryption");
            handleEncryption();
        }

        LOG.info("Finished processing ContextRefreshedEvent");
    }

    protected void handleEncryption() {
        try {
            encryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
        } catch (Exception e) {
            LOG.error("Error creating payload encryption key", e);
        }

        try {
            passwordEncryptionService.encryptPasswords();
        } catch (Exception e) {
            LOG.error("Error encrypting passwords", e);
        }
    }

}
