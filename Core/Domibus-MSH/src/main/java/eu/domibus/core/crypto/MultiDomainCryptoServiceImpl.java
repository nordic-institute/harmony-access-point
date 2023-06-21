package eu.domibus.core.crypto;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.DomibusCacheConstants;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class MultiDomainCryptoServiceImpl implements MultiDomainCryptoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainCryptoServiceImpl.class);

    public static final String DOMIBUS_TRUSTSTORE_NAME = "domibus.truststore";
    public static final String DOMIBUS_KEYSTORE_NAME = "domibus.keystore";
    public static final String CERT_VALIDATION_BY_ALIAS = "certValidationByAlias";

    protected volatile Map<Domain, DomainCryptoService> domainCertificateProviderMap = new HashMap<>();

    protected final DomainCryptoServiceFactory domainCryptoServiceFactory;

    protected DomibusLocalCacheService domibusLocalCacheService;

    protected CertificateHelper certificateHelper;

    protected CertificateService certificateService;

    protected final DomainService domainService;

    private final KeystorePersistenceService keystorePersistenceService;

    private final ObjectProvider<DomibusCryptoType> domibusCryptoTypes;

    public MultiDomainCryptoServiceImpl(DomainCryptoServiceFactory domainCryptoServiceFactory,
                                        DomibusLocalCacheService domibusLocalCacheService,
                                        CertificateHelper certificateHelper,
                                        CertificateService certificateService,
                                        DomainService domainService,
                                        KeystorePersistenceService keystorePersistenceService,
                                        ObjectProvider<DomibusCryptoType> domibusCryptoTypes) {
        this.domainCryptoServiceFactory = domainCryptoServiceFactory;
        this.domibusLocalCacheService = domibusLocalCacheService;
        this.certificateHelper = certificateHelper;
        this.certificateService = certificateService;
        this.domainService = domainService;
        this.keystorePersistenceService = keystorePersistenceService;
        this.domibusCryptoTypes = domibusCryptoTypes;
    }

    @Override
    public X509Certificate[] getX509Certificates(Domain domain, CryptoType cryptoType) throws WSSecurityException {
        LOG.debug("Get certificates for domain [{}] and cryptoType [{}]", domain,
                domibusCryptoTypes.getObject(cryptoType).asString());
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
    public void resetTrustStore(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.resetTrustStore();
    }

    @Override
    public void resetSecurityProfiles(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.resetSecurityProfiles();
    }

    @Override
    public boolean isTrustStoreChangedOnDisk(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.isTrustStoreChangedOnDisk();
    }

    @Override
    public boolean isKeyStoreChangedOnDisk(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.isKeyStoreChangedOnDisk();
    }

    @Override
    public String getTrustStoreFileExtension() {
        return certificateHelper.getStoreFileExtension(keystorePersistenceService.getTrustStorePersistenceInfo().getType());
    }

    @Override
    public void replaceTrustStore(Domain domain, KeyStoreContentInfo storeInfo) {
        replaceStore(domain, storeInfo, (domainCertificateProvider) -> domainCertificateProvider.replaceTrustStore(storeInfo));
    }

    @Override
    public void replaceKeyStore(Domain domain, KeyStoreContentInfo storeInfo) {
        replaceStore(domain, storeInfo, (domainCertificateProvider) -> domainCertificateProvider.replaceKeyStore(storeInfo));
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
    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = CERT_VALIDATION_BY_ALIAS, key = "#domain.code + #alias")
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
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.resetStores();
    }

    @Override
    public void resetKeyStore(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.resetKeyStore();
    }

    @Override
    public List<TrustStoreEntry> getKeyStoreEntries(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getKeyStoreEntries();
    }

    @Override
    public KeyStoreContentInfo getKeyStoreContent(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getKeyStoreContent();
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getTrustStoreEntries();
    }

    @Override
    public KeyStoreContentInfo getTrustStoreContent(Domain domain) {
        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getTrustStoreContent();
    }

    @Override
    public void saveStoresFromDBToDisk() {
        final List<Domain> domains = domainService.getDomains();
        saveStoresFromDBToDisk(domains);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        saveStoresFromDBToDisk(Arrays.asList(domain));
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        domainCertificateProviderMap.remove(domain);
    }

    protected void saveStoresFromDBToDisk(List<Domain> domains) {
        certificateService.saveStoresFromDBToDisk(keystorePersistenceService.getKeyStorePersistenceInfo(), domains);
        certificateService.saveStoresFromDBToDisk(keystorePersistenceService.getTrustStorePersistenceInfo(), domains);
    }

    protected void replaceStore(Domain domain, KeyStoreContentInfo storeInfo, Consumer<DomainCryptoService> storeReplacer) {
        certificateHelper.validateStoreFileName(storeInfo.getFileName());
        if (StringUtils.isEmpty(storeInfo.getType())) {
            storeInfo.setType(certificateHelper.getStoreType(storeInfo.getFileName()));
        }

        final DomainCryptoService domainCertificateProvider = getDomainCertificateProvider(domain);
        storeReplacer.accept(domainCertificateProvider);

        domibusLocalCacheService.clearCache(CERT_VALIDATION_BY_ALIAS);
        saveCertificateAndLogRevocation(domain);
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

    private void saveCertificateAndLogRevocation(Domain domain) {
        // trigger update certificate table
        final KeyStore trustStore = getTrustStore(domain);
        final KeyStore keyStore = getKeyStore(domain);
        certificateService.saveCertificateAndLogRevocation(trustStore, keyStore);
    }

}
