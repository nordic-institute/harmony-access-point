package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.CertificateException;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.crypto.spi.*;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.SecurityUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
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
public class DefaultDomainCryptoServiceSpiImpl implements DomainCryptoServiceSpi {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultDomainCryptoServiceSpiImpl.class);

    protected Domain domain;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final CertificateService certificateService;

    protected final SignalService signalService;

    protected final DomibusCoreMapper coreMapper;

    protected final DomainTaskExecutor domainTaskExecutor;

    protected List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations = new ArrayList<>();

    protected final SecurityUtilImpl securityUtil;

    protected final SecurityProfileValidatorService securityProfileValidatorService;

    private final KeystorePersistenceService keystorePersistenceService;

    private final CertificateHelper certificateHelper;

    private final FileServiceUtil fileServiceUtil;

    public DefaultDomainCryptoServiceSpiImpl(DomibusPropertyProvider domibusPropertyProvider,
                                             CertificateService certificateService,
                                             SignalService signalService,
                                             DomibusCoreMapper coreMapper,
                                             DomainTaskExecutor domainTaskExecutor,
                                             SecurityUtilImpl securityUtil,
                                             SecurityProfileValidatorService securityProfileValidatorService,
                                             KeystorePersistenceService keystorePersistenceService,
                                             CertificateHelper certificateHelper,
                                             FileServiceUtil fileServiceUtil) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateService = certificateService;
        this.signalService = signalService;
        this.coreMapper = coreMapper;
        this.domainTaskExecutor = domainTaskExecutor;
        this.securityUtil = securityUtil;
        this.securityProfileValidatorService = securityProfileValidatorService;
        this.keystorePersistenceService = keystorePersistenceService;
        this.certificateHelper = certificateHelper;
        this.fileServiceUtil = fileServiceUtil;
    }

    public void init() {
        LOG.debug("Initializing the certificate provider for domain [{}]", domain);

        createSecurityProfileAliasConfigurations();

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
    public X509Certificate[] getX509Certificates(CryptoType cryptoType, String alias) throws WSSecurityException {
        final Merlin merlin = getMerlinForAlias(alias);
        if (merlin != null) {
            return merlin.getX509Certificates(cryptoType);
        }
        LOG.error("Could not get certificates for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get certificates for domain: " + domain);
    }

    @Override
    public X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException {
        final Merlin merlin = getMerlinForSingleLegacyAlias();
        if (merlin != null) {
            LOG.info("Legacy single keystore alias is used for domain [{}]", domain);
            return merlin.getX509Certificates(cryptoType);
        }
        LOG.error("Could not get certificates for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get certificates for domain: " + domain);
    }

    @Override
    public String getX509Identifier(X509Certificate cert, String alias) throws WSSecurityException {
        final Merlin merlin = getMerlinForAlias(alias);
        if (merlin != null) {
            return merlin.getX509Identifier(cert);
        }
        LOG.error("Could not get identifier(alias) for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get identifier(alias) of the certificate for domain: " + domain);
    }

    @Override
    public String getX509Identifier(X509Certificate cert) throws WSSecurityException {
        final Merlin merlin = getMerlinForSingleLegacyAlias();
        if (merlin != null) {
            return merlin.getX509Identifier(cert);
        }
        LOG.error("Could not get identifier(alias) for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get identifier(alias) of the certificate for domain: " + domain);
    }

    @Override
    public PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler, String alias) throws WSSecurityException {
        final Merlin merlin = getMerlinForAlias(alias);
        if (merlin != null) {
            return merlin.getPrivateKey(certificate, callbackHandler);
        }
        LOG.error("Could not get private key for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get private key for domain: " + domain);
    }

    @Override
    public PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException {
        final Merlin merlin = getMerlinForSingleLegacyAlias();
        if (merlin != null) {
            return merlin.getPrivateKey(certificate, callbackHandler);
        }
        LOG.error("Could not get private key for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get private key for domain: " + domain);
    }

    @Override
    public PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler, String alias) throws WSSecurityException {
        final Merlin merlin = getMerlinForAlias(alias);
        if (merlin != null) {
            return merlin.getPrivateKey(publicKey, callbackHandler);
        }
        LOG.error("Could not get private key for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get private key for domain: " + domain);
    }

    @Override
    public PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException {
        final Merlin merlin = getMerlinForSingleLegacyAlias();
        if (merlin != null) {
            return merlin.getPrivateKey(publicKey, callbackHandler);
        }
        LOG.error("Could not get private key for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get private key for domain: " + domain);
    }

    @Override
    public PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException {
        final Merlin merlin = getMerlinForAlias(identifier);
        if (merlin != null) {
            return merlin.getPrivateKey(identifier, password);
        }
        LOG.error("Could not get private key for identifier(alias) [{}] on domain [{}]", identifier, domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get private key for domain: " + domain);
    }

    @Override
    public void verifyTrust(PublicKey publicKey, String alias) throws WSSecurityException {
        final Merlin merlin = getMerlinForAlias(alias);
        if (merlin == null) {
            LOG.error("Could not verify trust for domain [{}]", domain);
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not verify trust for domain: " + domain);
        }
        merlin.verifyTrust(publicKey);
    }

    @Override
    public void verifyTrust(PublicKey publicKey) throws WSSecurityException {
        final Merlin merlin = getMerlinForSingleLegacyAlias();
        if (merlin == null) {
            LOG.error("Could not verify trust for domain [{}]", domain);
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not verify trust for domain: " + domain);
        }
        merlin.verifyTrust(publicKey);
    }

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints, String alias)
            throws WSSecurityException {
        final Merlin merlin = getMerlinForAlias(alias);
        if (merlin == null) {
            LOG.error("Could not verify trust for domain [{}]", domain);
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not verify trust for domain: " + domain);
        }
        merlin.verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
    }

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        final Merlin merlin = getMerlinForSingleLegacyAlias();
        if (merlin == null) {
            LOG.error("Could not verify trust for domain [{}]", domain);
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not verify trust for domain: " + domain);
        }
        merlin.verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
    }

    @Override
    public String getDefaultX509Identifier(String alias) throws WSSecurityException {
        final Merlin merlin = getMerlinForAlias(alias);
        if (merlin != null) {
            return merlin.getDefaultX509Identifier();
        }
        LOG.error("Could not get default identifier for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get default identifier for domain: " + domain);
    }

    @Override
    public String getDefaultX509Identifier() throws WSSecurityException {
        final Merlin merlin = getMerlinForSingleLegacyAlias();
        if (merlin != null) {
            return merlin.getDefaultX509Identifier();
        }
        LOG.error("Could not get default identifier for domain [{}]", domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get default identifier for domain: " + domain);
    }

    private Merlin getMerlinForAlias(String alias) {
        return securityProfileAliasConfigurations.stream()
                .filter(profileConfiguration -> profileConfiguration.getAlias().equalsIgnoreCase(alias))
                .map(SecurityProfileAliasConfiguration::getMerlin)
                .findFirst().orElse(null);
    }

    private Merlin getMerlinForSingleLegacyAlias() {
        return securityProfileAliasConfigurations.stream()
                .map(SecurityProfileAliasConfiguration::getMerlin)
                .findFirst().orElse(null);
    }

    @Override
    public KeyStore getKeyStore() {
        return securityProfileAliasConfigurations.stream()
                .map(securityProfileConfiguration -> securityProfileConfiguration.getMerlin().getKeyStore())
                .findFirst()
                .orElse(null);
    }

    @Override
    public KeyStore getTrustStore() {
        return securityProfileAliasConfigurations.stream()
                .map(securityProfileConfiguration -> securityProfileConfiguration.getMerlin().getTrustStore())
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return securityProfileAliasConfigurations.stream()
                .filter(profileConfiguration -> profileConfiguration.getAlias().equalsIgnoreCase(alias))
                .map(SecurityProfileAliasConfiguration::getPassword)
                .findAny().orElse(null);
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
    public void resetSecurityProfiles() {
        LOG.info("Resetting security profiles on domain [{}]", domain);
        init();
    }

    @Override
    public synchronized void replaceTrustStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        replaceStore(storeContent, storeFileName, storePassword, DOMIBUS_TRUSTSTORE_NAME,
                keystorePersistenceService::getTrustStorePersistenceInfo, this::reloadTrustStore, this::validateTrustStoreCertificateTypes);
    }

    @Override
    public synchronized void replaceKeyStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        replaceStore(storeContent, storeFileName, storePassword, DOMIBUS_KEYSTORE_NAME,
                keystorePersistenceService::getKeyStorePersistenceInfo, this::reloadKeyStore, this::validateKeyStoreCertificateTypes);
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
        List<CertificateEntry> certificates = Collections.singletonList(new CertificateEntry(alias, certificate));
        return addCertificates(overwrite, certificates);
    }

    @Override
    public synchronized void addCertificate(List<CertificateEntrySpi> certs, boolean overwrite) {
        List<CertificateEntry> certificates = certs.stream().map(el -> new CertificateEntry(el.getAlias(), el.getCertificate()))
                .collect(Collectors.toList());
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

    @Override
    public boolean isTrustStoreChanged() {
        return certificateService.isStoreChangedOnDisk(getTrustStore(), keystorePersistenceService.getTrustStorePersistenceInfo());
    }

    @Override
    public boolean isKeyStoreChanged() {
        return certificateService.isStoreChangedOnDisk(getKeyStore(), keystorePersistenceService.getKeyStorePersistenceInfo());
    }

    protected boolean addCertificates(boolean overwrite, List<CertificateEntry> certificates) {
        boolean added = certificateService.addCertificates(keystorePersistenceService.getTrustStorePersistenceInfo(), certificates, overwrite);
        if (added) {
            resetTrustStore();
        }
        return added;
    }

    private byte[] getContentFromFile(String location) {
        try {
            return fileServiceUtil.getContentFromFile(location);
        } catch (IOException e) {
            throw new DomibusCertificateException("Could not read store from [" + location + "]");
        }
    }

    protected boolean removeCertificates(List<String> aliases) {
        boolean removed = certificateService.removeCertificates(keystorePersistenceService.getTrustStorePersistenceInfo(), aliases);
        if (removed) {
            resetTrustStore();
        }
        return removed;
    }

    protected synchronized void replaceStore(byte[] storeContent, String storeFileName, String storePassword,
                                             String storeName, Supplier<KeystorePersistenceInfo> persistenceInfoGetter,
                                             Runnable storeReloader, Consumer<KeyStore> certificateTypeValidator) throws CryptoSpiException {
        boolean replaced;
        try {
            KeyStoreContentInfo storeContentInfo = certificateHelper.createStoreContentInfo(storeName, storeFileName, storeContent, storePassword);
            KeystorePersistenceInfo persistenceInfo = persistenceInfoGetter.get();

            final KeyStore newStore = certificateService.loadStore(storeContentInfo);
            certificateTypeValidator.accept(newStore);

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

    protected void validateTrustStoreCertificateTypes(KeyStore trustStore) {
        securityProfileValidatorService.validateStoreCertificateTypes(securityProfileAliasConfigurations, trustStore, StoreType.TRUSTSTORE);
    }

    protected void initTrustStore() {
        initStore(DOMIBUS_TRUSTSTORE_NAME, this::loadTrustStoreProperties, keystorePersistenceService::getTrustStorePersistenceInfo,
                (keyStore, profileConfiguration) -> profileConfiguration.getMerlin().setTrustStore(keyStore), this::validateTrustStoreCertificateTypes);
    }

    protected void loadTrustStoreProperties() {
        KeystorePersistenceInfo persistenceInfo = keystorePersistenceService.getTrustStorePersistenceInfo();
        loadStoreProperties(persistenceInfo, this::loadTrustStorePropertiesForMerlin);
    }

    protected void loadStoreProperties(KeystorePersistenceInfo persistenceInfo, BiConsumer<KeystorePersistenceInfo, SecurityProfileAliasConfiguration> storePropertiesLoader) {
        final String trustStoreType = persistenceInfo.getType();
        final String trustStorePassword = persistenceInfo.getPassword();

        if (StringUtils.isAnyEmpty(trustStoreType, trustStorePassword)) {
            String message = String.format("One of the [%s] property values is null for domain [%s]: Type=[%s], Password",
                    persistenceInfo.getName(), domain, trustStoreType);
            LOG.error(message);
            throw new ConfigurationException(message);
        }

        securityProfileAliasConfigurations.forEach(
                profileConfiguration -> storePropertiesLoader.accept(persistenceInfo, profileConfiguration));
    }

    private void loadTrustStorePropertiesForMerlin(KeystorePersistenceInfo persistenceInfo, SecurityProfileAliasConfiguration profileAliasConfiguration) {
        try {
            profileAliasConfiguration.getMerlin()
                    .loadProperties(getTrustStoreProperties(persistenceInfo.getType(), persistenceInfo.getPassword()), Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            LOG.error("Error occurred when loading the properties of the TrustStore");
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of TrustStore: " + e.getMessage(), e);
        }
    }

    protected void validateKeyStoreCertificateTypes(KeyStore keystore) {
        securityProfileValidatorService.validateStoreCertificateTypes(securityProfileAliasConfigurations, keystore, StoreType.KEYSTORE);
    }

    protected void initKeyStore() {
        initStore(DOMIBUS_KEYSTORE_NAME, this::loadKeyStoreProperties, keystorePersistenceService::getKeyStorePersistenceInfo,
                (keyStore, profileConfiguration) -> profileConfiguration.getMerlin().setKeyStore(keyStore), this::validateKeyStoreCertificateTypes);
    }

    protected void initStore(String storeName, Runnable propertiesLoader, Supplier<KeystorePersistenceInfo> persistenceInfoGetter,
                             BiConsumer<KeyStore, SecurityProfileAliasConfiguration> merlinStoreSetter, Consumer<KeyStore> certificateTypeValidator) {
        LOG.debug("Initializing the [{}] certificate provider for domain [{}]", storeName, domain);

        domainTaskExecutor.submit(() -> {
            propertiesLoader.run();

            KeyStore store = certificateService.getStore(persistenceInfoGetter.get());

            try {
                certificateTypeValidator.accept(store);
            } catch (CertificateException e) {
                LOG.error("Error validating store [{}]: {}", storeName, e.getMessage());
            }

            securityProfileAliasConfigurations.forEach(
                    profileConfiguration -> merlinStoreSetter.accept(store, profileConfiguration));
        }, domain);

        LOG.debug("Finished initializing the [{}] certificate provider for domain [{}]", storeName, domain);
    }

    private void addSecurityProfileAliasConfiguration(String aliasProperty, String passwordProperty, SecurityProfile securityProfile) {
        final String aliasValue = domibusPropertyProvider.getProperty(domain, aliasProperty);
        final String passwordValue = domibusPropertyProvider.getProperty(domain, passwordProperty);

        String aliasDescription = StringUtils.substringBefore(StringUtils.substringAfter(aliasProperty, "key.private."), "alias=");

        if (StringUtils.isNotBlank(aliasValue) && StringUtils.isBlank(passwordValue)) {
            String message = String.format("The private key password corresponding to the alias=[%s] was not set for domain [%s]: ", aliasValue, domain);
            throw new ConfigurationException(message);
        }

        checkIfAliasIsDuplicated(aliasValue, aliasDescription);

        if (StringUtils.isNotBlank(aliasValue)) {
            SecurityProfileAliasConfiguration profileAliasConfiguration = new SecurityProfileAliasConfiguration(aliasValue, passwordValue, new Merlin(), securityProfile, aliasDescription);
            securityProfileAliasConfigurations.add(profileAliasConfiguration);
        }
    }

    private void checkIfAliasIsDuplicated(String aliasValue, String aliasDescription) {
        Optional<SecurityProfileAliasConfiguration> existing = securityProfileAliasConfigurations.stream()
                .filter(configuration -> configuration.getAlias().equalsIgnoreCase(aliasValue))
                .findFirst();
        if (existing.isPresent()) {
            String message = getDuplicateErrorMessage(aliasValue, aliasDescription, existing.get());
            throw new ConfigurationException(message);
        }
    }

    private String getDuplicateErrorMessage(String aliasValue, String aliasDescription, SecurityProfileAliasConfiguration profileConfiguration) {
        if (securityProfileValidatorService.isLegacySingleAliasKeystoreDefined()) {
            return String.format("Both legacy single keystore alias [%s] and security profile alias [%s] for [%s] are defined for domain: [%s]",
                    aliasValue, profileConfiguration.getAlias(), aliasDescription, domain);
        }
        return String.format("Keystore alias [%s] for [%s] already used on domain [%s] for [%s]. All RSA and ECC aliases (decrypt, sign) must be different from each other.",
                aliasValue, aliasDescription, domain, profileConfiguration.getDescription());
    }

    protected void createSecurityProfileAliasConfigurations() {

        securityProfileAliasConfigurations.clear();

        //without Security Profiles
        boolean legacySingleAliasKeystore  = securityProfileValidatorService.isLegacySingleAliasKeystoreDefined();
        if (legacySingleAliasKeystore) {
            addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD, null);
        }

        //RSA Profile
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_PASSWORD, SecurityProfile.RSA);
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_PASSWORD, SecurityProfile.RSA);

        //ECC Profile
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_PASSWORD, SecurityProfile.ECC);
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_PASSWORD, SecurityProfile.ECC);

        if (legacySingleAliasKeystore && securityProfileAliasConfigurations.size() > 1) {
            LOG.error("Both legacy single keystore alias and security profiles are defined for domain [{}]. Please define only legacy single keystore alias" +
                    " or security profiles.", domain);

            throw new ConfigurationException("Both legacy single keystore alias and security profiles are defined for domain: " + domain);
        }

        if (securityProfileAliasConfigurations.size() == 0) {
            throw new ConfigurationException("No keystore alias defined for domain: " + domain);
        }

        LOG.debug("Created security profile alias configurations for domain [{}]", domain);
    }

    protected void loadKeyStoreProperties() {
        KeystorePersistenceInfo persistenceInfo = keystorePersistenceService.getKeyStorePersistenceInfo();
        loadStoreProperties(persistenceInfo, this::loadKeyStorePropertiesForMerlin);
    }

    private void loadKeyStorePropertiesForMerlin(KeystorePersistenceInfo persistenceInfo, SecurityProfileAliasConfiguration profileAliasConfiguration) {
        try {
            profileAliasConfiguration.getMerlin()
                    .loadProperties(getKeyStoreProperties(profileAliasConfiguration.getAlias(), persistenceInfo.getType(), persistenceInfo.getPassword()),
                            Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            LOG.error("Error occurred when loading the properties of the keystore");
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of keystore: " + e.getMessage(), e);
        }
    }

    protected Properties getKeyStoreProperties(String alias, String keystoreType, String keystorePassword) {
        if (StringUtils.isEmpty(alias)) {
            LOG.error("The keystore alias [{}] for domain [{}] is null", alias, domain);
            throw new ConfigurationException("Error while trying to load the keystore alias for domain: " + domain);
        }

        Properties properties = new Properties();
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystoreType);
        final String keyStorePasswordProperty = Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD; //NOSONAR
        properties.setProperty(keyStorePasswordProperty, keystorePassword);
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, alias);

        Properties logProperties = new Properties();
        logProperties.putAll(properties);
        logProperties.remove(keyStorePasswordProperty);
        LOG.debug("Keystore properties for domain [{}] and alias [{}]are [{}]", domain, alias, logProperties);

        return properties;
    }

    protected Properties getTrustStoreProperties(String trustStoreType, String trustStorePassword) {
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
                (keyStore, securityProfileConfiguration) -> securityProfileConfiguration.getMerlin().setKeyStore(keyStore),
                signalService::signalKeyStoreUpdate, this::validateKeyStoreCertificateTypes);
    }

    private synchronized void reloadTrustStore() throws CryptoSpiException {
        reloadStore(keystorePersistenceService::getTrustStorePersistenceInfo, this::getTrustStore, this::loadTrustStoreProperties,
                (keyStore, securityProfileConfiguration) -> securityProfileConfiguration.getMerlin().setTrustStore(keyStore),
                signalService::signalTrustStoreUpdate, this::validateTrustStoreCertificateTypes);
    }

    private synchronized void reloadStore(Supplier<KeystorePersistenceInfo> persistenceGetter,
                                          Supplier<KeyStore> storeGetter,
                                          Runnable storePropertiesLoader,
                                          BiConsumer<KeyStore, SecurityProfileAliasConfiguration> storeSetter,
                                          Consumer<Domain> signaller,
                                          Consumer<KeyStore> certificateTypeValidator) throws CryptoSpiException {
        KeystorePersistenceInfo persistenceInfo = persistenceGetter.get();
        String storeLocation = persistenceInfo.getFileLocation();
        try {
            KeyStore currentStore = storeGetter.get();
            final KeyStore newStore = certificateService.getStore(persistenceInfo);
            String storeName = persistenceInfo.getName();
            certificateTypeValidator.accept(newStore);
            if (securityUtil.areKeystoresIdentical(currentStore, newStore)) {
                LOG.info("[{}] on disk and in memory are identical, so no reloading.", storeName);
                return;
            }

            LOG.info("Replacing the [{}] with entries [{}] with the one from the file [{}] with entries [{}] on domain [{}].",
                    storeName, certificateService.getStoreEntries(currentStore), storeLocation, certificateService.getStoreEntries(newStore), domain);
            storePropertiesLoader.run();
            securityProfileAliasConfigurations.forEach(securityProfileConfiguration -> {
                storeSetter.accept(newStore, securityProfileConfiguration);
                securityProfileConfiguration.getMerlin().clearCache();
            });

            signaller.accept(domain);
        } catch (CryptoException ex) {
            throw new CryptoSpiException("Error while replacing the keystore from file " + storeLocation, ex);
        }
    }
}
