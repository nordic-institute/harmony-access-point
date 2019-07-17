package eu.domibus.spring;

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

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Component
public class DomibusContextRefreshedListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusContextRefreshedListener.class);

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

        domainTaskExecutor.submit(() -> handleEncryption());

        LOG.info("Finished processing ContextRefreshedEvent");

    }

    protected void handleEncryption() {
        encryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
        passwordEncryptionService.encryptPasswords();

        //signal to plugins that's ok to encrypt passwords
        //the operation must be idempotent
    }

}
