package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.crypto.spi.*;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.*;

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

    final protected DomibusPropertyProvider domibusPropertyProvider;

    final protected CertificateService certificateService;

    final protected SignalService signalService;

    final protected DomibusCoreMapper coreMapper;

    protected final DomainTaskExecutor domainTaskExecutor;

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
        loadTrustStoreProperties();
        loadKeyStoreProperties();
        LOG.debug("Finished initializing the certificate provider for domain [{}]", domain);
    }

    public void init(List<Enum> initValue) {
        if (initValue.isEmpty()) {
            LOG.debug("No initializing value found for the certificate provider");
            return;
        } else if (initValue.contains(INTI_TRUSTSTORE_NAME) && initValue.contains(INTI_KEYSTORE_NAME)) {
            init();
        } else if (initValue.contains(INTI_TRUSTSTORE_NAME)) {
            loadTrustStoreProperties();
        } else if (initValue.contains(INTI_KEYSTORE_NAME)) {
            loadKeyStoreProperties();
        }

    }

    protected void loadTrustStoreProperties() {
        LOG.debug("Initializing the truststore certificate provider for domain [{}]", domain);
        try {
            super.loadProperties(getTrustStoreProperties(), Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of TrustStore: " + e.getMessage(), e);
        }

        domainTaskExecutor.submit(() -> {
            KeyStore trustStore = loadTrustStore();
            super.setTrustStore(trustStore);
        }, domain);

        LOG.debug("Finished initializing the truststore certificate provider for domain [{}]", domain);
    }

    protected void loadKeyStoreProperties() {
        LOG.debug("Initializing the keystore certificate provider for domain [{}]", domain);
        try {
            super.loadProperties(getKeystoreProperties(), Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of keystore: " + e.getMessage(), e);
        }

        domainTaskExecutor.submit(() -> {
            KeyStore keyStore = certificateService.getTrustStore(DOMIBUS_KEYSTORE_NAME);
            super.setKeyStore(keyStore);
        }, domain);

        LOG.debug("Finished initializing the keyStore certificate provider for domain [{}]", domain);
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
        final KeyStore trustStore = loadTrustStore();
        setTrustStore(trustStore);
        signalService.signalTrustStoreUpdate(domain);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    public synchronized void replaceTrustStore(byte[] store, String password) throws CryptoSpiException {
        try {
            certificateService.replaceTrustStore(store, password, DOMIBUS_TRUSTSTORE_NAME);
        } catch (CryptoException ex) {
            throw new CryptoSpiException(ex);
        }
        refreshTrustStore();
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
        boolean removed = certificateService.removeCertificates(DOMIBUS_TRUSTSTORE_NAME, Arrays.asList(alias));
        if (removed) {
            refreshTrustStore();
        }
        return removed;
    }

    @Override
    public synchronized void removeCertificate(List<String> aliases) {
        boolean removed = certificateService.removeCertificates(DOMIBUS_TRUSTSTORE_NAME, aliases);
        if (removed) {
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

    protected KeyStore loadTrustStore() {
        return certificateService.getTrustStore(DOMIBUS_TRUSTSTORE_NAME);
    }

    protected Properties getKeystoreProperties() {
        final String keystoreType = getKeystoreType();
        final String keystorePassword = getKeystorePassword();
        final String privateKeyAlias = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS);

        if (StringUtils.isAnyEmpty(keystoreType, keystorePassword, privateKeyAlias)) {
            LOG.error("One of the keystore property values is null for domain [{}]: keystoreType=[{}], keystorePassword, privateKeyAlias=[{}]",
                    domain, keystoreType, privateKeyAlias);
            throw new ConfigurationException("Error while trying to load the keystore properties for domain " + domain);
        }

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystoreType);
        final String keyStorePasswordProperty = Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD; //NOSONAR
        result.setProperty(keyStorePasswordProperty, keystorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, privateKeyAlias);

        Properties logProperties = new Properties();
        logProperties.putAll(result);
        logProperties.remove(keyStorePasswordProperty);
        LOG.debug("Keystore properties for domain [{}] are [{}]", domain, logProperties);

        return result;
    }

    private String getKeystoreType() {
        return domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_TYPE);
    }

    private String getKeystorePassword() {
        String password = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_PASSWORD);
        if (StringUtils.isNotBlank(password) && passwordEncryptor != null) {
            return decryptPassword(password, passwordEncryptor);
        }
        return password;
    }

    protected Properties getTrustStoreProperties() {
        final String trustStoreType = getTrustStoreType();
        final String trustStorePassword = getTrustStorePassword();

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

    protected String getTrustStoreLocation() {
        return domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
    }

    protected String getTrustStorePassword() {
        String password = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
        if (StringUtils.isNotBlank(password) && passwordEncryptor != null) {
            return decryptPassword(password, passwordEncryptor);
        }
        return password;
    }

    protected String getTrustStoreType() {
        return domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
    }

    protected String getTrustStoreBackUpLocation() {
        return domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_BACKUP_LOCATION);
    }

}
