package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionContext;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class PasswordDecryptionContextFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordDecryptionContextFactory.class);

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected PasswordDecryptionService passwordDecryptionService;

    public PasswordDecryptionContext getContext(Domain domain) {
        PasswordDecryptionContext result;
        if (domain != null) {
            result = new PasswordDecryptionContextDomain(passwordDecryptionService, domibusPropertyProvider, domibusConfigurationService, domain);
            LOG.trace("Using PasswordEncryptionContextDomain with domain [{}]", domain);
        } else {
            result = new PasswordDecryptionContextDefault(passwordDecryptionService, domibusPropertyProvider, domibusConfigurationService);
            LOG.trace("Using PasswordEncryptionContextDefault");
        }
        return result;
    }
}
