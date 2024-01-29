package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.crypto.DomibusCryptoType;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.lock.DomibusSynchronizationException;
import eu.domibus.api.multitenancy.lock.SynchronizationService;
import eu.domibus.api.party.PartyService;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.cryptacular.util.CertUtil;
import org.springframework.beans.factory.ObjectProvider;
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
import java.util.concurrent.Callable;
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

    /**
     * the following locks are used for performing changes to the keystore/truststore in a synchronized way; see also {@link eu.domibus.api.multitenancy.lock.SynchronizationService}
     */

    private static final String JAVA_CHANGE_LOCK = "changeLock";

    private static final String DB_SYNC_LOCK_KEY = "keystore-synchronization.lock";

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
    private final ObjectProvider<DomibusCryptoType> domibusCryptoTypes;
    private final PartyService partyService;

    private final SynchronizationService synchronizationService;

    public DefaultDomainCryptoServiceSpiImpl(DomibusPropertyProvider domibusPropertyProvider,
                                             CertificateService certificateService,
                                             SignalService signalService,
                                             DomibusCoreMapper coreMapper,
                                             DomainTaskExecutor domainTaskExecutor,
                                             SecurityUtilImpl securityUtil,
                                             SecurityProfileValidatorService securityProfileValidatorService,
                                             KeystorePersistenceService keystorePersistenceService,
                                             CertificateHelper certificateHelper,
                                             FileServiceUtil fileServiceUtil,
                                             ObjectProvider<DomibusCryptoType> domibusCryptoTypes,
                                             PartyService partyService,
                                             SynchronizationService synchronizationService) {
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
        this.domibusCryptoTypes = domibusCryptoTypes;
        this.partyService = partyService;
        this.synchronizationService = synchronizationService;
    }

    public void init() {
        LOG.debug("Initializing the certificate provider for domain [{}]", domain);

        createSecurityProfileAliasConfigurations();

        try {
            initTrustStore();
        } catch (Exception ex) {
            LOG.error("Exception while trying to initialize the domibus truststore", ex);
        }
        try {
            initKeyStore();
        } catch (Exception ex) {
            LOG.error("Exception while trying to initialize the domibus keystore", ex);
        }

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
            LOG.info("Legacy single keystore alias is used for domain [{}] for crypto type [{}]", domain, domibusCryptoTypes.getObject(cryptoType).asString());
            X509Certificate[] certificates = merlin.getX509Certificates(cryptoType);
            if (ArrayUtils.isNotEmpty(certificates) && certificates[0] != null) {
                Boolean encryptionCertificatesPrintingEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_LOGGING_REMOTE_CERTIFICATES_PRINT);
                if (encryptionCertificatesPrintingEnabled) {
                    if (isRemoteCertificate(certificates[0], cryptoType)) {
                        logRemoteCertificate(Optional.of(certificates[0]));
                    }
                }
            }
            return certificates;
        }
        LOG.error("Could not get certificates for domain [{}] for crypto type [{}]", domain, domibusCryptoTypes.getObject(cryptoType).asString());
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get certificates for domain: " + domain);
    }

    private boolean isRemoteCertificate(X509Certificate certificate, CryptoType cryptoType) throws WSSecurityException {
        if (certificate == null) {
            LOG.trace("Cannot verify whether the certificate is remote because it is undefined");
            return false;
        }

        if (cryptoType == null) {
            LOG.trace("Cannot verify whether the certificate is remote because the provided crypto type is undefined");
            return false;
        }

        String localPartyName = partyService.getGatewayParty().getName();
        String alias = CertUtil.subjectCN(certificate);

        // The certificate of the remote receiver is used on the sender to encrypt (CryptoType of type ALIAS) while
        // the certificate of the remote sender is used on the receiver to verify trust (CryptoType of type SKI_BYTES)
        return (cryptoType.getType() == CryptoType.TYPE.ALIAS || cryptoType.getType() == CryptoType.TYPE.SKI_BYTES)
                && !StringUtils.equalsIgnoreCase(localPartyName, alias);
    }

    private void logRemoteCertificate(Optional<X509Certificate> certificate) {
        logCertificate(certificate, "Found the certificate of the remote entity used during the encryption or the trust verification phase having alias [{}]");
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
            Boolean signingCertificatesPrintingEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_LOGGING_LOCAL_CERTIFICATES_PRINT);
            if (signingCertificatesPrintingEnabled) {
                logLocalCertificate(Optional.ofNullable(certificate));
            }
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
            Boolean signingCertificatesPrintingEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_LOGGING_LOCAL_CERTIFICATES_PRINT);
            if (signingCertificatesPrintingEnabled) {
                Optional<X509Certificate> certificate = Optional.empty();
                try {
                    certificate = Optional.ofNullable(getCertificateFromKeyStore(identifier));
                } catch (KeyStoreException e) {
                    LOG.error("Could not retrieve from the keystore the certificate corresponding to the private key using the identifier [{}]", identifier);
                }
                logLocalCertificate(certificate);
            }
            return merlin.getPrivateKey(identifier, password);
        }
        LOG.error("Could not get private key for identifier(alias) [{}] on domain [{}]", identifier, domain);
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "Could not get private key for domain: " + domain);
    }

    private void logLocalCertificate(Optional<X509Certificate> certificate) {
        logCertificate(certificate, "Found the certificate of the local entity corresponding to the private key used during the signing or the decryption phase having alias [{}]");
    }

    private void logCertificate(Optional<X509Certificate> certificate, String message) {
        if (!certificate.isPresent()) {
            LOG.info("Not logging any details because the certificate is absent");
            return;
        }

        X509Certificate x509Certificate = certificate.get();
        if (LOG.isInfoEnabled()) {
            LOG.info(message, CertUtil.subjectCN(x509Certificate));
        }

        // Print all certificate details in DEBUG mode
        LOG.debug("Certificate details: [{}]", x509Certificate);

        String fingerprint = certificateService.extractFingerprints(x509Certificate);
        LOG.info("Certificate details of most interest: Fingerprint=[{}], Subject=[{}], Validity=[From: {}, To: {}], Issuer=[{}], SerialNumber=[{}]",
                fingerprint,
                x509Certificate.getSubjectDN(),
                x509Certificate.getNotBefore(),
                x509Certificate.getNotAfter(),
                x509Certificate.getIssuerDN(),
                x509Certificate.getSerialNumber());
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
        Merlin merlin = securityProfileAliasConfigurations.stream()
                .filter(profileConfiguration -> profileConfiguration.getAlias().equalsIgnoreCase(alias))
                .map(SecurityProfileAliasConfiguration::getMerlin)
                .findFirst().orElse(null);
        if (LOG.isTraceEnabled() && merlin != null) {
            LOG.trace("Security provider for merlin keystore: [{}]", merlin.getKeyStore() == null ? null : merlin.getKeyStore().getProvider());
            LOG.trace("Security provider for merlin truststore: [{}]", merlin.getTrustStore() == null ? null : merlin.getTrustStore().getProvider());
        }
        return merlin;
    }

    private Merlin getMerlinForSingleLegacyAlias() {
        Merlin merlin = securityProfileAliasConfigurations.stream()
                .map(SecurityProfileAliasConfiguration::getMerlin)
                .findFirst().orElse(null);
        if (LOG.isTraceEnabled() && merlin != null) {
            LOG.trace("Security provider for merlin keystore: [{}]", merlin.getKeyStore() == null ? null : merlin.getKeyStore().getProvider());
            LOG.trace("Security provider for merlin truststore: [{}]", merlin.getTrustStore() == null ? null : merlin.getTrustStore().getProvider());
        }
        return merlin;
    }

    @Override
    public KeyStore getKeyStore() {
        return securityProfileAliasConfigurations.stream()
                .findFirst()
                .map(securityProfileConfiguration -> securityProfileConfiguration.getMerlin().getKeyStore())
                .orElseThrow(() -> new DomibusCertificateException("Could not find any keystore in the security profile configuration."));
    }

    @Override
    public KeyStore getTrustStore() {
        return securityProfileAliasConfigurations.stream()
                .findFirst()
                .map(securityProfileConfiguration -> securityProfileConfiguration.getMerlin().getTrustStore())
                .orElseThrow(() -> new DomibusCertificateException("Could not find any truststore in the security profile configuration."));
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return securityProfileAliasConfigurations.stream()
                .filter(profileConfiguration -> profileConfiguration.getAlias().equalsIgnoreCase(alias))
                .map(SecurityProfileAliasConfiguration::getPassword)
                .findAny()
                .orElseThrow(() -> new DomibusCertificateException("Could not find private key password."));
    }

    @Override
    public void refreshTrustStore() {
        executeWithLock(this::doResetTrustStore);
    }

    @Override
    public void refreshKeyStore() {
        executeWithLock(this::doReloadKeyStore);
    }

    @Override
    public void resetKeyStore() {
        executeWithLock(this::doReloadKeyStore);
    }

    @Override
    public void resetTrustStore() {
        executeWithLock(this::doResetTrustStore);
    }

    protected void doResetTrustStore() {
        doReloadTrustStore();
    }

    @Override
    public void resetSecurityProfiles() {
        LOG.info("Resetting security profiles on domain [{}]", domain);
        init();
    }

    @Override
    public void replaceTrustStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        executeWithLock(() ->
                doReplaceTrustStore(storeContent, storeFileName, storePassword)
        );
    }

    private void doReplaceTrustStore(byte[] storeContent, String storeFileName, String storePassword) {
        replaceStore(storeContent, storeFileName, storePassword, DOMIBUS_TRUSTSTORE_NAME,
                keystorePersistenceService::getTrustStorePersistenceInfo, this::doReloadTrustStore, this::getTrustStore, this::validateTrustStoreCertificateTypes);
    }

    @Override
    public void replaceKeyStore(byte[] storeContent, String storeFileName, String storePassword) throws CryptoSpiException {
        executeWithLock(() ->
                doReplaceKeyStore(storeContent, storeFileName, storePassword)
        );
    }

    private void doReplaceKeyStore(byte[] storeContent, String storeFileName, String storePassword) {
        replaceStore(storeContent, storeFileName, storePassword, DOMIBUS_KEYSTORE_NAME,
                keystorePersistenceService::getKeyStorePersistenceInfo, this::doReloadKeyStore, this::getKeyStore, this::validateKeyStoreCertificateTypes);
    }

    @Override
    public void replaceTrustStore(String storeFileLocation, String storePassword) throws CryptoSpiException {
        executeWithLock(() -> {
            Path path = Paths.get(storeFileLocation);
            String storeName = path.getFileName().toString();
            byte[] storeContent = getContentFromFile(storeFileLocation);
            doReplaceTrustStore(storeContent, storeName, storePassword);
        });
    }

    @Override
    public void replaceKeyStore(String storeFileLocation, String storePassword) {
        executeWithLock(() -> {
            Path path = Paths.get(storeFileLocation);
            String storeName = path.getFileName().toString();
            byte[] storeContent = getContentFromFile(storeFileLocation);
            doReplaceKeyStore(storeContent, storeName, storePassword);
        });
    }

    @Override
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException {
        LOG.debug("Checking certificate validation for [{}]", alias);
        KeyStore trustStore = getTrustStore();
        return certificateService.isCertificateChainValid(trustStore, alias);
    }

    @Override
    public boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        List<CertificateEntry> certificates = Collections.singletonList(new CertificateEntry(alias, certificate));
        return addCertificates(overwrite, certificates);
    }

    @Override
    public void addCertificate(List<CertificateEntrySpi> certs, boolean overwrite) {
        List<CertificateEntry> certificates = certs.stream().map(el -> new CertificateEntry(el.getAlias(), el.getCertificate()))
                .collect(Collectors.toList());
        addCertificates(overwrite, certificates);
    }

    @Override
    public boolean removeCertificate(String alias) {
        List<String> aliases = Collections.singletonList(alias);
        return removeCertificates(aliases);
    }

    @Override
    public void removeCertificate(List<String> aliases) {
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
        return executeWithLock(() -> {
            boolean added = certificateService.addCertificates(keystorePersistenceService.getTrustStorePersistenceInfo(), certificates, overwrite);
            if (added) {
                doResetTrustStore();
            }
            return added;
        });
    }

    private byte[] getContentFromFile(String location) {
        try {
            return fileServiceUtil.getContentFromFile(location);
        } catch (IOException e) {
            throw new DomibusCertificateException("Could not read store from [" + location + "]");
        }
    }

    protected boolean removeCertificates(List<String> aliases) {
        return executeWithLock(() -> {
            boolean removed = certificateService.removeCertificates(keystorePersistenceService.getTrustStorePersistenceInfo(), aliases);
            if (removed) {
                doResetTrustStore();
            }
            return removed;
        });
    }

    protected void replaceStore(byte[] storeContent, String storeFileName, String storePassword,
                                String storeName, Supplier<KeystorePersistenceInfo> persistenceInfoGetter,
                                Runnable storeReloader, Supplier<KeyStore> storeGetter,
                                Consumer<KeyStore> certificateTypeValidator) throws CryptoSpiException {

        KeyStoreContentInfo storeContentInfo = certificateHelper.createStoreContentInfo(storeName, storeFileName, storeContent, storePassword);
        KeyStore newStore = certificateService.loadStore(storeContentInfo);
        try {
            KeyStore currentStore = storeGetter.get();
            if (securityUtil.areKeystoresIdentical(newStore, currentStore)) {
                throw new SameResourceCryptoSpiException(storeName, storeFileName,
                        String.format("Current store [%s] was not replaced with the content of the file [%s] because they are identical.", storeName, storeFileName));
            }
            LOG.info("Preparing to replace the current store [{}] having entries [{}] with entries [{}].",
                    storeName, certificateService.getStoreEntries(currentStore), certificateService.getStoreEntries(newStore));
        } catch (SameResourceCryptoSpiException sre) {
            throw sre;
        } catch (Exception ex) {
            LOG.warn("Could not retrieve the disk store, so no comparing them.", ex);
            LOG.info("Setting the store [{}] with entries [{}].", storeName, certificateService.getStoreEntries(newStore));
        }

        try {
            certificateTypeValidator.accept(newStore);

            KeystorePersistenceInfo persistenceInfo = persistenceInfoGetter.get();
            certificateService.replaceStore(storeContentInfo, persistenceInfo, false);
        } catch (CryptoException ex) {
            throw new CryptoSpiException(String.format("Error while replacing the store [%s] with content of the file named [%s].", storeName, storeFileName), ex);
        }

        LOG.debug("Store [{}] successfully replaced with entries [{}].", storeName, certificateService.getStoreEntries(newStore));
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
        if (securityProfileValidatorService.isLegacySingleAliasKeystoreDefined(domain)) {
            return String.format("Both legacy single keystore alias [%s] and security profile alias [%s] for [%s] are defined for domain: [%s]",
                    aliasValue, profileConfiguration.getAlias(), aliasDescription, domain);
        }
        return String.format("Keystore alias [%s] for [%s] already used on domain [%s] for [%s]. All RSA and ECC aliases (decrypt, sign) must be different from each other.",
                aliasValue, aliasDescription, domain, profileConfiguration.getDescription());
    }

    protected void createSecurityProfileAliasConfigurations() {

        securityProfileAliasConfigurations.clear();

        //without Security Profiles
        boolean legacySingleAliasKeystore = securityProfileValidatorService.isLegacySingleAliasKeystoreDefined(domain);
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

    private void doReloadKeyStore() throws CryptoSpiException {
        reloadStore(keystorePersistenceService::getKeyStorePersistenceInfo, this::getKeyStore, this::loadKeyStoreProperties,
                (keyStore, securityProfileConfiguration) -> securityProfileConfiguration.getMerlin().setKeyStore(keyStore),
                signalService::signalKeyStoreUpdate, this::validateKeyStoreCertificateTypes);
    }

    private void doReloadTrustStore() throws CryptoSpiException {
        reloadStore(keystorePersistenceService::getTrustStorePersistenceInfo, this::getTrustStore, this::loadTrustStoreProperties,
                (keyStore, securityProfileConfiguration) -> securityProfileConfiguration.getMerlin().setTrustStore(keyStore),
                signalService::signalTrustStoreUpdate, this::validateTrustStoreCertificateTypes);
    }

    private void reloadStore(Supplier<KeystorePersistenceInfo> persistenceGetter,
                             Supplier<KeyStore> storeGetter,
                             Runnable storePropertiesLoader,
                             BiConsumer<KeyStore, SecurityProfileAliasConfiguration> storeSetter,
                             Consumer<Domain> signaller,
                             Consumer<KeyStore> certificateTypeValidator) throws CryptoSpiException {
        KeystorePersistenceInfo persistenceInfo = persistenceGetter.get();
        String storeLocation = persistenceInfo.getFileLocation();
        try {
            final KeyStore newStore = certificateService.getStore(persistenceInfo);
            String storeName = persistenceInfo.getName();
            certificateTypeValidator.accept(newStore);

            try {
                KeyStore currentStore = storeGetter.get();
                if (securityUtil.areKeystoresIdentical(currentStore, newStore)) {
                    LOG.info("[{}] on disk and in memory are identical, so no reloading.", storeName);
                    return;
                }

                LOG.info("Replacing the current [{}] with entries [{}] with the one from the file [{}] with entries [{}] on domain [{}].",
                        storeName, certificateService.getStoreEntries(currentStore), storeLocation, certificateService.getStoreEntries(newStore), domain);
            } catch (Exception ex) {
                LOG.warn("Could not retrieve the current store named [{}].", storeName, ex);
                LOG.info("Replacing the [{}] with the one from the file [{}] with entries [{}] on domain [{}].",
                        storeName, storeLocation, certificateService.getStoreEntries(newStore), domain);
            }
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

    private void executeWithLock(Runnable task) {
        try {
            synchronizationService.execute(task, DB_SYNC_LOCK_KEY, JAVA_CHANGE_LOCK);
        } catch (DomibusSynchronizationException ex) {
            Throwable cause = ExceptionUtils.getRootCause(ex);
            if (cause instanceof CryptoSpiException) {
                throw (CryptoSpiException) cause;
            }
            throw new CryptoSpiException(cause);
        }
    }

    private <R> R executeWithLock(Callable<R> task) {
        try {
            return synchronizationService.execute(task, DB_SYNC_LOCK_KEY, JAVA_CHANGE_LOCK);
        } catch (DomibusSynchronizationException ex) {
            Throwable cause = ExceptionUtils.getRootCause(ex);
            if (cause instanceof CryptoSpiException) {
                throw (CryptoSpiException) cause;
            }
            throw new CryptoSpiException(cause);
        }
    }
}
