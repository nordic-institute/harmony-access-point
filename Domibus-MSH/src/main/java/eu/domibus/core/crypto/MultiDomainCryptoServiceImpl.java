package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.crypto.api.DomainCryptoServiceFactory;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.callback.CallbackHandler;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class MultiDomainCryptoServiceImpl implements MultiDomainCryptoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainCryptoServiceImpl.class);

    protected volatile Map<Domain, DomainCryptoService> domainCertificateProviderMap = new HashMap<>();

    @Autowired
    DomainCryptoServiceFactory domainCertificateProviderFactory;

    @Autowired
    private DomibusCacheService domibusCacheService;

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
    public void replaceTrustStore(Domain domain, String storeFileName, byte[] store, String password) throws CryptoException {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);

        validateTruststoreType(domainCertificateProvider.getTrustStoreType(), storeFileName);

        domainCertificateProvider.replaceTrustStore(store, password);
        domibusCacheService.clearCache("certValidationByAlias");
    }

    protected void validateTruststoreType(String storeType, String storeFileName) {
        String fileType = FilenameUtils.getExtension(storeFileName).toLowerCase();
        switch (storeType.toLowerCase()) {
            case "pkcs12":
                if (Arrays.asList("p12", "pfx").contains(fileType)) {
                    return;
                }
            case "jks":
                if (Arrays.asList("jks").contains(fileType)) {
                    return;
                }
        }
        throw new InvalidParameterException("Store file type (" + fileType + ") should match the configured truststore type (" + storeType + ").");
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

    protected DomainCryptoService getDomainCertificateProvider(Domain domain) {
        LOG.debug("Get domain CertificateProvider for domain [{}]", domain);
        if (domainCertificateProviderMap.get(domain) == null) {
            synchronized (domainCertificateProviderMap) {
                if (domainCertificateProviderMap.get(domain) == null) { //NOSONAR: double-check locking
                    LOG.debug("Creating domain CertificateProvider for domain [{}]", domain);
                    DomainCryptoService domainCertificateProvider = domainCertificateProviderFactory.createDomainCryptoService(domain);
                    domainCertificateProviderMap.put(domain, domainCertificateProvider);
                }
            }
        }
        return domainCertificateProviderMap.get(domain);
    }

    @Override
    public void reset() {
        domainCertificateProviderMap.values().stream().forEach(service -> service.reset());
    }

    @Override
    public void reset(Domain domain) {
        if (domain == null) {
            throw new InvalidParameterException("Domain is null.");
        }

        final DomainCryptoService domainCertificateProvider = domainCertificateProviderMap.get(domain);
        if (domainCertificateProvider == null) {
            throw new DomibusCertificateException("Domain certificate provider for domain [" + domain.getName() + "] not found.");
        }

        domainCertificateProvider.reset();
    }
}
