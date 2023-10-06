package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import eu.domibus.core.property.GlobalPropertyMetadataManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class PasswordEncryptionContextFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionContextFactory.class);

    protected final DomibusConfigurationService domibusConfigurationService;

    protected final PasswordEncryptionService passwordEncryptionService;

    protected final DomibusRawPropertyProvider domibusRawPropertyProvider;

    protected final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    public PasswordEncryptionContextFactory(DomibusConfigurationService domibusConfigurationService,
                                            PasswordEncryptionService passwordEncryptionService,
                                            DomibusRawPropertyProvider domibusRawPropertyProvider,
                                            GlobalPropertyMetadataManager globalPropertyMetadataManager) {
        this.domibusConfigurationService = domibusConfigurationService;
        this.passwordEncryptionService = passwordEncryptionService;
        this.domibusRawPropertyProvider = domibusRawPropertyProvider;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
    }

    public PasswordEncryptionContext getPasswordEncryptionContext(Domain domain) {
        PasswordEncryptionContext result;
        if (domain != null) {
            result = new PasswordEncryptionContextDomain(passwordEncryptionService, domibusRawPropertyProvider, domibusConfigurationService, globalPropertyMetadataManager, domain);
            LOG.trace("Using PasswordEncryptionContextDomain with domain [{}]", domain);
        } else {
            result = new PasswordEncryptionContextGlobal(passwordEncryptionService, domibusRawPropertyProvider, domibusConfigurationService, globalPropertyMetadataManager);
            LOG.trace("Using PasswordEncryptionContextDefault");
        }
        return result;
    }
}
