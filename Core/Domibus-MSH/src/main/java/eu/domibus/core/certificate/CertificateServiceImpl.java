package eu.domibus.core.certificate;

import com.google.common.collect.Lists;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.crypto.NoKeyStoreContentInformationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.*;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.certificate.crl.CRLService;
import eu.domibus.core.certificate.crl.DomibusCRLException;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.SecurityUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CERTIFICATE_REVOCATION_OFFSET;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CRL_BY_CERT_CACHE_ENABLED;
import static eu.domibus.logging.DomibusMessageCode.SEC_CERTIFICATE_SOON_REVOKED;
import static eu.domibus.logging.DomibusMessageCode.SEC_DOMIBUS_CERTIFICATE_REVOKED;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 3.2
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateServiceImpl.class);

    public static final String REVOCATION_TRIGGER_OFFSET_PROPERTY = DOMIBUS_CERTIFICATE_REVOCATION_OFFSET;

    private final CRLService crlService;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final CertificateDao certificateDao;

    private final EventService eventService;

    private final CertificateHelper certificateHelper;

    private final KeystorePersistenceService keystorePersistenceService;

    protected final DomainTaskExecutor domainTaskExecutor;

    private final PasswordDecryptionService passwordDecryptionService;

    private final DomainContextProvider domainContextProvider;

    private final AlertConfigurationService alertConfigurationService;

    protected final AuditService auditService;

    private final SecurityUtilImpl securityUtil;

    public CertificateServiceImpl(CRLService crlService,
                                  DomibusPropertyProvider domibusPropertyProvider,
                                  CertificateDao certificateDao,
                                  EventService eventService,
                                  CertificateHelper certificateHelper,
                                  KeystorePersistenceService keystorePersistenceService,
                                  DomainTaskExecutor domainTaskExecutor,
                                  PasswordDecryptionService passwordDecryptionService,
                                  DomainContextProvider domainContextProvider,
                                  SecurityUtilImpl securityUtil,
                                  AlertConfigurationService alertConfigurationService,
                                  AuditService auditService) {
        this.crlService = crlService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateDao = certificateDao;
        this.eventService = eventService;
        this.certificateHelper = certificateHelper;
        this.keystorePersistenceService = keystorePersistenceService;
        this.domainTaskExecutor = domainTaskExecutor;

        this.passwordDecryptionService = passwordDecryptionService;
        this.domainContextProvider = domainContextProvider;
        this.alertConfigurationService = alertConfigurationService;
        this.auditService = auditService;
        this.securityUtil = securityUtil;
    }

    @Override
    public boolean isCertificateChainValid(List<? extends java.security.cert.Certificate> certificateChain) {
        for (java.security.cert.Certificate certificate : certificateChain) {
            boolean certificateValid = isCertificateValid((X509Certificate) certificate);
            if (!certificateValid) {
                LOG.warn("Sender certificate not valid [{}]", certificate);
                return false;
            }
            LOG.debug("Sender certificate valid [{}]", certificate);
        }
        return true;
    }

    @Override
    public boolean isCertificateChainValid(KeyStore keyStore, String alias) throws DomibusCertificateException {
        X509Certificate[] certificateChain = null;
        try {
            certificateChain = getCertificateChain(keyStore, alias);
        } catch (KeyStoreException e) {
            throw new DomibusCertificateException("Error getting the certificate chain from the store for [" + alias + "]", e);
        }
        if (certificateChain == null || certificateChain.length == 0 || certificateChain[0] == null) {
            throw new DomibusCertificateException("Could not find alias in the store [" + alias + "]");
        }

        for (X509Certificate certificate : certificateChain) {
            boolean certificateValid = isCertificateValid(certificate);
            if (!certificateValid) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException {
        boolean isValid = checkValidity(cert);
        if (!isValid) {
            LOG.warn("Certificate is not valid:[{}] ", cert);
            return false;
        }
        try {
            if(LOG.isDebugEnabled()) {
                Boolean useCrlByCertCache = domibusPropertyProvider.getBooleanProperty(DOMIBUS_CRL_BY_CERT_CACHE_ENABLED);
                LOG.debug("CRL by certificate cache is [{}]", useCrlByCertCache ? "enabled" : "disabled");
            }
            return !crlService.isCertificateRevoked(cert);
        } catch (Exception e) {
            throw new DomibusCertificateException(e);
        }
    }

    @Override
    public String extractCommonName(final X509Certificate certificate) throws InvalidNameException {

        final String dn = certificate.getSubjectDN().getName();
        LOG.debug("DN is:[{}]", dn);
        final LdapName ln = new LdapName(dn);
        for (final Rdn rdn : ln.getRdns()) {
            if (StringUtils.equalsIgnoreCase(rdn.getType(), "CN")) {
                LOG.debug("CN is: " + rdn.getValue());
                return rdn.getValue().toString();
            }
        }
        throw new IllegalArgumentException("The certificate does not contain a common name (CN): " + certificate.getSubjectDN().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCertificateAndLogRevocation(final KeyStore trustStore, final KeyStore keyStore) {
        saveCertificateData(trustStore, keyStore);
        logCertificateRevocationWarning();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendCertificateAlerts() {
        sendCertificateImminentExpirationAlerts();
        sendCertificateExpiredAlerts();
    }

    @Override
    public X509Certificate loadCertificate(String content) {
        if (StringUtils.isEmpty(content)) {
            throw new DomibusCertificateException("Certificate content cannot be null.");
        }

        return loadCertificate(content.getBytes(StandardCharsets.UTF_8), isPemFormat(content));
    }

    @Override
    public X509Certificate loadCertificate(byte[] content) {
        return loadCertificate(content, true);
    }

    protected X509Certificate loadCertificate(byte[] content, boolean isPemFormat) {
        if (ArrayUtils.isEmpty(content)) {
            throw new DomibusCertificateException("Certificate content cannot be null.");
        }

        CertificateFactory certFactory;
        X509Certificate cert;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new DomibusCertificateException("Could not initialize certificate factory", e);
        }

        try (InputStream contentStream = new ByteArrayInputStream(content)) {
            InputStream resultStream = contentStream;
            if (!isPemFormat) {
                resultStream = Base64.getMimeDecoder().wrap(contentStream);
            }
            cert = (X509Certificate) certFactory.generateCertificate(resultStream);
        } catch (IOException | CertificateException e) {
            throw new DomibusCertificateException("Could not generate certificate", e);
        }
        return cert;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serializeCertificateChainIntoPemFormat(List<? extends java.security.cert.Certificate> certificates) {
        StringWriter sw = new StringWriter();
        for (java.security.cert.Certificate certificate : certificates) {
            try (PemWriter pw = new PemWriter(sw)) {
                PemObjectGenerator gen = new JcaMiscPEMGenerator(certificate);
                pw.writeObject(gen);
            } catch (IOException e) {
                throw new DomibusCertificateException(String.format("Error while serializing certificates:[%s]", certificate.getType()), e);
            }
        }
        final String certificateChainValue = sw.toString();
        LOG.debug("Serialized certificates:[{}]", certificateChainValue);
        return certificateChainValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public X509Certificate[] getCertificatesWithProvider(X509Certificate[] certificates, String provider) {
        List<java.security.cert.Certificate> serCerts = Arrays.asList(certificates);
        LOG.debug("Reloading certificates with [{}]", provider);
        return deserializeCertificateChainFromPemFormat(serializeCertificateChainIntoPemFormat(serCerts), provider).toArray(new X509Certificate[]{});
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<X509Certificate> deserializeCertificateChainFromPemFormat(String chain, String provider) {
        List<X509Certificate> certificates = new ArrayList<>();
        try (PemReader reader = new PemReader(new StringReader(chain))) {
            CertificateFactory cf;
            PemObject pemObject;
            if (provider == null) {
                LOG.debug("Loading Certificate factory with default provider");
                cf = CertificateFactory.getInstance("X509");
            } else {
                LOG.debug("Loading Certificate factory with provider:[{}]", provider);
                cf = CertificateFactory.getInstance("X509", provider);
            }
            while ((pemObject = reader.readPemObject()) != null) {
                if (pemObject.getType().equals("CERTIFICATE")) {
                    java.security.cert.Certificate c = cf.generateCertificate(new ByteArrayInputStream(pemObject.getContent()));
                    final X509Certificate certificate = (X509Certificate) c;
                    LOG.debug("Deserialized certificate:[{}]", certificate.getSubjectDN());
                    certificates.add(certificate);
                } else {
                    throw new DomibusCertificateException("Unknown type " + pemObject.getType());
                }
            }

        } catch (IOException | CertificateException | NoSuchProviderException e) {
            throw new DomibusCertificateException("Error while instantiating certificates from pem", e);
        }
        return certificates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.security.cert.Certificate extractLeafCertificateFromChain(List<? extends java.security.cert.Certificate> certificates) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Extracting leaf certificate from chain");
            for (java.security.cert.Certificate certificate : certificates) {
                LOG.trace("Certificate:[{}]", certificate);
            }
        }
        Set<String> issuerSet = new HashSet<>();
        Map<String, X509Certificate> subjectMap = new HashMap<>();
        for (java.security.cert.Certificate certificate : certificates) {
            X509Certificate x509Certificate = (X509Certificate) certificate;
            final String subjectName = x509Certificate.getSubjectDN().getName();
            subjectMap.put(subjectName, x509Certificate);
            final String issuerName = x509Certificate.getIssuerDN().getName();
            issuerSet.add(issuerName);
            LOG.debug("Certificate subject:[{}] issuer:[{}]", subjectName, issuerName);
        }

        final Set<String> allSubject = subjectMap.keySet();
        //There should always be one more subject more than issuers. Indeed the root CA has the same value as issuer and subject.
        allSubject.removeAll(issuerSet);
        //the unique entry in the set is the leaf.
        if (allSubject.size() == 1) {
            final String leafSubjet = allSubject.iterator().next();
            LOG.debug("Not an issuer:[{}]", leafSubjet);
            return subjectMap.get(leafSubjet);
        }
        if (certificates.size() == 1) {
            LOG.trace("In case of unique self-signed certificate, the issuer and the subject are the same: returning it.");
            return certificates.get(0);
        }
        LOG.error("Certificate exchange type is X_509_PKIPATHV_1 but no leaf certificate has been found");
        return null;
    }

    @Override
    public TrustStoreEntry convertCertificateContent(String certificateContent) {
        X509Certificate cert = loadCertificate(certificateContent);
        return createTrustStoreEntry(null, cert);
    }

    @Override
    public TrustStoreEntry createTrustStoreEntry(X509Certificate cert, String alias) {
        LOG.debug("Create TrustStore Entry for [{}] = [{}] ", alias, cert);
        return createTrustStoreEntry(alias, cert);
    }

    public boolean replaceStore(KeyStoreContentInfo storeInfo, KeystorePersistenceInfo persistenceInfo, boolean checkEqual) {
        if (StringUtils.isEmpty(storeInfo.getType())) {
            storeInfo.setType(certificateHelper.getStoreType(storeInfo.getFileName()));
        }
        String storeName = persistenceInfo.getName();
        try {
            KeyStore uploadedStore = loadStore(storeInfo);
            if (checkEqual && storesAreEqual(persistenceInfo, storeName, uploadedStore)) {
                return false;
            }
            if (sameProperties(storeInfo, persistenceInfo)) {
                // same props, so just save the store on disk
                keystorePersistenceService.saveStore(storeInfo, persistenceInfo);
            } else {
                // we need to copy the certificates to a store with the same props as the ones on disk store
                KeyStore destStore = getNewKeystore(persistenceInfo.getType());
                copyStoreCertificates(uploadedStore, destStore);
                keystorePersistenceService.saveStore(destStore, persistenceInfo);
            }
            LOG.info("Store [{}] successfully replaced with entries [{}].", storeName, getStoreEntries(uploadedStore));

            auditService.addStoreReplacedAudit(storeName);
            return true;
        } catch (Exception exc) {
            throw new CryptoException("Could not replace store " + storeName, exc);
        }
    }

    @Override
    public KeyStore getStore(KeystorePersistenceInfo keystorePersistenceInfo) {
        KeyStoreContentInfo keyStoreInfo = getStoreContent(keystorePersistenceInfo);
        return loadStore(keyStoreInfo);
    }

    @Override
    public List<TrustStoreEntry> getStoreEntries(KeystorePersistenceInfo keystorePersistenceInfo) {
        KeyStoreContentInfo storeInfo = keystorePersistenceService.loadStore(keystorePersistenceInfo);
        KeyStore store = loadStore(storeInfo);

        return getStoreEntries(store);
    }

    @Override
    public boolean addCertificate(KeystorePersistenceInfo persistenceInfo, byte[] certificateContent, String alias, boolean overwrite) {
        X509Certificate certificate = loadCertificate(certificateContent);
        List<CertificateEntry> certificates = Arrays.asList(new CertificateEntry(alias, certificate));

        return doAddCertificates(persistenceInfo, certificates, overwrite);
    }

    @Override
    public boolean addCertificates(KeystorePersistenceInfo persistenceInfo, List<CertificateEntry> certificates, boolean overwrite) {
        return doAddCertificates(persistenceInfo, certificates, overwrite);
    }

    @Override
    public boolean removeCertificate(KeystorePersistenceInfo persistenceInfo, String alias) {
        List<String> aliases = Arrays.asList(alias);
        return doRemoveCertificates(persistenceInfo, aliases);
    }

    @Override
    public boolean removeCertificates(KeystorePersistenceInfo persistenceInfo, List<String> aliases) {
        return doRemoveCertificates(persistenceInfo, aliases);
    }

    @Override
    public KeyStoreContentInfo getStoreContent(KeystorePersistenceInfo keystorePersistenceInfo) {
        return keystorePersistenceService.loadStore(keystorePersistenceInfo);
    }

    @Override
    public KeyStoreContentInfo getStoreContent(KeyStore store, String storeName, String password) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            String decryptedPassword = decrypt(storeName, password);
            store.store(byteStream, decryptedPassword.toCharArray());
            byte[] content = byteStream.toByteArray();

            return certificateHelper.createStoreContentInfo(storeName, content, store.getType(), decryptedPassword);
        } catch (Exception e) {
            throw new CryptoException("Could not get content of store:" + storeName, e);
        }
    }

    @Override
    public List<TrustStoreEntry> getStoreEntries(final KeyStore store) {
        try {
            List<TrustStoreEntry> storeEntries = new ArrayList<>();
            final Enumeration<String> aliases = store.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                final X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
                TrustStoreEntry storeEntry = createTrustStoreEntry(alias, certificate);
                if (storeEntry != null) {
                    Integer certificateExpiryAlertDays = domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
                    storeEntry.setCertificateExpiryAlertDays(certificateExpiryAlertDays);
                    storeEntries.add(storeEntry);
                } else {
                    LOG.debug("The alias:[{}] does not exist or does not contain a certificate.", alias);
                }
            }
            return storeEntries;
        } catch (KeyStoreException ex) {
            LOG.warn("Could not extract entries from the store", ex);
            return Lists.newArrayList();
        }
    }

    @Override
    public void saveStoresFromDBToDisk(KeystorePersistenceInfo keystorePersistenceInfo, List<Domain> domains) {
        String name = keystorePersistenceInfo.getName();
        LOG.debug("Persisting the store [{}] for all domains if not yet exists.", name);
        for (Domain domain : domains) {
            try {
                domainTaskExecutor.submit(() -> keystorePersistenceService.saveStoreFromDBToDisk(keystorePersistenceInfo), domain);
            } catch (DomibusCertificateException dce) {
                LOG.warn("The store [{}] for domain [{}] could not be persisted!", name, domain, dce);
            }
        }
        LOG.debug("Finished persisting the store [{}] for all domains.", name);
    }

    @Override
    public boolean isStoreChangedOnDisk(KeyStore store, KeystorePersistenceInfo persistenceInfo) {
        String storeName = persistenceInfo.getName();

        KeyStore storeOnDisk = getStore(persistenceInfo);

        boolean different = !securityUtil.areKeystoresIdentical(store, storeOnDisk);
        if (different) {
            LOG.info("The store [{}] on disk has different content than the persisted one.", storeName);
        } else {
            LOG.debug("The store [{}] on disk has the same content as the persisted one.", storeName);
        }
        return different;
    }

    @Override
    public KeyStore loadStore(KeyStoreContentInfo storeInfo) {
        if (storeInfo == null) {
            throw new NoKeyStoreContentInformationException("Could not load a null store");
        }

        try (InputStream contentStream = new ByteArrayInputStream(storeInfo.getContent())) {
            KeyStore keystore = getNewKeystore(storeInfo.getType());
            LOG.debug("Creating a new store [{}] to load content", storeInfo);
            keystore.load(contentStream, storeInfo.getPassword().toCharArray());
            return keystore;
        } catch (Exception ex) {
            throw new CryptoException("Could not load store named " + storeInfo.getName(), ex);
        }
    }

    private boolean storesAreEqual(KeystorePersistenceInfo persistenceInfo, String storeName, KeyStore uploadedStore) {
        try {
            KeyStore diskStore = getStore(persistenceInfo);
            if (securityUtil.areKeystoresIdentical(uploadedStore, diskStore)) {
                LOG.debug("Current store [{}] with entries [{}] is identical with the new one, so no replacing.", storeName, getStoreEntries(diskStore));
                return true;
            }
            LOG.info("Preparing to replace the current store [{}] having entries [{}].", storeName, getStoreEntries(diskStore));
            return false;
        } catch (Exception ex) {
            LOG.warn("Could not check if store [{}] on disk is identical to the uploaded one; replacing anyway.", storeName, ex);
            return false;
        }
    }

    private boolean sameProperties(KeyStoreContentInfo storeInfo, KeystorePersistenceInfo persistenceInfo) {
        return StringUtils.equalsIgnoreCase(storeInfo.getType(), persistenceInfo.getType())
                && StringUtils.equalsIgnoreCase(storeInfo.getPassword(), persistenceInfo.getPassword());
    }

    public KeyStore getNewKeystore(String storeType) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore instance = KeyStore.getInstance(storeType);
        instance.load(null, null);
        return instance;
    }

    protected void copyStoreCertificates(KeyStore srcStore, KeyStore destStore) {
        try {
            final Enumeration<String> aliases = srcStore.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                final X509Certificate certificate = (X509Certificate) srcStore.getCertificate(alias);
                destStore.setCertificateEntry(alias, certificate);
                LOG.debug("Copy certificate [{}] named [{}]", certificate, alias);
            }
        } catch (Exception e) {
            throw new DomibusCertificateException("Error while copying certificates from source store", e);
        }
    }

    protected boolean doAddCertificates(KeystorePersistenceInfo persistenceInfo, List<CertificateEntry> certificates, boolean overwrite) {
        LOG.info("Adding certificates [{}] to [{}]", certificates.stream().map(certificateEntry -> certificateEntry.getAlias()).collect(Collectors.toList()), persistenceInfo.getFileLocation());
        KeyStore store = getStore(persistenceInfo);

        int addedNr = 0;
        for (CertificateEntry certificateEntry : certificates) {
            boolean added = doAddCertificate(store, certificateEntry.getCertificate(), certificateEntry.getAlias(), overwrite);
            if (added) {
                addedNr++;
            }
        }
        if (addedNr > 0) {
            LOG.debug("Added [{}] certificates so persisting the store.", addedNr);
            keystorePersistenceService.saveStore(store, persistenceInfo);
            auditService.addCertificateAddedAudit(persistenceInfo.getName());
            return true;
        }
        LOG.trace("Added 0 certificates so exiting without persisting the store.");
        return false;
    }

    protected boolean doRemoveCertificates(KeystorePersistenceInfo persistenceInfo, List<String> aliases) {
        LOG.info("Removing certificates with aliases [{}] from [{}]", aliases, persistenceInfo.getFileLocation());
        KeyStore store = getStore(persistenceInfo);

        int removedNr = 0;
        for (String alias : aliases) {
            boolean removed = doRemoveCertificate(store, alias);
            if (removed) {
                removedNr++;
            }
        }
        if (removedNr > 0) {
            LOG.debug("Removed [{}] certificates so persisting the store.", removedNr);
            keystorePersistenceService.saveStore(store, persistenceInfo);
            auditService.addCertificateRemovedAudit(persistenceInfo.getName());
            return true;
        }
        LOG.trace("Removed 0 certificates so exiting without persisting the store.");
        return false;
    }

    protected boolean doAddCertificate(KeyStore keystore, X509Certificate certificate, String alias, boolean overwrite) {
        boolean containsAlias;
        try {
            containsAlias = keystore.containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the store. This should never happen", e);
        }
        if (containsAlias && !overwrite) {
            LOG.info("The store already contains alias [{}] and the overwrite is false so no adding.", alias);
            return false;
        }
        if (certificateHelper.containsAndIdentical(keystore, alias, certificate)) {
            LOG.info("The store already contains alias [{}] and it is identical so no adding.", alias);
            return false;
        }
        try {
            if (containsAlias) {
                keystore.deleteEntry(alias);
                LOG.info("Deleted certificate with alias [{}]", alias);
            }
            keystore.setCertificateEntry(alias, certificate);
            LOG.info("Added certificate with alias [{}]: [{}]", alias, certificate);
            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    protected boolean doRemoveCertificate(KeyStore keystore, String alias) {
        boolean containsAlias;
        try {
            containsAlias = keystore.containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the store. This should never happen", e);
        }
        if (!containsAlias) {
            LOG.info("The store does not contain alias [{}] so no removing.", alias);
            return false;
        }
        try {
            keystore.deleteEntry(alias);
            LOG.info("Removed certificate with alias [{}]", alias);
            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Create or update all keystore certificates in the db.
     *
     * @param trustStore the trust store
     * @param keyStore   the key store
     */
    protected void saveCertificateData(KeyStore trustStore, KeyStore keyStore) {
        List<eu.domibus.core.certificate.Certificate> certificates = groupAllKeystoreCertificates(trustStore, keyStore);
        certificateDao.removeUnusedCertificates(certificates);
        for (eu.domibus.core.certificate.Certificate certificate : certificates) {
            certificateDao.saveOrUpdate(certificate);
        }
    }

    /**
     * Load expired and soon expired certificates, add a warning of error log, and save a flag saying the system already
     * notified it for the day.
     */
    protected void logCertificateRevocationWarning() {
        List<eu.domibus.core.certificate.Certificate> unNotifiedSoonRevoked = certificateDao.getUnNotifiedSoonRevoked();
        for (eu.domibus.core.certificate.Certificate certificate : unNotifiedSoonRevoked) {
            LOG.securityWarn(SEC_CERTIFICATE_SOON_REVOKED, certificate.getCertificateType(), certificate.getAlias(), certificate.getNotAfter());
            certificateDao.updateRevocation(certificate);
        }

        List<eu.domibus.core.certificate.Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedRevoked();
        for (eu.domibus.core.certificate.Certificate certificate : unNotifiedRevoked) {
            LOG.securityError(SEC_DOMIBUS_CERTIFICATE_REVOKED, certificate.getCertificateType(), certificate.getAlias(), certificate.getNotAfter());
            certificateDao.updateRevocation(certificate);
        }
    }

    /**
     * Group keystore and trustStore certificates in a list.
     *
     * @param trustStore the trust store
     * @param keyStore   the key store
     * @return a list of certificate.
     */
    protected List<eu.domibus.core.certificate.Certificate> groupAllKeystoreCertificates(KeyStore trustStore, KeyStore keyStore) {
        List<eu.domibus.core.certificate.Certificate> allCertificates = new ArrayList<>();
        allCertificates.addAll(loadAndEnrichCertificateFromKeystore(trustStore, CertificateType.PUBLIC));
        allCertificates.addAll(loadAndEnrichCertificateFromKeystore(keyStore, CertificateType.PRIVATE));
        return Collections.unmodifiableList(allCertificates);
    }

    /**
     * Load certificate from a keystore and enrich them with status and type.
     *
     * @param keyStore        the store where to retrieve the certificates.
     * @param certificateType the type of the certificate (Public/Private)
     * @return the list of certificates.
     */
    private List<eu.domibus.core.certificate.Certificate> loadAndEnrichCertificateFromKeystore(KeyStore keyStore, CertificateType certificateType) {
        List<eu.domibus.core.certificate.Certificate> certificates = new ArrayList<>();
        if (keyStore != null) {
            certificates = extractCertificateFromKeyStore(keyStore);
            for (eu.domibus.core.certificate.Certificate certificate : certificates) {
                certificate.setCertificateType(certificateType);
                CertificateStatus certificateStatus = getCertificateStatus(certificate.getNotAfter());
                certificate.setCertificateStatus(certificateStatus);
            }
        }
        return certificates;
    }

    /**
     * Process the certificate status base on its expiration date.
     *
     * @param notAfter the expiration date of the certificate.
     * @return the certificate status.
     */
    protected CertificateStatus getCertificateStatus(Date notAfter) {
        int revocationOffsetInDays = domibusPropertyProvider.getIntegerProperty(REVOCATION_TRIGGER_OFFSET_PROPERTY);
        LOG.debug("Property [{}], value [{}]", REVOCATION_TRIGGER_OFFSET_PROPERTY, revocationOffsetInDays);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime offsetDate = now.plusDays(revocationOffsetInDays);
        LocalDateTime certificateEnd = LocalDateTime.ofInstant(notAfter.toInstant(), ZoneOffset.UTC);

        LOG.debug("Current date[{}], offset date[{}], certificate end date:[{}]", now, offsetDate, certificateEnd);
        if (now.isAfter(certificateEnd)) {
            return CertificateStatus.REVOKED;
        } else if (offsetDate.isAfter(certificateEnd)) {
            return CertificateStatus.SOON_REVOKED;
        }
        return CertificateStatus.OK;
    }

    protected List<eu.domibus.core.certificate.Certificate> extractCertificateFromKeyStore(KeyStore trustStore) {
        List<eu.domibus.core.certificate.Certificate> certificates = new ArrayList<>();
        try {
            final Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                final X509Certificate x509Certificate = (X509Certificate) trustStore.getCertificate(alias);
                eu.domibus.core.certificate.Certificate certificate = new eu.domibus.core.certificate.Certificate();
                certificate.setAlias(alias);
                certificate.setNotAfter(x509Certificate.getNotAfter());
                certificate.setNotBefore(x509Certificate.getNotBefore());
                certificates.add(certificate);
            }
        } catch (KeyStoreException e) {
            LOG.warn(e.getMessage(), e);
        }
        return Collections.unmodifiableList(certificates);
    }

    protected void sendCertificateExpiredAlerts() {
        RepetitiveAlertConfiguration configuration = (RepetitiveAlertConfiguration) alertConfigurationService.getConfiguration(AlertType.CERT_EXPIRED);
        final boolean activeModule = configuration.isActive();
        LOG.debug("Certificate expired alert module activated:[{}]", activeModule);
        if (!activeModule) {
            LOG.info("Certificate Expired Module is not active; returning.");
            return;
        }
        final Integer revokedDuration = configuration.getDelay();
        final Integer revokedFrequency = configuration.getFrequency();
        Date endNotification = Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusDays(revokedDuration).toInstant());
        Date notificationDate = Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusDays(revokedFrequency).toInstant());

        LOG.debug("Searching for expired certificate with notification date smaller than:[{}] and expiration date > current date - offset[{}]->[{}]", notificationDate, revokedDuration, endNotification);
        certificateDao.findExpiredToNotifyAsAlert(notificationDate, endNotification).forEach(certificate -> {
            certificate.setAlertExpiredNotificationDate(Date.from(ZonedDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()));
            certificateDao.saveOrUpdate(certificate);
            final String alias = certificate.getAlias();
            eventService.enqueueCertificateExpiredEvent(alias, certificate.getNotAfter());
        });
    }

    protected boolean checkValidity(X509Certificate cert) {
        boolean result = false;
        try {
            cert.checkValidity();
            result = true;
        } catch (Exception e) {
            LOG.warn("Certificate is not valid " + cert, e);
        }

        return result;
    }

    /**
     * Send alerts for certificate imminent expiration. It does not take into account certificates which were dynamically discovered. The dynamically discovered certificates are updated periodically from SMP and it doesn't make sense to send alerts for them
     */
    protected void sendCertificateImminentExpirationAlerts() {
        RepetitiveAlertConfiguration configuration = (RepetitiveAlertConfiguration) alertConfigurationService.getConfiguration(AlertType.CERT_IMMINENT_EXPIRATION);
        final Boolean activeModule = configuration.isActive();
        LOG.debug("Certificate Imminent expiration alert module activated:[{}]", activeModule);
        if (BooleanUtils.isNotTrue(activeModule)) {
            LOG.info("Imminent Expiration Certificate Module is not active; returning.");
            return;
        }
        final Integer imminentExpirationDelay = configuration.getDelay();
        final Integer imminentExpirationFrequency = configuration.getFrequency();

        final Date today = Date.from(ZonedDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant());
        final Date maxDate = Date.from(ZonedDateTime.now(ZoneOffset.UTC).plusDays(imminentExpirationDelay).toInstant());
        final Date notificationDate = Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusDays(imminentExpirationFrequency).toInstant());

        LOG.debug("Searching for certificate about to expire with notification date smaller than:[{}] and expiration date between current date and current date + offset[{}]->[{}]. Dynamically discovered certificates are not taken into account",
                notificationDate, imminentExpirationDelay, maxDate);
        certificateDao.findImminentExpirationToNotifyAsAlert(notificationDate, today, maxDate).forEach(certificate -> {
            certificate.setAlertImminentNotificationDate(today);
            certificateDao.saveOrUpdate(certificate);

            final String alias = certificate.getAlias();
            eventService.enqueueImminentCertificateExpirationEvent(alias, certificate.getNotAfter());
        });
    }

    protected X509Certificate[] getCertificateChain(KeyStore trustStore, String alias) throws KeyStoreException {
        //TODO get the certificate chain manually based on the issued by info from the original certificate
        final java.security.cert.Certificate[] certificateChain = trustStore.getCertificateChain(alias);
        if (certificateChain == null) {
            X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
            return new X509Certificate[]{certificate};
        }
        return Arrays.copyOf(certificateChain, certificateChain.length, X509Certificate[].class);

    }

    protected boolean isPemFormat(String content) {
        return StringUtils.startsWith(StringUtils.trim(content), "-----BEGIN CERTIFICATE-----");
    }

    private TrustStoreEntry createTrustStoreEntry(String alias, final X509Certificate certificate) {
        if (certificate == null)
            return null;
        TrustStoreEntry entry = new TrustStoreEntry(
                alias,
                certificate.getSubjectDN().getName(),
                certificate.getIssuerDN().getName(),
                certificate.getNotBefore(),
                certificate.getNotAfter());
        entry.setFingerprints(extractFingerprints(certificate));
        return entry;
    }

    @Override
    public String extractFingerprints(final X509Certificate certificate) {
        if (certificate == null) {
            LOG.debug("No fingerprint to extract because no certificate provided");
            return null;
        }

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new DomibusCertificateException("Could not initialize MessageDigest", e);
        }
        byte[] der;
        try {
            der = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new DomibusCertificateException("Could not encode certificate", e);
        }
        md.update(der);
        byte[] digest = md.digest();
        String digestHex = DatatypeConverter.printHexBinary(digest);
        return digestHex.toLowerCase();
    }

    /**
     * Extracts all Certificate Policy identifiers the "Certificate policy" extension of X.509.
     * If the certificate policy extension is unavailable, returns an empty list.
     *
     * @param cert a X509 certificate
     * @return the list of CRL urls of certificate policy identifiers
     */
    @Override
    public List<String> getCertificatePolicyIdentifiers(X509Certificate cert) {

        byte[] certPolicyExt = cert.getExtensionValue(Extension.certificatePolicies.getId());
        if (certPolicyExt == null) {
            return new ArrayList<>();
        }

        CertificatePolicies policies;
        try {
            policies = CertificatePolicies.getInstance(JcaX509ExtensionUtils.parseExtensionValue(certPolicyExt));
        } catch (IOException e) {
            throw new DomibusCRLException("Error occurred while reading certificate policy object!", e);
        }

        return Arrays.stream(policies.getPolicyInformation())
                .map(PolicyInformation::getPolicyIdentifier)
                .map(ASN1ObjectIdentifier::getId)
                .map(StringUtils::trim)
                .collect(Collectors.toList());
    }

    private String decrypt(String trustName, String password) {
        return passwordDecryptionService.decryptPropertyIfEncrypted(domainContextProvider.getCurrentDomainSafely(),
                trustName + ".password", password);
    }
}


