package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.TruststoreInfo;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.crypto.spi.*;
import eu.domibus.core.exception.ConfigurationException;
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

    protected Map<String, Merlin> aliasesAndMerlinInstancesMap = new LinkedHashMap<>();

    protected Map<String, String> aliasesAndPasswordsMap = new LinkedHashMap<>();

    protected Boolean isLegacySingleAliasKeystoreDefined = false;


    public DefaultDomainCryptoServiceSpiImpl(DomibusPropertyProvider domibusPropertyProvider,
                                             CertificateService certificateService,
                                             SignalService signalService,
                                             DomibusCoreMapper coreMapper,
                                             DomainTaskExecutor domainTaskExecutor) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateService = certificateService;
        this.signalService = signalService;
        this.coreMapper = coreMapper;
        this.domainTaskExecutor = domainTaskExecutor;
    }

    public void init() {
        LOG.debug("Initializing the certificate provider for domain [{}]", domain);
        createAliasesMaps();
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
        final Merlin merlin = aliasesAndMerlinInstancesMap.get(alias);
        if (merlin != null) {
            return merlin.getX509Certificates(cryptoType);
        }
        return null;
    }

    @Override
    public X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException {
        if (!isLegacySingleAliasKeystoreDefined) {
            LOG.error("Legacy single keystore alias is not defined for domain [{}]", domain);
        } else if (aliasesAndMerlinInstancesMap.values().stream().findFirst().isPresent()) {
            LOG.info("Legacy single keystore alias is is used for domain [{}]", domain);
            return aliasesAndMerlinInstancesMap.values().stream().findFirst().get().getX509Certificates(cryptoType);
        }
        return null;
    }

    @Override
    public String getX509Identifier(X509Certificate cert, String alias) throws WSSecurityException {
        final Merlin merlin = aliasesAndMerlinInstancesMap.get(alias);
        if (merlin != null) {
            return merlin.getX509Identifier(cert);
        }
        return null;
    }

    @Override
    public String getX509Identifier(X509Certificate cert) throws WSSecurityException {
        if (!isLegacySingleAliasKeystoreDefined) {
            LOG.error("Legacy single keystore alias is not defined for domain [{}]", domain);
        } else if (aliasesAndMerlinInstancesMap.values().stream().findFirst().isPresent()) {
            return aliasesAndMerlinInstancesMap.values().stream().findFirst().get().getX509Identifier(cert);
        }
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler, String alias) throws WSSecurityException {
        final Merlin merlin = aliasesAndMerlinInstancesMap.get(alias);
        if (merlin != null) {
            return merlin.getPrivateKey(certificate, callbackHandler);
        }
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException {
        if (!isLegacySingleAliasKeystoreDefined) {
            LOG.error("Legacy single keystore alias is not defined for domain [{}]", domain);
        } else if (aliasesAndMerlinInstancesMap.values().stream().findFirst().isPresent()) {
            return aliasesAndMerlinInstancesMap.values().stream().findFirst().get().getPrivateKey(certificate, callbackHandler);
        }
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler, String alias) throws WSSecurityException {
        final Merlin merlin = aliasesAndMerlinInstancesMap.get(alias);
        if (merlin != null) {
            return merlin.getPrivateKey(publicKey, callbackHandler);
        }
        return null;
    }


    @Override
    public PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException {
        if (!isLegacySingleAliasKeystoreDefined) {
            LOG.error("Legacy single keystore alias is not defined for domain [{}]", domain);
        } else if (aliasesAndMerlinInstancesMap.values().stream().findFirst().isPresent()) {
            return aliasesAndMerlinInstancesMap.values().stream().findFirst().get().getPrivateKey(publicKey, callbackHandler);
        }
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException {
        final Merlin merlin = aliasesAndMerlinInstancesMap.get(identifier);
        if (merlin != null) {
            return merlin.getPrivateKey(identifier, password);
        }
        return null;
    }

    @Override
    public void verifyTrust(PublicKey publicKey, String alias) throws WSSecurityException {
        final Merlin merlin = aliasesAndMerlinInstancesMap.get(alias);
        if (merlin != null) {
            merlin.verifyTrust(publicKey);
        } else {
            LOG.error("Alias [{}] not found when verifying trust for domain [{}]", alias, domain);
        }
    }

    @Override
    public void verifyTrust(PublicKey publicKey) throws WSSecurityException {
        if (!isLegacySingleAliasKeystoreDefined) {
            LOG.error("Legacy single keystore alias is not defined for domain [{}]", domain);
        } else if (aliasesAndMerlinInstancesMap.values().stream().findFirst().isPresent()) {
            aliasesAndMerlinInstancesMap.values().stream().findFirst().get().verifyTrust(publicKey);
        } else {
            LOG.error("CryptoBase implementation is not present");
        }
    }

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints, String alias) throws WSSecurityException {
        final Merlin merlin = aliasesAndMerlinInstancesMap.get(alias);
        if (merlin != null) {
            merlin.verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
        } else {
            LOG.error("Alias [{}] not found when verifying trust for domain [{}]", alias, domain);
        }
    }

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        if (!isLegacySingleAliasKeystoreDefined) {
            LOG.error("Legacy single keystore alias is not defined for domain [{}]", domain);
        } else if (aliasesAndMerlinInstancesMap.values().stream().findFirst().isPresent()) {
            aliasesAndMerlinInstancesMap.values().stream().findFirst().get().verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
        } else {
            LOG.error("CryptoBase implementation is not present");
        }
    }

    @Override
    public String getDefaultX509Identifier(String alias) throws WSSecurityException {
        final Merlin merlin = aliasesAndMerlinInstancesMap.get(alias);
        if (merlin != null) {
            return merlin.getDefaultX509Identifier();
        }
        return null;
    }

    @Override
    public String getDefaultX509Identifier() throws WSSecurityException {
        if (!isLegacySingleAliasKeystoreDefined) {
            LOG.error("Legacy single keystore alias is not defined for domain [{}]", domain);
        } else if (aliasesAndMerlinInstancesMap.values().stream().findFirst().isPresent()) {
            return aliasesAndMerlinInstancesMap.values().stream().findFirst().get().getDefaultX509Identifier();
        }
        return null;
    }

    @Override
    public KeyStore getKeyStore() {
        return aliasesAndMerlinInstancesMap.values().stream().map(Merlin::getKeyStore).findFirst().orElse(null);
    }

    @Override
    public KeyStore getTrustStore() {
        return aliasesAndMerlinInstancesMap.values().stream().map(Merlin::getTrustStore).findFirst().orElse(null);
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return aliasesAndPasswordsMap.get(alias);
    }

    @Override
    public synchronized void refreshTrustStore() {
        loadTrustStoreProperties();

        KeyStore old = getTrustStore();
        final KeyStore current = certificateService.getTrustStore(DOMIBUS_TRUSTSTORE_NAME);
        aliasesAndMerlinInstancesMap.forEach(
                (k, v) -> v.setTrustStore(current));

        if (areKeystoresIdentical(old, current)) {
            LOG.debug("New truststore and previous truststore are identical");
        } else {
            signalService.signalTrustStoreUpdate(domain);
        }
    }

    @Override
    public synchronized void refreshKeyStore() {
        loadKeystoreProperties();

        KeyStore old = getKeyStore();
        final KeyStore current = certificateService.getTrustStore(DOMIBUS_KEYSTORE_NAME);
        aliasesAndMerlinInstancesMap.forEach(
                (k, v) -> v.setKeyStore(current));

        if (areKeystoresIdentical(old, current)) {
            LOG.debug("New keystore and previous keystore are identical");
        } else {
            signalService.signalKeyStoreUpdate(domain);
        }
    }

    @Override
    public void resetKeyStore() {
        String location = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_LOCATION);
        String password = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_PASSWORD);
        replaceKeyStore(location, password);
    }

    @Override
    public void resetTrustStore() {
        String location = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String password = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
        replaceTrustStore(location, password);
    }

    @Override
    public synchronized void replaceTrustStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        try {
            certificateService.replaceStore(storeFileName, storeContent, storePassword, DOMIBUS_TRUSTSTORE_NAME);
        } catch (CryptoException ex) {
            throw new CryptoSpiException("Error while replacing the truststore with content of the file named " + storeFileName, ex);
        }
        refreshTrustStore();
    }

    @Override
    public synchronized void replaceTrustStore(String storeLocation, String storePassword) throws CryptoSpiException {
        try {
            certificateService.replaceStore(storeLocation, storePassword, DOMIBUS_TRUSTSTORE_NAME);
        } catch (CryptoException ex) {
            throw new CryptoSpiException("Error while replacing the truststore from " + storeLocation, ex);
        }
        refreshTrustStore();
    }

    @Override
    public synchronized void replaceKeyStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        try {
            certificateService.replaceStore(storeFileName, storeContent, storePassword, DOMIBUS_KEYSTORE_NAME);
        } catch (CryptoException ex) {
            throw new CryptoSpiException("Error while replacing the keystore with content of the file named " + storeFileName, ex);
        }
        refreshKeyStore();
    }

    @Override
    public synchronized void replaceKeyStore(String storeFileLocation, String storePassword) {
        try {
            certificateService.replaceStore(storeFileLocation, storePassword, DOMIBUS_KEYSTORE_NAME);
        } catch (CryptoException ex) {
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

            KeyStore trustStore = certificateService.getTrustStore(DOMIBUS_TRUSTSTORE_NAME);
            aliasesAndMerlinInstancesMap.forEach(
                    (k, v) -> v.setTrustStore(trustStore));
        }, domain);

        LOG.debug("Finished initializing the truststore certificate provider for domain [{}]", domain);
    }

    protected void loadTrustStoreProperties() {
        aliasesAndMerlinInstancesMap.forEach((k, v) -> loadTrustStorePropertiesForMerlin(v));
    }

    private void loadTrustStorePropertiesForMerlin(Merlin merlin) {
        try {
            merlin.loadProperties(getTrustStoreProperties(), Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of TrustStore: " + e.getMessage(), e);
        }
    }

    protected void initKeyStore() {
        LOG.debug("Initializing the keystore certificate provider for domain [{}]", domain);

        domainTaskExecutor.submit(() -> {

            KeyStore keyStore = certificateService.getTrustStore(DOMIBUS_KEYSTORE_NAME);
            aliasesAndMerlinInstancesMap.forEach(
                    (k, v) -> v.setKeyStore(keyStore));
        }, domain);

        LOG.debug("Finished initializing the keyStore certificate provider for domain [{}]", domain);
    }

    private void readPrivateKeyAliasAndPasswordProperties(String aliasProperty, String passwordProperty) {
        final String aliasValue = domibusPropertyProvider.getProperty(domain, aliasProperty);
        final String passwordValue = domibusPropertyProvider.getProperty(domain, passwordProperty);

        if (StringUtils.isNotBlank(aliasValue) && StringUtils.isBlank(passwordValue)) {
            LOG.error("One of the keystore property values is null for domain [{}]: private key alias=[{}], private key password",
                    domain, aliasValue);
            throw new ConfigurationException("Error while trying to load the private key properties for domain: " + domain);
        } else if (StringUtils.isNotBlank(aliasValue)) {
            if (aliasesAndPasswordsMap.containsKey(aliasValue)) {
                throw new ConfigurationException("Keystore alias already defined for domain: " + domain);
            }
            aliasesAndPasswordsMap.put(aliasValue, passwordValue);
        }
    }

    protected void createAliasesMaps() {
        aliasesAndPasswordsMap.clear();

        //without Security Profiles
        if (domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS) != null) {
            readPrivateKeyAliasAndPasswordProperties(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD);

            isLegacySingleAliasKeystoreDefined = true;
        }

        //with Security Profiles

        //RSA Profile
        readPrivateKeyAliasAndPasswordProperties(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_RSA_PASSWORD);
        readPrivateKeyAliasAndPasswordProperties(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_PASSWORD);
        readPrivateKeyAliasAndPasswordProperties(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_PASSWORD);

        //ECC Profile
        readPrivateKeyAliasAndPasswordProperties(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_ECC_PASSWORD);
        readPrivateKeyAliasAndPasswordProperties(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_PASSWORD);
        readPrivateKeyAliasAndPasswordProperties(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_ALIAS, DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_PASSWORD);

        if (isLegacySingleAliasKeystoreDefined && aliasesAndPasswordsMap.size() > 1) {
            LOG.error("Both legacy single keystore alias and security profiles are defined for domain [{}]. Please define only legacy single keystore alias" +
                    " or security profiles.", domain);

            throw new ConfigurationException("Both legacy single keystore alias and security profiles are defined for domain: " + domain);
        }

        Set<String> aliases = aliasesAndPasswordsMap.keySet();

        LOG.debug("Created map of private key aliases [{}] and private key passwords for domain [{}]", aliases, domain);

        aliases.forEach(
                alias -> aliasesAndMerlinInstancesMap.put(alias, new Merlin())
        );

        LOG.debug("Created map of private key aliases [{}] and Merlin instances for domain [{}]", aliases, domain);
    }

    protected void loadKeystoreProperties() {
        createAliasesMaps();

        final String keystoreType = getKeystoreType();
        final String keystorePassword = getKeystorePassword();
        if (StringUtils.isAnyEmpty(keystoreType, keystorePassword)) {
            LOG.error("One of the keystore property values is null for domain [{}]: keystoreType=[{}], keystorePassword",
                    domain, keystoreType);
            throw new ConfigurationException("Error while trying to load the keystore properties for domain: " + domain);
        }

        aliasesAndMerlinInstancesMap.forEach((k, v) -> loadKeyStorePropertiesForMerlin(keystoreType, keystorePassword, k, v));
    }

    private void loadKeyStorePropertiesForMerlin(String keystoreType, String keystorePassword, String k, Merlin v) {
        try {
            v.loadProperties(getKeyStoreProperties(k, keystoreType, keystorePassword), Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of keystore: " + e.getMessage(), e);
        }
    }

    protected Properties getKeyStoreProperties(String alias, String keystoreType, String keystorePassword) {

        if (StringUtils.isEmpty(alias)) {
            LOG.error("The keystore alias [{}] for domain [{}] is null", alias, domain);
            throw new ConfigurationException("Error while trying to load the keystore alias: " + alias + " for domain: " + domain);
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

    protected boolean areKeystoresIdentical(KeyStore store1, KeyStore store2) {
        try {
            if (store1 == null && store2 == null) {
                LOG.debug("Identical keystores: both are null");
                return true;
            }
            if (store1 == null || store2 == null) {
                LOG.debug("Different keystores: [{}] vs [{}]", store1, store2);
                return false;
            }
            if (store1.size() != store2.size()) {
                LOG.debug("Different keystores: [{}] vs [{}] entries", store1.size(), store2.size());
                return false;
            }
            final Enumeration<String> aliases = store1.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if (!store2.containsAlias(alias)) {
                    LOG.debug("Different keystores: [{}] alias is not found in both", alias);
                    return false;
                }
                if (!store1.getCertificate(alias).equals(store2.getCertificate(alias))) {
                    LOG.debug("Different keystores: [{}] certificate is different", alias);
                    return false;
                }
            }
            return true;
        } catch (KeyStoreException e) {
            throw new DomibusCertificateException("Invalid keystore", e);
        }
    }

    private String getKeystoreType() {
        TruststoreInfo trust = certificateService.getTruststoreInfo(DOMIBUS_KEYSTORE_NAME);
        return trust.getType();
    }

    private String getKeystorePassword() {
        TruststoreInfo trust = certificateService.getTruststoreInfo(DOMIBUS_KEYSTORE_NAME);
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
        TruststoreInfo trust = certificateService.getTruststoreInfo(DOMIBUS_TRUSTSTORE_NAME);
        return trust.getPassword();
    }

    protected String getTrustStoreType() {
        TruststoreInfo trust = certificateService.getTruststoreInfo(DOMIBUS_TRUSTSTORE_NAME);
        return trust.getType();
    }

}
