package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class PasswordEncryptionServiceImpl implements PasswordEncryptionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionServiceImpl.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public void createAllPasswordEncryptionKeyIfNotExists() {
        LOG.debug("Creating password encryption key if not yet exists");

        createPasswordEncryptionKeyIfNotExists();

        if (domibusConfigurationService.isMultiTenantAware()) {
            final List<Domain> domains = domainService.getDomains();
            for (Domain domain : domains) {
                createPasswordEncryptionKeyIfNotExists(domain);
            }
        }

        LOG.debug("Finished creating password encryption key if not yet exists");
    }

    protected void createPasswordEncryptionKeyIfNotExists() {
        LOG.debug("Checking if the encryption key should be created");

        final Boolean encryptionActive = domibusConfigurationService.isPasswordEncryptionActive(null);
        if (!encryptionActive) {
            LOG.debug("Password encryption is not activated");
            return;
        }


        LOG.debug("Finished creating the encryption key");
    }


    protected void createPasswordEncryptionKeyIfNotExists(Domain domain) {
        LOG.debug("Checking if the encryption key should be created for domain [{}]", domain);

        final Boolean passwordDomainEncryptionActive = domibusConfigurationService.isPasswordEncryptionActive(domain);
        if (!passwordDomainEncryptionActive) {
            LOG.debug("Password encryption is not activated for domain [{}]", domain);
            return;
        }


        LOG.debug("Finished creating the encryption key for domain [{}]", domain);
    }
}
