package eu.domibus.core.certificate;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.api.util.FileServiceUtil;
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
public class KeyStorePersistenceServiceImpl implements KeystorePersistenceService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(KeyStorePersistenceServiceImpl.class);

    private final CertificateHelper certificateHelper;

    protected final TruststoreDao truststoreDao;

    private final PasswordDecryptionService passwordDecryptionService;

    private final DomainContextProvider domainContextProvider;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final DomibusRawPropertyProvider domibusRawPropertyProvider;

    private final BackupService backupService;

    private final FileServiceUtil fileServiceUtil;

    public KeyStorePersistenceServiceImpl(CertificateHelper certificateHelper,
                                          TruststoreDao truststoreDao,
                                          PasswordDecryptionService passwordDecryptionService,
                                          DomainContextProvider domainContextProvider,
                                          DomibusPropertyProvider domibusPropertyProvider,
                                          DomibusRawPropertyProvider domibusRawPropertyProvider,
                                          BackupService backupService, FileServiceUtil fileServiceUtil) {
        this.certificateHelper = certificateHelper;
        this.truststoreDao = truststoreDao;
        this.passwordDecryptionService = passwordDecryptionService;
        this.domainContextProvider = domainContextProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusRawPropertyProvider = domibusRawPropertyProvider;
        this.backupService = backupService;
        this.fileServiceUtil = fileServiceUtil;
    }

    @Override
    public KeystorePersistenceInfo getTrustStorePersistenceInfo() {
        return new TrustStorePersistenceInfoImpl();
    }

    @Override
    public KeystorePersistenceInfo getKeyStorePersistenceInfo() {
        return new KeyStorePersistenceInfoImpl();
    }

    @Override
    public KeyStoreContentInfo loadStore(KeystorePersistenceInfo persistenceInfo) {
        String storePath = persistenceInfo.getFileLocation();
        String storeType = persistenceInfo.getType();

        String storeName = persistenceInfo.getName();
        if (storePath == null) {
            if (persistenceInfo.isOptional()) {
                LOG.debug("The store location of [{}] is missing (and optional) so exiting.", storeName);
                return null;
            }
            throw new DomibusCertificateException(String.format("Store [%s] is missing and is not optional.", storeName));
        }
        certificateHelper.validateStoreType(storeType, storePath);

        byte[] contentOnDisk = getStoreContentFromFile(storePath);
        String password = decrypt(storeName, persistenceInfo.getPassword());

        String fileName = FilenameUtils.getName(storePath);

        return certificateHelper.createStoreContentInfo(storeName, fileName, contentOnDisk, password, storeType);
    }

    @Override
    public void saveStore(KeyStoreContentInfo contentInfo, KeystorePersistenceInfo persistenceInfo) {
        saveStore(contentInfo.getContent(), contentInfo.getType(), persistenceInfo);
    }

    @Override
    public void saveStore(KeyStore store, KeystorePersistenceInfo persistenceInfo) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            String password = persistenceInfo.getPassword();
            String decryptedPassword = decrypt(persistenceInfo.getName(), password);
            store.store(byteStream, decryptedPassword.toCharArray());
            byte[] content = byteStream.toByteArray();

            saveStore(content, persistenceInfo.getType(), persistenceInfo);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new CryptoException("Could not persist store:", e);
        }
    }

    @Override
    @Transactional
    public void saveStoreFromDBToDisk(KeystorePersistenceInfo keystorePersistenceInfo) {
        String storeName = keystorePersistenceInfo.getName();
        String filePath = keystorePersistenceInfo.getFileLocation();
        try {
            if (filePath == null) {
                if (keystorePersistenceInfo.isOptional()) {
                    LOG.debug("The store location of [{}] is missing (and optional) so exiting.", storeName);
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

            if (persisted.getModificationTime().getTime() < storeFile.lastModified()) {
                LOG.info("The store [{}] on disk [{}] is newer [{}] than the persisted one [{}], so no overwriting.",
                        storeName, filePath, storeFile.lastModified(), persisted.getModificationTime().getTime());
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

    protected void saveStore(byte[] storeContent, String storeType, KeystorePersistenceInfo persistenceInfo) {
        String storeFileLocation = persistenceInfo.getFileLocation();
        File storeFile = new File(storeFileLocation);
        try {
            backupService.backupFile(storeFile, "backups");
        } catch (IOException e) {
            throw new CryptoException("Could not backup store:", e);
        }

        try {
            if (StringUtils.equals(storeType, persistenceInfo.getType())) {
                // same store type: just persist it
                Files.write(Paths.get(storeFileLocation), storeContent);
            } else {
                // different store type: store name changes, so type and location properties must be also changed
                String fileExtension = certificateHelper.getStoreFileExtension(storeType);
                String newFileName = FilenameUtils.getBaseName(storeFileLocation) + "." + fileExtension;
                Path newStoreFileLocation = Paths.get(FilenameUtils.getFullPath(storeFileLocation), newFileName);

                Files.write(newStoreFileLocation, storeContent, StandardOpenOption.CREATE);

                String newFileLocationString = newStoreFileLocation.toString().replace("\\", "/");
                persistenceInfo.updateTypeAndFileLocation(storeType, newFileLocationString);
            }
        } catch (IOException e) {
            throw new CryptoException("Could not persist store:", e);
        }
    }

    protected byte[] getStoreContentFromFile(String location) {
        try {
            return fileServiceUtil.getContentFromFile(location);
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
        public String getType() {
            return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
        }

        @Override
        public String getPassword() {
            return domibusRawPropertyProvider.getRawPropertyValue(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
        }

        @Override
        public void updateTypeAndFileLocation(String type, String fileLocation) {
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_TRUSTSTORE_TYPE, type);
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION, fileLocation);
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
        public String getType() {
            return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_TYPE);
        }

        @Override
        public String getPassword() {
            return domibusRawPropertyProvider.getRawPropertyValue(DOMIBUS_SECURITY_KEYSTORE_PASSWORD);
        }

        @Override
        public void updateTypeAndFileLocation(String type, String fileLocation) {
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_KEYSTORE_TYPE, type);
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_KEYSTORE_LOCATION, fileLocation);
        }
    }
}