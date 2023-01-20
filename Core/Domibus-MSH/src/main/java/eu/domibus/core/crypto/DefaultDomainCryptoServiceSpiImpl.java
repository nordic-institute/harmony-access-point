package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.TruststoreInfo;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.SecurityProfile;
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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
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

    private final CertificateHelper certificateHelper;

    public DefaultDomainCryptoServiceSpiImpl(DomibusPropertyProvider domibusPropertyProvider,
                                             CertificateService certificateService,
                                             SignalService signalService,
                                             DomibusCoreMapper coreMapper,
                                             DomainTaskExecutor domainTaskExecutor,
                                             SecurityUtilImpl securityUtil, CertificateHelper certificateHelper) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateService = certificateService;
        this.signalService = signalService;
        this.coreMapper = coreMapper;
        this.domainTaskExecutor = domainTaskExecutor;
        this.securityUtil = securityUtil;
        this.certificateHelper = certificateHelper;
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
        loadTrustStoreProperties();

        KeyStore old = getTrustStore();
        final KeyStore current = certificateService.getStore(DOMIBUS_TRUSTSTORE_NAME);
        securityProfileAliasConfigurations.forEach(
                securityProfileConfiguration -> securityProfileConfiguration.getMerlin().setTrustStore(current));

        if (securityUtil.areKeystoresIdentical(old, current)) {
            LOG.debug("New truststore and previous truststore are identical");
        } else {
            signalService.signalTrustStoreUpdate(domain);
        }
    }

    @Override
    public synchronized void refreshKeyStore() {
        loadKeystoreProperties();

        KeyStore old = getKeyStore();
        final KeyStore current = certificateService.getStore(DOMIBUS_KEYSTORE_NAME);
        securityProfileAliasConfigurations.forEach(
                securityProfileConfiguration -> {
                    Merlin merlin = securityProfileConfiguration.getMerlin();
                    merlin.setKeyStore(current);
                    merlin.clearCache();
                });

        if (securityUtil.areKeystoresIdentical(old, current)) {
            LOG.debug("New keystore and previous keystore are identical");
        } else {
            signalService.signalKeyStoreUpdate(domain);
        }
    }

    @Override
    public void resetKeyStore() {
        String location = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_LOCATION);
        String password = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_PASSWORD);
        certificateHelper.validateStoreType(domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_TYPE), location);

        LOG.info("Replacing the keystore with the content of the disk file named [{}] on domain [{}].", location, domain);
        replaceKeyStore(location, password);
    }

    @Override
    public void resetTrustStore() {
        String location = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String password = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
        certificateHelper.validateStoreType(domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_TYPE), location);

        LOG.info("Replacing the truststore with the content of the disk file named [{}] on domain [{}].", location, domain);
        replaceTrustStore(location, password);
    }

    @Override
    public void resetSecurityProfiles() {
        LOG.info("Resetting security profiles on domain [{}]", domain);
        init();
    }

    @Override
    public synchronized void replaceTrustStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        try {
            certificateService.replaceStore(storeFileName, storeContent, storePassword, DOMIBUS_TRUSTSTORE_NAME);
        } catch (CryptoException ex) {
            LOG.error("Error while replacing the truststore with content of the file named [{}]", storeFileName);
            throw new CryptoSpiException("Error while replacing the truststore with content of the file named " + storeFileName, ex);
        }
        refreshTrustStore();
    }

    @Override
    public synchronized void replaceTrustStore(String storeLocation, String storePassword) throws CryptoSpiException {
        try {
            certificateService.replaceStore(storeLocation, storePassword, DOMIBUS_TRUSTSTORE_NAME);
        } catch (CryptoException ex) {
            LOG.error("Error while replacing the truststore from [{}]", storeLocation);
            throw new CryptoSpiException("Error while replacing the truststore from " + storeLocation, ex);
        }
        refreshTrustStore();
    }

    @Override
    public synchronized void replaceKeyStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        try {
            certificateService.replaceStore(storeFileName, storeContent, storePassword, DOMIBUS_KEYSTORE_NAME);
        } catch (CryptoException ex) {
            LOG.error("Error while replacing the keystore with content of the file named [{}]", storeFileName);
            throw new CryptoSpiException("Error while replacing the keystore with content of the file named " + storeFileName, ex);
        }
        refreshKeyStore();
    }

    @Override
    public synchronized void replaceKeyStore(String storeFileLocation, String storePassword) {
        try {
            certificateService.replaceStore(storeFileLocation, storePassword, DOMIBUS_KEYSTORE_NAME);
        } catch (CryptoException ex) {
            LOG.error("Error while replacing the keystore from [{}]", storeFileLocation);
            throw new CryptoSpiException("Error while replacing the keystore from " + storeFileLocation, ex);
        }
        refreshKeyStore();
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
        boolean added = certificateService.addCertificates(DOMIBUS_TRUSTSTORE_NAME, certificates, overwrite);
        if (added) {
            refreshTrustStore();
        }
        return added;
    }

    @Override
    public synchronized void addCertificate(List<CertificateEntrySpi> certificates, boolean overwrite) {
        List<CertificateEntry> certificates2 = certificates.stream().map(el -> new CertificateEntry(el.getAlias(), el.getCertificate())).collect(Collectors.toList());
        boolean added = certificateService.addCertificates(DOMIBUS_TRUSTSTORE_NAME, certificates2, overwrite);
        if (added) {
            refreshTrustStore();
        }
    }

    @Override
    public synchronized boolean removeCertificate(String alias) {
        Long entityId = certificateService.removeCertificates(DOMIBUS_TRUSTSTORE_NAME, Collections.singletonList(alias));
        if (entityId != null) {
            refreshTrustStore();
        }
        return entityId != null;
    }

    @Override
    public synchronized void removeCertificate(List<String> aliases) {
        Long entityId = certificateService.removeCertificates(DOMIBUS_TRUSTSTORE_NAME, aliases);
        if (entityId != null) {
            refreshTrustStore();
        }
    }

    @Override
    public String getIdentifier() {
        return AbstractCryptoServiceSpi.DEFAULT_AUTHENTICATION_SPI;
    }

    @Override
    public void setDomain(DomainSpi domain) {
        this.domain = coreMapper.domainSpiToDomain(domain);
    }

    protected void initTrustStore() {
        LOG.debug("Initializing the truststore certificate provider for domain [{}]", domain);

        domainTaskExecutor.submit(() -> {
            loadTrustStoreProperties();

            KeyStore trustStore = certificateService.getStore(DOMIBUS_TRUSTSTORE_NAME);
            securityProfileAliasConfigurations.forEach(
                    profileConfiguration -> profileConfiguration.getMerlin().setTrustStore(trustStore));
        }, domain);

        LOG.debug("Finished initializing the truststore certificate provider for domain [{}]", domain);
    }

    protected void loadTrustStoreProperties() {
        securityProfileAliasConfigurations.forEach(
                profileConfiguration -> loadTrustStorePropertiesForMerlin(profileConfiguration.getMerlin()));
    }

    private void loadTrustStorePropertiesForMerlin(Merlin merlin) {
        try {
            merlin.loadProperties(getTrustStoreProperties(), Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            LOG.error("Error occurred when loading the properties of the TrustStore");
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of TrustStore: " + e.getMessage(), e);
        }
    }

    protected void initKeyStore() {
        LOG.debug("Initializing the keystore certificate provider for domain [{}]", domain);

        domainTaskExecutor.submit(() -> {
            loadKeystoreProperties();

            KeyStore keyStore = certificateService.getStore(DOMIBUS_KEYSTORE_NAME);
            securityProfileAliasConfigurations.forEach(
                    profileConfiguration -> profileConfiguration.getMerlin().setKeyStore(keyStore));
        }, domain);

        LOG.debug("Finished initializing the keyStore certificate provider for domain [{}]", domain);
    }

    private void addSecurityProfileAliasConfiguration(String aliasProperty, String passwordProperty, SecurityProfile securityProfile) {
        final String aliasValue = domibusPropertyProvider.getProperty(domain, aliasProperty);
        final String passwordValue = domibusPropertyProvider.getProperty(domain, passwordProperty);

        String desc = StringUtils.substringBefore(StringUtils.substringAfter(aliasProperty, "key.private."), "alias=");

        if (StringUtils.isNotBlank(aliasValue) && StringUtils.isBlank(passwordValue)) {
            String message = String.format("The private key password corresponding to the alias=[%s] was not set for domain [%s]: ", aliasValue, domain);
            throw new ConfigurationException(message);
        }
        Optional<SecurityProfileAliasConfiguration> existing = securityProfileAliasConfigurations.stream()
                .filter(configuration -> configuration.getAlias().equalsIgnoreCase(aliasValue))
                .findFirst();
        if (existing.isPresent()) {
            String message = String.format("Keystore alias [%s] for [%s] already used on domain [%s] for [%s]. All RSA and ECC aliases (decrypt, sign) must be different from each other.",
                    aliasValue, desc, domain, existing.get().getDescription());
            throw new ConfigurationException(message);
        }
        if (StringUtils.isNotBlank(aliasValue)) {
            SecurityProfileAliasConfiguration profileAliasConfiguration = new SecurityProfileAliasConfiguration(aliasValue, passwordValue, new Merlin(), securityProfile, desc);
            securityProfileAliasConfigurations.add(profileAliasConfiguration);
        }
    }

    protected void createSecurityProfileAliasConfigurations() {

        securityProfileAliasConfigurations.clear();

        //without Security Profiles
        boolean isLegacySingleAliasKeystoreDefined = false;
        if (domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS) != null) {
            addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD, null);
            isLegacySingleAliasKeystoreDefined = true;
        }

        //RSA Profile
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_RSA_PASSWORD, SecurityProfile.RSA);
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_PASSWORD, SecurityProfile.RSA);
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_PASSWORD, SecurityProfile.RSA);

        //ECC Profile
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_ECC_PASSWORD, SecurityProfile.ECC);
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_PASSWORD, SecurityProfile.ECC);
        addSecurityProfileAliasConfiguration(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_PASSWORD, SecurityProfile.ECC);

        if (isLegacySingleAliasKeystoreDefined && securityProfileAliasConfigurations.size() > 1) {
            LOG.error("Both legacy single keystore alias and security profiles are defined for domain [{}]. Please define only legacy single keystore alias" +
                    " or security profiles.", domain);

            throw new ConfigurationException("Both legacy single keystore alias and security profiles are defined for domain: " + domain);
        }

        LOG.debug("Created security profile alias configurations for domain [{}]", domain);
    }

    protected void loadKeystoreProperties() {
        final String keystoreType = getKeystoreType();
        final String keystorePassword = getKeystorePassword();
        if (StringUtils.isAnyEmpty(keystoreType, keystorePassword)) {
            LOG.error("One of the keystore property values is null for domain [{}]: keystoreType=[{}], keystorePassword",
                    domain, keystoreType);
            throw new ConfigurationException("Error while trying to load the keystore properties for domain: " + domain);
        }

        securityProfileAliasConfigurations.forEach(
                securityProfileConfiguration -> loadKeyStorePropertiesForMerlin(keystoreType,
                        keystorePassword,
                        securityProfileConfiguration.getAlias(),
                        securityProfileConfiguration.getMerlin()));
    }

    private void loadKeyStorePropertiesForMerlin(String keystoreType, String keystorePassword, String alias, Merlin merlin) {
        try {
            merlin.loadProperties(getKeyStoreProperties(alias, keystoreType, keystorePassword), Merlin.class.getClassLoader(), null);
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

    private String getKeystoreType() {
        TruststoreInfo trust = certificateService.getStoreInfo(DOMIBUS_KEYSTORE_NAME);
        return trust.getType();
    }

    private String getKeystorePassword() {
        TruststoreInfo trust = certificateService.getStoreInfo(DOMIBUS_KEYSTORE_NAME);
        return trust.getPassword();
    }

    protected Properties getTrustStoreProperties() {
        final String trustStoreType = getTrustStoreType();
        final String trustStorePassword = getTrustStorePassword();

        if (StringUtils.isAnyEmpty(trustStoreType, trustStorePassword)) {
            LOG.error("One of the truststore property values is null for domain [{}]: trustStoreType=[{}], trustStorePassword",
                    domain, trustStoreType);
            throw new ConfigurationException("Error while trying to load the truststore properties for domain: " + domain);
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

    protected String getTrustStorePassword() {
        TruststoreInfo trust = certificateService.getStoreInfo(DOMIBUS_TRUSTSTORE_NAME);
        return trust.getPassword();
    }

    protected String getTrustStoreType() {
        TruststoreInfo trust = certificateService.getStoreInfo(DOMIBUS_TRUSTSTORE_NAME);
        return trust.getType();
    }

}
