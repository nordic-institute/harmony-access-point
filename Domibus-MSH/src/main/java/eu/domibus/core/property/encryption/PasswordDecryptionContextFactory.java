package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordDecryptionContext;
import eu.domibus.core.property.DomibusPropertyRetrieveManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion perpegel
 * @since 5.0
 */
@Service
public class PasswordDecryptionContextFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordDecryptionContextFactory.class);

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomibusPropertyRetrieveManager domibusPropertyRetrieveManager;

    public PasswordDecryptionContext getContext(Domain domain) {
        PasswordDecryptionContext result;
        if (domain != null) {
            result = new PasswordDecryptionContextDomain(domibusPropertyRetrieveManager, domibusConfigurationService, domain);
            LOG.trace("Using PasswordDecryptionContextDomain with domain [{}]", domain);
        } else {
            result = new PasswordDecryptionContextDefault(domibusPropertyRetrieveManager, domibusConfigurationService);
            LOG.trace("Using PasswordDecryptionContextDefault");
        }
        return result;
    }
}
