package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class PasswordEncryptionContextFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionContextFactory.class);

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected PasswordEncryptionService passwordEncryptionService;

    public PasswordEncryptionContext getPasswordEncryptionContext(Domain domain) {
        PasswordEncryptionContext result = null;
        if (domain != null) {
            result = new PasswordEncryptionContextDomain(passwordEncryptionService, domibusPropertyProvider, domibusConfigurationService, domain);
            LOG.trace("Using PasswordEncryptionContextDomain with domain [{}]", domain);
        } else {
            result = new PasswordEncryptionContextDefault(passwordEncryptionService, domibusPropertyProvider, domibusConfigurationService);
            LOG.trace("Using PasswordEncryptionContextDefault");
        }
        return result;
    }
}
