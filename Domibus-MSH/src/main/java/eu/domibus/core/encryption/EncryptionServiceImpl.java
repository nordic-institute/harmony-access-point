package eu.domibus.core.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.encryption.EncryptionService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
public class EncryptionServiceImpl implements EncryptionService {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(EncryptionServiceImpl.class);

    public static final String ENCRYPTION_LOCK = "encryption.lock";

    @Autowired
    protected PayloadEncryptionService payloadEncryptionService;

    @Autowired
    protected PasswordEncryptionService passwordEncryptionService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainService domainService;

    @Override
    public void handleEncryption() {
        if (useLockForEncryption()) {
            LOG.debug("Handling encryption using lock file");

            final File fileLock = getLockFileLocation();
            domainTaskExecutor.submit(() -> doHandleEncryption(), null, fileLock);
        } else {
            LOG.debug("Handling encryption");
            doHandleEncryption();
        }
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

    protected boolean useLockForEncryption() {
        final boolean clusterDeployment = domibusConfigurationService.isClusterDeployment();
        LOG.debug("Cluster deployment? [{}]", clusterDeployment);

        final boolean anyEncryptionActive = isAnyEncryptionActive();
        LOG.debug("isAnyEncryptionActive? [{}]", anyEncryptionActive);

        return clusterDeployment && isAnyEncryptionActive();
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
            if (payloadEncryptionActive) {
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

    protected File getLockFileLocation() {
        return new File(domibusConfigurationService.getConfigLocation(), ENCRYPTION_LOCK);
    }
}
