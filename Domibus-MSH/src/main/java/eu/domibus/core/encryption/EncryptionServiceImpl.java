package eu.domibus.core.encryption;

import eu.domibus.api.encryption.EncryptionService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EncryptionServiceImpl implements EncryptionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EncryptionServiceImpl.class);

    protected final PayloadEncryptionService payloadEncryptionService;

    protected final PasswordEncryptionService passwordEncryptionService;

    protected final DomibusConfigurationService domibusConfigurationService;

    protected final DomainService domainService;

    public EncryptionServiceImpl(
            PayloadEncryptionService payloadEncryptionService,
            PasswordEncryptionService passwordEncryptionService,
            DomibusConfigurationService domibusConfigurationService,
            DomainService domainService) {
        this.payloadEncryptionService = payloadEncryptionService;
        this.passwordEncryptionService = passwordEncryptionService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainService = domainService;
    }

    @Override
    public void handleEncryption() {
        final boolean anyEncryptionActive = isAnyEncryptionActive();
        if (!anyEncryptionActive) {
            LOG.info("Encryption is not active; exiting");
            return;
        }
        LOG.debug("Handling encryption");
        doHandleEncryption();
    }

    protected void doHandleEncryption() {
        try {
            payloadEncryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
        } catch (Exception e) {
            LOG.error("Error creating payload encryption key", e);
        }

        try {
            passwordEncryptionService.encryptPasswords();
        } catch (Exception e) {
            LOG.error("Error encrypting passwords", e);
        }
    }

    protected boolean isAnyEncryptionActive() {
        final boolean generalPasswordEncryptionActive = domibusConfigurationService.isPasswordEncryptionActive();
        if (generalPasswordEncryptionActive) {
            LOG.debug("General password encryption is active");
            return true;
        }

        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            final Boolean payloadEncryptionActive = domibusConfigurationService.isPayloadEncryptionActive(domain);
            if (BooleanUtils.isTrue(payloadEncryptionActive)) {
                LOG.debug("Payload encryption is active for domain [{}]", domain);
                return true;
            }

            final boolean passwordEncryptionActive = domibusConfigurationService.isPasswordEncryptionActive(domain);
            if (passwordEncryptionActive) {
                LOG.debug("Password encryption is active for domain [{}]", domain);
                return true;
            }
        }

        return false;
    }

}
