package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.PasswordEncryptionContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class PasswordEncryptionContextFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionContextFactory.class);

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    public PasswordEncryptionContext getPasswordEncryptionContext(Domain domain) {
        PasswordEncryptionContext result = null;
        if (domain != null) {
            result = new PasswordEncryptionContextDomain(domibusPropertyProvider, domibusConfigurationService, domain);
            LOG.trace("Using PasswordEncryptionContextDomain");
        } else {
            result = new PasswordEncryptionContextDefault(domibusPropertyProvider, domibusConfigurationService);
            LOG.trace("Using PasswordEncryptionContextDefault");
        }
        return result;
    }
}
