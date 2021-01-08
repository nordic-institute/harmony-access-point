package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordDecryptionContext;
import eu.domibus.core.property.PropertyRetrieveManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Ion perpegel
 * @since 5.0
 */
@Service
public class PasswordDecryptionContextFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordDecryptionContextFactory.class);

    private final DomibusConfigurationService domibusConfigurationService;

    // this is intentionally not domibusPropertyProvider: to cut the cyclic dependency
    private final PropertyRetrieveManager propertyRetrieveManager;

    public PasswordDecryptionContextFactory(DomibusConfigurationService domibusConfigurationService,
                                            PropertyRetrieveManager propertyRetrieveManager) {
        this.domibusConfigurationService = domibusConfigurationService;
        this.propertyRetrieveManager = propertyRetrieveManager;
    }

    public PasswordDecryptionContext getContext(Domain domain) {
        PasswordDecryptionContext result;
        if (domain != null) {
            result = new PasswordDecryptionContextDomain(propertyRetrieveManager, domibusConfigurationService, domain);
            LOG.trace("Using PasswordDecryptionContextDomain with domain [{}]", domain);
        } else {
            result = new PasswordDecryptionContextDefault(propertyRetrieveManager, domibusConfigurationService);
            LOG.trace("Using PasswordDecryptionContextDefault");
        }
        return result;
    }
}
