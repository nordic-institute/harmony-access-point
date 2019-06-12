package eu.domibus.spring;

import eu.domibus.core.encryption.EncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    protected EncryptionService encryptionService;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Start processing ContextRefreshedEvent");

        encryptionService.createEncryptionKeyForAllDomainsIfNotExists();

        LOG.info("Finished processing ContextRefreshedEvent");

    }
}
