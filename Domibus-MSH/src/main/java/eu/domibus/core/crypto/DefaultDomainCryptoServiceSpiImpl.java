package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.crypto.spi.*;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.SecurityUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author Cosmin Baciu
 * @since 4.0
 * <p>
 * Default authentication implementation of the SPI. Cxf-Merlin.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Qualifier(AbstractCryptoServiceSpi.DEFAULT_AUTHENTICATION_SPI)
public class DefaultDomainCryptoServiceSpiImpl extends Merlin implements DomainCryptoServiceSpi {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultDomainCryptoServiceSpiImpl.class);

    protected Domain domain;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final CertificateService certificateService;

    protected final SignalService signalService;

    protected final DomibusCoreMapper coreMapper;

    protected final DomainTaskExecutor domainTaskExecutor;

    private final CertificateHelper certificateHelper;

    protected final SecurityUtilImpl securityUtil;

    private final KeystorePersistenceService keystorePersistenceService;

    private final FileServiceUtil fileServiceUtil;

    public DefaultDomainCryptoServiceSpiImpl(DomibusPropertyProvider domibusPropertyProvider,
                                             CertificateService certificateService,
                                             SignalService signalService,
                                             DomibusCoreMapper coreMapper,
                                             DomainTaskExecutor domainTaskExecutor,
                                             CertificateHelper certificateHelper,
                                             SecurityUtilImpl securityUtil,
                                             KeystorePersistenceService keystorePersistenceService,
                                             FileServiceUtil fileServiceUtil) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateService = certificateService;
        this.signalService = signalService;
        this.coreMapper = coreMapper;
        this.domainTaskExecutor = domainTaskExecutor;
        this.certificateHelper = certificateHelper;
        this.securityUtil = securityUtil;
        this.keystorePersistenceService = keystorePersistenceService;
        this.fileServiceUtil = fileServiceUtil;
    }

    public void init() {
        LOG.debug("Initializing the certificate provider for domain [{}]", domain);
        initTrustStore();
        initKeyStore();
        LOG.debug("Finished initializing the certificate provider for domain [{}]", domain);
    }

    @Override
    public X509Certificate getCertificateFromKeyStore(String alias) throws KeyStoreException {
        return (X509Certificate) getKeyStore().getCertificate(alias);
    }

    @Override
    public X509Certificate getCertificateFromTrustStore(String alias) throws KeyStoreException {
        return (X509Certificate) getTrustStore().getCertificate(alias);
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD);
    }

    @Override
    public synchronized void refreshTrustStore() {
        reloadTrustStore();
    }

    @Override
    public synchronized void refreshKeyStore() {
        reloadKeyStore();
    }

    @Override
    public void resetKeyStore() {
        reloadKeyStore();
    }

    @Override
    public void resetTrustStore() {
        reloadTrustStore();
    }

    @Override
    public boolean isTrustStoreChanged() {
        return certificateService.isStoreChangedOnDisk(getTrustStore(), keystorePersistenceService.getTrustStorePersistenceInfo());
    }

    @Override
    public boolean isKeyStoreChanged() {
        return certificateService.isStoreChangedOnDisk(getKeyStore(), keystorePersistenceService.getKeyStorePersistenceInfo());
    }

    @Override
    public synchronized void replaceTrustStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        replaceStore(storeContent, storeFileName, storePassword, DOMIBUS_TRUSTSTORE_NAME,
                keystorePersistenceService::getTrustStorePersistenceInfo, this::reloadTrustStore);
    }

    @Override
    public synchronized void replaceKeyStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        replaceStore(storeContent, storeFileName, storePassword, DOMIBUS_KEYSTORE_NAME,
                keystorePersistenceService::getKeyStorePersistenceInfo, this::reloadKeyStore);
    }

    @Override
    public synchronized void replaceTrustStore(String storeFileLocation, String storePassword) throws CryptoSpiException {
        Path path = Paths.get(storeFileLocation);
        String storeName = path.getFileName().toString();
        byte[] storeContent = getContentFromFile(storeFileLocation);
        replaceTrustStore(storeContent, storeName, storePassword);
    }

    @Override
    public synchronized void replaceKeyStore(String storeFileLocation, String storePassword) {
        Path path = Paths.get(storeFileLocation);
        String storeName = path.getFileName().toString();
        byte[] storeContent = getContentFromFile(storeFileLocation);
        replaceKeyStore(storeContent, storeName, storePassword);
    }

    @Override
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException {
        LOG.debug("Checking certificate validation for [{}]", alias);
        KeyStore trustStore = getTrustStore();
        return certificateService.isCertificateChainValid(trustStore, alias);
    }

    @Override
    public synchronized boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        List<CertificateEntry> certificates = Arrays.asList(new CertificateEntry(alias, certificate));
        return addCertificates(overwrite, certificates);
    }

    @Override
    public synchronized void addCertificate(List<CertificateEntrySpi> certs, boolean overwrite) {
        List<CertificateEntry> certificates = certs.stream().map(el -> new CertificateEntry(el.getAlias(), el.getCertificate())).collect(Collectors.toList());
        addCertificates(overwrite, certificates);
    }

    @Override
    public synchronized boolean removeCertificate(String alias) {
        List<String> aliases = Collections.singletonList(alias);
        return removeCertificates(aliases);
    }

    @Override
    public synchronized void removeCertificate(List<String> aliases) {
        removeCertificates(aliases);
    }

    @Override
    public String getIdentifier() {
        return AbstractCryptoServiceSpi.DEFAULT_AUTHENTICATION_SPI;
    }

    @Override
    public void setDomain(DomainSpi domain) {
        this.domain = coreMapper.domainSpiToDomain(domain);
    }

    protected boolean addCertificates(boolean overwrite, List<CertificateEntry> certificates) {
        boolean added = certificateService.addCertificates(keystorePersistenceService.getTrustStorePersistenceInfo(), certificates, overwrite);
        if (added) {
            resetTrustStore();
        }
        return added;
    }

    protected boolean removeCertificates(List<String> aliases) {
        boolean removed = certificateService.removeCertificates(keystorePersistenceService.getTrustStorePersistenceInfo(), aliases);
        if (removed) {
            resetTrustStore();
        }
        return removed;
    }

    protected synchronized void replaceStore(byte[] storeContent, String storeFileName, String storePassword,
                                             String storeName, Supplier<KeystorePersistenceInfo> persistenceInfoGetter, Runnable storeReloader) throws CryptoSpiException {
        boolean replaced;
        try {
            KeyStoreContentInfo storeContentInfo = certificateHelper.createStoreContentInfo(storeName, storeFileName, storeContent, storePassword);
            KeystorePersistenceInfo persistenceInfo = persistenceInfoGetter.get();
            replaced = certificateService.replaceStore(storeContentInfo, persistenceInfo);
        } catch (CryptoException ex) {
            throw new CryptoSpiException(String.format("Error while replacing the store [%s] with content of the file named [%s].", storeName, storeFileName), ex);
        }

        if (!replaced) {
            throw new SameResourceCryptoSpiException(storeName, storeFileName,
                    String.format("Current store [%s] was not replaced with the content of the file [%s] because they are identical.",
                            storeName, storeFileName));
        }
        storeReloader.run();
    }

    private byte[] getContentFromFile(String location) {
        try {
            return fileServiceUtil.getContentFromFile(location);
        } catch (IOException e) {
            throw new DomibusCertificateException("Could not read store from [" + location + "]");
        }
    }

    protected void initTrustStore() {
        initStore(DOMIBUS_TRUSTSTORE_NAME, this::loadTrustStoreProperties, keystorePersistenceService::getTrustStorePersistenceInfo,
                super::setTrustStore);
    }

    protected void loadTrustStoreProperties() {
        KeystorePersistenceInfo persistenceInfo = keystorePersistenceService.getTrustStorePersistenceInfo();
        loadStoreProperties(persistenceInfo, this::loadTrustStorePropertiesForMerlin);
    }

    protected void loadTrustStorePropertiesForMerlin(KeystorePersistenceInfo persistenceInfo) {
        try {
            super.loadProperties(getTrustStoreProperties(persistenceInfo.getType(), persistenceInfo.getPassword()),
                    Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of TrustStore: " + e.getMessage(), e);
        }
    }

    protected void loadStoreProperties(KeystorePersistenceInfo persistenceInfo, Consumer<KeystorePersistenceInfo> storePropertiesLoader) {
        final String trustStoreType = persistenceInfo.getType();
        final String trustStorePassword = persistenceInfo.getPassword();

        if (StringUtils.isAnyEmpty(trustStoreType, trustStorePassword)) {
            String message = String.format("One of the [%s] property values is null for domain [%s]: Type=[%s], Password",
                    persistenceInfo.getName(), domain, trustStoreType);
            LOG.error(message);
            throw new ConfigurationException(message);
        }

        storePropertiesLoader.accept(persistenceInfo);
    }

    protected void initKeyStore() {
        initStore(DOMIBUS_KEYSTORE_NAME, this::loadKeyStoreProperties, keystorePersistenceService::getKeyStorePersistenceInfo,
                super::setKeyStore);
    }

    protected void initStore(String storeName, Runnable propertiesLoader, Supplier<KeystorePersistenceInfo> persistenceInfoGetter,
                             Consumer<KeyStore> merlinStoreSetter) {
        LOG.debug("Initializing the [{}] certificate provider for domain [{}]", storeName, domain);

        domainTaskExecutor.submit(() -> {
            propertiesLoader.run();

            KeyStore store = certificateService.getStore(persistenceInfoGetter.get());
            merlinStoreSetter.accept(store);
        }, domain);

        LOG.debug("Finished initializing the [{}] certificate provider for domain [{}]", storeName, domain);
    }

    protected void loadKeyStoreProperties() {
        KeystorePersistenceInfo persistenceInfo = keystorePersistenceService.getKeyStorePersistenceInfo();
        loadStoreProperties(persistenceInfo, this::loadKeyStorePropertiesForMerlin);
    }

    protected void loadKeyStorePropertiesForMerlin(KeystorePersistenceInfo persistenceInfo) {
        final String privateKeyAlias = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS);
        try {
            super.loadProperties(getKeyStoreProperties(privateKeyAlias, persistenceInfo.getType(), persistenceInfo.getPassword()),
                    Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of keystore: " + e.getMessage(), e);
        }
    }

    protected Properties getKeyStoreProperties(String alias, String keystoreType, String keystorePassword) {
        if (StringUtils.isAnyEmpty(keystoreType, keystorePassword, alias)) {
            LOG.error("One of the keystore property values is null for domain [{}]: keystoreType=[{}], keystorePassword, privateKeyAlias=[{}]",
                    domain, keystoreType, alias);
            throw new ConfigurationException("Error while trying to load the keystore properties for domain " + domain);
        }

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystoreType);
        final String keyStorePasswordProperty = Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD; //NOSONAR
        result.setProperty(keyStorePasswordProperty, keystorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, alias);

        Properties logProperties = new Properties();
        logProperties.putAll(result);
        logProperties.remove(keyStorePasswordProperty);
        LOG.debug("Keystore properties for domain [{}] are [{}]", domain, logProperties);

        return result;
    }

    protected Properties getTrustStoreProperties(String trustStoreType, String trustStorePassword) {
        if (StringUtils.isAnyEmpty(trustStoreType, trustStorePassword)) {
            LOG.error("One of the truststore property values is null for domain [{}]: trustStoreType=[{}], trustStorePassword",
                    domain, trustStoreType);
            throw new ConfigurationException("Error while trying to load the truststore properties for domain " + domain);
        }

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_TYPE, trustStoreType);
        final String trustStorePasswordProperty = Merlin.PREFIX + Merlin.TRUSTSTORE_PASSWORD; //NOSONAR
        result.setProperty(trustStorePasswordProperty, trustStorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.LOAD_CA_CERTS, "false");

        Properties logProperties = new Properties();
        logProperties.putAll(result);
        logProperties.remove(trustStorePasswordProperty);
        LOG.debug("Truststore properties for domain [{}] are [{}]", domain, logProperties);

        return result;
    }

    private synchronized void reloadKeyStore() throws CryptoSpiException {
        reloadStore(keystorePersistenceService::getKeyStorePersistenceInfo, this::getKeyStore, this::loadKeyStoreProperties,
                super::setKeyStore, signalService::signalKeyStoreUpdate);
    }

    private synchronized void reloadTrustStore() throws CryptoSpiException {
        reloadStore(keystorePersistenceService::getTrustStorePersistenceInfo, this::getTrustStore, this::loadTrustStoreProperties,
                super::setTrustStore, signalService::signalTrustStoreUpdate);
    }

    private synchronized void reloadStore(Supplier<KeystorePersistenceInfo> persistenceGetter,
                                          Supplier<KeyStore> storeGetter,
                                          Runnable storePropertiesLoader,
                                          Consumer<KeyStore> storeSetter,
                                          Consumer<Domain> signaller) throws CryptoSpiException {
        KeystorePersistenceInfo persistenceInfo = persistenceGetter.get();
        String storeLocation = persistenceInfo.getFileLocation();
        try {
            KeyStore currentStore = storeGetter.get();
            final KeyStore newStore = certificateService.getStore(persistenceInfo);
            String storeName = persistenceInfo.getName();
            if (securityUtil.areKeystoresIdentical(currentStore, newStore)) {
                LOG.info("[{}] on disk and in memory are identical, so no reloading.", storeName);
                return;
            }

            LOG.info("Replacing the [{}] with entries [{}] with the one from the file [{}] with entries [{}] on domain [{}].",
                    storeName, certificateService.getStoreEntries(currentStore), storeLocation, certificateService.getStoreEntries(newStore), domain);
            storePropertiesLoader.run();
            storeSetter.accept(newStore);
            super.clearCache();

            signaller.accept(domain);
        } catch (CryptoException ex) {
            throw new CryptoSpiException("Error while replacing the keystore from file " + storeLocation, ex);
        }
    }
}
