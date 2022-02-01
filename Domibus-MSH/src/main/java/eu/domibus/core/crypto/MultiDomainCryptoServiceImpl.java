package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.security.auth.callback.CallbackHandler;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Pattern;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class MultiDomainCryptoServiceImpl implements MultiDomainCryptoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainCryptoServiceImpl.class);

    public final static String DOMIBUS_TRUSTSTORE_NAME = "domibus.truststore";
    public final static String DOMIBUS_KEYSTORE_NAME = "domibus.keystore";
    public static final String CERT_VALIDATION_BY_ALIAS = "certValidationByAlias";

    protected volatile Map<Domain, DomainCryptoService> domainCertificateProviderMap = new HashMap<>();

    protected final DomainCryptoServiceFactory domainCryptoServiceFactory;

    protected DomibusCacheService domibusCacheService;

    protected CertificateHelper certificateHelper;

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected CertificateService certificateService;

    final protected DomibusRawPropertyProvider domibusRawPropertyProvider;

    protected final DomainService domainService;

    public MultiDomainCryptoServiceImpl(DomainCryptoServiceFactory domainCryptoServiceFactory,
                                        DomibusCacheService domibusCacheService,
                                        CertificateHelper certificateHelper,
                                        DomibusPropertyProvider domibusPropertyProvider,
                                        CertificateService certificateService,
                                        DomibusRawPropertyProvider domibusRawPropertyProvider,
                                        DomainService domainService) {
        this.domainCryptoServiceFactory = domainCryptoServiceFactory;
        this.domibusCacheService = domibusCacheService;
        this.certificateHelper = certificateHelper;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateService = certificateService;
        this.domibusRawPropertyProvider = domibusRawPropertyProvider;
        this.domainService = domainService;
    }

    @Override
    public X509Certificate[] getX509Certificates(Domain domain, CryptoType cryptoType) throws WSSecurityException {
        LOG.debug("Get certificates for domain [{}] and cryptoType [{}]", domain, cryptoType);
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getX509Certificates(cryptoType);
    }

    @Override
    public String getX509Identifier(Domain domain, X509Certificate cert) throws WSSecurityException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getX509Identifier(cert);
    }

    @Override
    public PrivateKey getPrivateKey(Domain domain, X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getPrivateKey(certificate, callbackHandler);
    }

    @Override
    public PrivateKey getPrivateKey(Domain domain, PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getPrivateKey(publicKey, callbackHandler);
    }

    @Override
    public PrivateKey getPrivateKey(Domain domain, String identifier, String password) throws WSSecurityException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getPrivateKey(identifier, password);
    }

    @Override
    public void verifyTrust(Domain domain, X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
    }

    @Override
    public void verifyTrust(Domain domain, PublicKey publicKey) throws WSSecurityException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.verifyTrust(publicKey);
    }

    @Override
    public String getDefaultX509Identifier(Domain domain) throws WSSecurityException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getDefaultX509Identifier();
    }

    @Override
    public String getPrivateKeyPassword(Domain domain, String privateKeyAlias) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getPrivateKeyPassword(privateKeyAlias);
    }

    @Override
    public void refreshTrustStore(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.refreshTrustStore();
    }

    @Override
    public void refreshKeyStore(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.refreshKeyStore();
    }

    @Override
    public void replaceTrustStore(Domain domain, String storeFileName, byte[] storeContent, String storePassword) throws CryptoException {
        doReplaceTrustStore(domain, storeFileName, storeContent, storePassword);
    }

    @Override
    public void replaceTrustStore(Domain domain, String storeFileLocation, String storePassword) throws CryptoException {
        doReplaceTrustStore(domain, storeFileLocation, null, storePassword);
    }

    @Override
    public void replaceKeyStore(Domain domain, String storeFileLocation, String storePassword) throws CryptoException {
        certificateHelper.validateStoreType(domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_TYPE), storeFileLocation);
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.replaceKeyStore(storeFileLocation, storePassword);

        saveCertificateAndLogRevocation(domain);
    }

    @Override
    public KeyStore getKeyStore(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getKeyStore();
    }

    @Override
    public KeyStore getTrustStore(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getTrustStore();
    }

    @Override
    @Cacheable(value = "certValidationByAlias", key = "#domain.code + #alias")
    public boolean isCertificateChainValid(Domain domain, String alias) throws DomibusCertificateException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.isCertificateChainValid(alias);
    }

    @Override
    public X509Certificate getCertificateFromKeystore(Domain domain, String alias) throws KeyStoreException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getCertificateFromKeyStore(alias);
    }

    @Override
    public boolean addCertificate(Domain domain, X509Certificate certificate, String alias, boolean overwrite) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.addCertificate(certificate, alias, overwrite);
    }

    @Override
    public void addCertificate(Domain domain, List<CertificateEntry> certificates, boolean overwrite) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.addCertificate(certificates, overwrite);
    }

    @Override
    public X509Certificate getCertificateFromTruststore(Domain domain, String alias) throws KeyStoreException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getCertificateFromTrustStore(alias);
    }

    @Override
    public boolean removeCertificate(Domain domain, String alias) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.removeCertificate(alias);
    }

    @Override
    public void removeCertificate(Domain domain, List<String> aliases) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.removeCertificate(aliases);
    }

    @Override
    public void reset(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCryptoService(domain);
        domainCertificateProvider.reset();
    }

    @Override
    public void reset(Domain domain, KeyStoreType type) {
        final DomainCryptoService domainCertificateProvider = getDomainCryptoService(domain);
        domainCertificateProvider.reset(type);
    }

    @Override
    public byte[] getTruststoreContent(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getTruststoreContent();
    }

    @Override
    public void persistTruststoresIfApplicable() {
        final List<Domain> domains = domainService.getDomains();
        persistTruststoresIfApplicable(domains);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        persistTruststoresIfApplicable(Arrays.asList(domain));
    }

    @Override
    public void onDomainRemoved(Domain domain) {
    }

    private void doReplaceTrustStore(Domain domain, String storeFileNameOrLocation, byte[] storeContent, String storePassword) {
        certificateHelper.validateStoreType(domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_TYPE), storeFileNameOrLocation);

        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        if (storeContent != null) {
            domainCertificateProvider.replaceTrustStore(storeContent, storeFileNameOrLocation, storePassword);
        } else {
            domainCertificateProvider.replaceTrustStore(storeFileNameOrLocation, storePassword);
        }

        domibusCacheService.clearCache(CERT_VALIDATION_BY_ALIAS);
        saveCertificateAndLogRevocation(domain);
    }


    protected void persistTruststoresIfApplicable(List<Domain> domains) {
        certificateService.persistTruststoresIfApplicable(DOMIBUS_TRUSTSTORE_NAME, false,
                () -> Optional.of(domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION)),
                () -> domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_TYPE),
                () -> domibusRawPropertyProvider.getRawPropertyValue(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD),
                domains
        );

        certificateService.persistTruststoresIfApplicable(DOMIBUS_KEYSTORE_NAME, false,
                () -> Optional.of(domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_LOCATION)),
                () -> domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEYSTORE_TYPE),
                () -> domibusRawPropertyProvider.getRawPropertyValue(DOMIBUS_SECURITY_KEYSTORE_PASSWORD),
                domains
        );
    }

    protected DomainCryptoService getDomainCertificateProvider(Domain domain) {
        LOG.debug("Get domain CertificateProvider for domain [{}]", domain);
        if (domainCertificateProviderMap.get(domain) == null) {
            synchronized (domainCertificateProviderMap) {
                if (domainCertificateProviderMap.get(domain) == null) { //NOSONAR: double-check locking
                    LOG.debug("Creating domain CertificateProvider for domain [{}]", domain);
                    DomainCryptoService domainCertificateProvider = domainCryptoServiceFactory.domainCryptoService(domain);
                    domainCertificateProviderMap.put(domain, domainCertificateProvider);
                }
            }
        }
        return domainCertificateProviderMap.get(domain);
    }

    private DomainCryptoService getDomainCryptoService(Domain domain) {
        if (domain == null) {
            throw new InvalidParameterException("Domain is null.");
        }

        final DomainCryptoService domainCertificateProvider = domainCertificateProviderMap.get(domain);
        if (domainCertificateProvider == null) {
            throw new DomibusCertificateException("Domain certificate provider for domain [" + domain.getName() + "] not found.");
        }
        return domainCertificateProvider;
    }

    private void saveCertificateAndLogRevocation(Domain domain) {
        // trigger update certificate table
        final KeyStore trustStore = getTrustStore(domain);
        final KeyStore keyStore = getKeyStore(domain);
        certificateService.saveCertificateAndLogRevocation(trustStore, keyStore);
    }
}
