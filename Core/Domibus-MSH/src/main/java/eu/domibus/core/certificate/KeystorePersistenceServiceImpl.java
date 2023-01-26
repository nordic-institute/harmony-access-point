package eu.domibus.core.certificate;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.KeyStoreInfo;
import eu.domibus.api.pki.KeystorePersistenceInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
@Service
public class KeystorePersistenceServiceImpl implements KeystorePersistenceService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(KeystorePersistenceServiceImpl.class);

    private final CertificateHelper certificateHelper;

    protected final TruststoreDao truststoreDao;

    private final PasswordDecryptionService passwordDecryptionService;

    private final DomainContextProvider domainContextProvider;

    public KeystorePersistenceServiceImpl(CertificateHelper certificateHelper, TruststoreDao truststoreDao,
                                          PasswordDecryptionService passwordDecryptionService, DomainContextProvider domainContextProvider) {
        this.certificateHelper = certificateHelper;
        this.truststoreDao = truststoreDao;
        this.passwordDecryptionService = passwordDecryptionService;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public KeyStoreInfo loadStoreContentFromDisk(KeystorePersistenceInfo keystorePersistenceInfo) {
        KeyStoreInfo result = new KeyStoreInfo();

        Optional<String> filePathHolder = keystorePersistenceInfo.getFilePath();
        String storeName = keystorePersistenceInfo.getName();
        if (!filePathHolder.isPresent()) {
            if (keystorePersistenceInfo.isOptional()) {
                LOG.info("The store location of [{}] is missing (and optional) so exiting.", storeName);
                return result;
            }
            throw new DomibusCertificateException(String.format("Store [%s] is missing and is not optional.", storeName));
        }

        String filePath = filePathHolder.get();
        String storeType = keystorePersistenceInfo.getType();
        certificateHelper.validateStoreType(storeType, filePath);

        byte[] contentOnDisk = getStoreContentFromFile(filePath);
        String password = decrypt(storeName, keystorePersistenceInfo.getPassword());

        result.setContent(contentOnDisk);
        result.setName(storeName);
        result.setType(storeType);
        result.setPassword(password);

        return result;
    }

    @Override
    @Transactional
    public void saveStoreFromDBToDisk(KeystorePersistenceInfo keystorePersistenceInfo) {
        String storeName = keystorePersistenceInfo.getName();
        Optional<String> filePathHolder = keystorePersistenceInfo.getFilePath();
        try {
            if (!filePathHolder.isPresent()) {
                if (keystorePersistenceInfo.isOptional()) {
                    LOG.info("The store location of [{}] is missing (and optional) so exiting.", storeName);
                    return;
                }
                throw new DomibusCertificateException(String.format("Truststore with type [%s] is missing and is not optional.", storeName));
            }

            String filePath = filePathHolder.get();
            certificateHelper.validateStoreType(keystorePersistenceInfo.getType(), filePath);

            File storeFile = new File(filePath);
            TruststoreEntity persisted = truststoreDao.findByNameSafely(storeName);
            if (persisted == null) {
                LOG.info("Store [{}] is not found in the DB.", storeName);
                return;
            }

            byte[] contentOnDisk = getStoreContentFromFile(filePath);
            if (!Arrays.equals(persisted.getContent(), contentOnDisk)) {
                LOG.info("Saving the store [{}] from db to the disc.", storeName);
                Files.write(storeFile.toPath(), persisted.getContent());
            } else {
                LOG.info("The store [{}] on disk has the same content.", storeName);
            }
            truststoreDao.delete(persisted);
        } catch (Exception ex) {
            LOG.error(String.format("The store [%s], whose file location is [%s], could not be persisted! " +
                    "Please check that the store file is present and the location property is set accordingly.", storeName, filePathHolder.get()), ex);
        }
    }

    protected byte[] getStoreContentFromFile(String location) {
        File file = new File(location);
        Path path = Paths.get(file.getAbsolutePath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new DomibusCertificateException("Could not read store from [" + location + "]");
        }
    }

    private String decrypt(String trustName, String password) {
        return passwordDecryptionService.decryptPropertyIfEncrypted(domainContextProvider.getCurrentDomainSafely(),
                trustName + ".password", password);
    }
}
