package eu.domibus.core.payload.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.core.encryption.EncryptionKeyDao;
import eu.domibus.core.encryption.EncryptionKeyEntity;
import eu.domibus.core.encryption.EncryptionUsage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service("EncryptionServiceImpl")
public class PayloadEncryptionServiceImpl implements PayloadEncryptionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadEncryptionServiceImpl.class);

    @Autowired
    protected EncryptionKeyDao encryptionKeyDao;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected EncryptionUtil encryptionUtil;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Override
    public void createPayloadEncryptionKeyForAllDomainsIfNotExists() {
        LOG.debug("Creating encryption key for all domains if not yet exists");

        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            createPayloadEncryptionKeyIfNotExists(domain);
        }

        LOG.debug("Finished creating encryption key for all domains if not yet exists");
    }

    @Override
    public void createPayloadEncryptionKeyIfNotExists(Domain domain) {
        final Boolean encryptionActive = domibusConfigurationService.isPayloadEncryptionActive(domain);
        if (encryptionActive) {
            domainTaskExecutor.submit(() -> createPayloadEncryptionKeyIfNotExists(), domain);
        } else {
            LOG.debug("Payload encryption is not activated for domain [{}]", domain);
        }
    }

    protected void createPayloadEncryptionKeyIfNotExists() {
        LOG.debug("Checking if the encryption key should be created");

        final EncryptionKeyEntity payloadKey = encryptionKeyDao.findByUsage(EncryptionUsage.PAYLOAD);
        if (payloadKey != null) {
            LOG.debug("Payload encryption key already exists");
            return;
        }

        LOG.debug("Creating payload encryption key");

        final SecretKey secretKey = encryptionUtil.generateSecretKey();
        final byte[] iv = encryptionUtil.generateIV();
        final EncryptionKeyEntity encryptionKeyEntity = new EncryptionKeyEntity();
        encryptionKeyEntity.setSecretKey(secretKey.getEncoded());
        encryptionKeyEntity.setInitVector(iv);
        encryptionKeyEntity.setUsage(EncryptionUsage.PAYLOAD);

        encryptionKeyDao.create(encryptionKeyEntity);

        LOG.debug("Finished creating payload encryption key");
    }

    @Override
    public Cipher getEncryptCipherForPayload() {
        LOG.debug("Getting the encrypt cipher for payload");

        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        final EncryptionKeyEntity encryptionKeyEntity = encryptionKeyDao.findByUsageCacheable(currentDomain.getCode(), EncryptionUsage.PAYLOAD);
        final SecretKey secretKey = encryptionUtil.getSecretKey(encryptionKeyEntity.getSecretKey());
        final GCMParameterSpec secretKeySpec = encryptionUtil.getSecretKeySpec(encryptionKeyEntity.getInitVector());
        final Cipher encryptCipher = encryptionUtil.getEncryptCipher(secretKey, secretKeySpec);

        LOG.debug("Finished getting the encrypt cipher for payload");

        return encryptCipher;
    }

    @Override
    public Cipher getDecryptCipherForPayload() {
        LOG.debug("Getting the decrypt cipher for payload");

        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        final EncryptionKeyEntity encryptionKeyEntity = encryptionKeyDao.findByUsageCacheable(currentDomain.getCode(), EncryptionUsage.PAYLOAD);
        final SecretKey secretKey = encryptionUtil.getSecretKey(encryptionKeyEntity.getSecretKey());
        final GCMParameterSpec secretKeySpec = encryptionUtil.getSecretKeySpec(encryptionKeyEntity.getInitVector());
        final Cipher decryptCipher = encryptionUtil.getDecryptCipher(secretKey, secretKeySpec);

        LOG.debug("Finished getting the decrypt cipher for payload");

        return decryptCipher;
    }

}
