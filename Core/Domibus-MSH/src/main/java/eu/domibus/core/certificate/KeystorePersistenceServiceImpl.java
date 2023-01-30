package eu.domibus.core.certificate;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.KeyStoreInfo;
import eu.domibus.api.pki.KeystorePersistenceInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import eu.domibus.core.util.backup.BackupService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

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

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final DomibusRawPropertyProvider domibusRawPropertyProvider;

    private final BackupService backupService;

    public KeystorePersistenceServiceImpl(CertificateHelper certificateHelper, TruststoreDao truststoreDao,
                                          PasswordDecryptionService passwordDecryptionService, DomainContextProvider domainContextProvider,
                                          DomibusPropertyProvider domibusPropertyProvider, DomibusRawPropertyProvider domibusRawPropertyProvider, BackupService backupService) {
        this.certificateHelper = certificateHelper;
        this.truststoreDao = truststoreDao;
        this.passwordDecryptionService = passwordDecryptionService;
        this.domainContextProvider = domainContextProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusRawPropertyProvider = domibusRawPropertyProvider;
        this.backupService = backupService;
    }

    @Override
    public KeystorePersistenceInfo getTrustStorePersistenceInfo() {
        KeystorePersistenceInfo persistenceInfo = new TrustStorePersistenceInfoImpl();
//        certificateHelper.validateStoreType(persistenceInfo.getType(), persistenceInfo.getFileLocation());
        return persistenceInfo;
    }

    @Override
    public KeystorePersistenceInfo getKeyStorePersistenceInfo() {
        KeystorePersistenceInfo persistenceInfo = new KeyStorePersistenceInfoImpl();
        // todo move everywhere these are read
//        certificateHelper.validateStoreType(persistenceInfo.getType(), persistenceInfo.getFileLocation());
        return persistenceInfo;
    }

    @Override
    public KeyStoreInfo loadStoreContentFromDisk(KeystorePersistenceInfo keystorePersistenceInfo) {
        KeyStoreInfo result = new KeyStoreInfo();

        String filePath = keystorePersistenceInfo.getFileLocation();
        String storeName = keystorePersistenceInfo.getName();
        if (filePath == null) {
            if (keystorePersistenceInfo.isOptional()) {
                LOG.info("The store location of [{}] is missing (and optional) so exiting.", storeName);
                return result;
            }
            throw new DomibusCertificateException(String.format("Store [%s] is missing and is not optional.", storeName));
        }

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
        String filePath = keystorePersistenceInfo.getFileLocation();
        try {
            if (filePath == null) {
                if (keystorePersistenceInfo.isOptional()) {
                    LOG.info("The store location of [{}] is missing (and optional) so exiting.", storeName);
                    return;
                }
                throw new DomibusCertificateException(String.format("Truststore with type [%s] is missing and is not optional.", storeName));
            }

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
                    "Please check that the store file is present and the location property is set accordingly.", storeName, filePath), ex);
        }
    }

    @Override
    public void saveToDisk(byte[] storeContent, String storeType, KeystorePersistenceInfo persistenceInfo) {
        String storeFileLocation = persistenceInfo.getFileLocation();
        File storeFile = new File(storeFileLocation);

        try {
            backupService.backupFile(storeFile);
        } catch (IOException e) {
            throw new CryptoException("Could not backup store:", e);
        }

        try {
            if (StringUtils.equals(storeType, persistenceInfo.getType())) {
                Files.write(Paths.get(storeFileLocation), storeContent);
            } else {
                String fileExtension = certificateHelper.getStoreFileExtension(storeType);
                String newFileName = FilenameUtils.getBaseName(storeFileLocation) + "." + fileExtension;
                Path newStoreFileLocation = Paths.get(FilenameUtils.getFullPath(storeFileLocation), newFileName);

                Files.write(newStoreFileLocation, storeContent, StandardOpenOption.CREATE);

                persistenceInfo.setType(storeType);
                String newFileLocationString = newStoreFileLocation.toString().replace("\\", "/");
                persistenceInfo.setFileLocation(newFileLocationString);
            }
        } catch (IOException e) {
            throw new CryptoException("Could not persist store:", e);
        }
    }

    @Override
    public void saveToDisk(KeyStore store, KeystorePersistenceInfo persistenceInfo) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            String password = persistenceInfo.getPassword();
            String decryptedPassword = decrypt(persistenceInfo.getName(), password);
            store.store(byteStream, decryptedPassword.toCharArray());
            byte[] content = byteStream.toByteArray();

            saveToDisk(content, persistenceInfo.getType(), persistenceInfo);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new CryptoException("Could not persist store:", e);
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

    class TrustStorePersistenceInfoImpl implements KeystorePersistenceInfo {

        @Override
        public String getName() {
            return DOMIBUS_TRUSTSTORE_NAME;
        }

        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public String getFileLocation() {
            return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        }

        @Override
        public void setFileLocation(String filLocation) {
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION, filLocation);
        }

        @Override
        public String getType() {
            return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
        }

        @Override
        public void setType(String type) {
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_TRUSTSTORE_TYPE, type);
        }

        @Override
        public String getPassword() {
            return domibusRawPropertyProvider.getRawPropertyValue(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
        }
    }

    class KeyStorePersistenceInfoImpl implements KeystorePersistenceInfo {

        @Override
        public String getName() {
            return DOMIBUS_KEYSTORE_NAME;
        }

        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public String getFileLocation() {
            return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_LOCATION);
        }

        @Override
        public void setFileLocation(String filLocation) {
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_KEYSTORE_LOCATION, filLocation);
        }

        @Override
        public String getType() {
            return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_TYPE);
        }

        @Override
        public void setType(String type) {
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_KEYSTORE_TYPE, type);
        }

        @Override
        public String getPassword() {
            return domibusRawPropertyProvider.getRawPropertyValue(DOMIBUS_SECURITY_KEYSTORE_PASSWORD);
        }
    }
}
