package eu.domibus.core.certificate;

import com.google.common.collect.Lists;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
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
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.SecurityUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CERTIFICATE_REVOCATION_OFFSET;
import static eu.domibus.logging.DomibusMessageCode.SEC_CERTIFICATE_REVOKED;
import static eu.domibus.logging.DomibusMessageCode.SEC_CERTIFICATE_SOON_REVOKED;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 3.2
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateServiceImpl.class);

    public static final String REVOCATION_TRIGGER_OFFSET_PROPERTY = DOMIBUS_CERTIFICATE_REVOCATION_OFFSET;
    private static final DateTimeFormatter BACKUP_SUFFIX_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final CRLService crlService;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final CertificateDao certificateDao;

    private final EventService eventService;

    private final CertificateHelper certificateHelper;

    private final KeystorePersistenceService keystorePersistenceService;

    protected final DomainService domainService;

    protected final DomainTaskExecutor domainTaskExecutor;

    protected final TruststoreDao truststoreDao;

    private final PasswordDecryptionService passwordDecryptionService;

//    private final PasswordEncryptionService passwordEncryptionService;

    private final DomainContextProvider domainContextProvider;

//    private final DomibusCoreMapper coreMapper;

    private final AlertConfigurationService alertConfigurationService;

    protected final AuditService auditService;

    private final SecurityUtilImpl securityUtilImpl;

    public CertificateServiceImpl(CRLService crlService,
                                  DomibusPropertyProvider domibusPropertyProvider,
                                  CertificateDao certificateDao,
                                  EventService eventService,
                                  CertificateHelper certificateHelper,
                                  KeystorePersistenceService keystorePersistenceService, DomainService domainService,
                                  DomainTaskExecutor domainTaskExecutor,
                                  TruststoreDao truststoreDao,
                                  PasswordDecryptionService passwordDecryptionService,
//                                  PasswordEncryptionService passwordEncryptionService,
                                  DomainContextProvider domainContextProvider,
//                                  DomibusCoreMapper coreMapper,
                                  AlertConfigurationService alertConfigurationService, AuditService auditService, SecurityUtilImpl securityUtilImpl) {
        this.crlService = crlService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateDao = certificateDao;
        this.eventService = eventService;
        this.certificateHelper = certificateHelper;
        this.keystorePersistenceService = keystorePersistenceService;
        this.domainService = domainService;
        this.domainTaskExecutor = domainTaskExecutor;
        this.truststoreDao = truststoreDao;
        this.passwordDecryptionService = passwordDecryptionService;
//        this.passwordEncryptionService = passwordEncryptionService;
        this.domainContextProvider = domainContextProvider;
//        this.coreMapper = coreMapper;
        this.alertConfigurationService = alertConfigurationService;
        this.auditService = auditService;
        this.securityUtilImpl = securityUtilImpl;
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

    // todo reuse code between these 2 methods
    @Override
    public X509Certificate loadCertificateFromString(String content) {
        if (content == null) {
            throw new DomibusCertificateException("Certificate content cannot be null.");
        }

        CertificateFactory certFactory;
        X509Certificate cert;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new DomibusCertificateException("Could not initialize certificate factory", e);
        }

        try (InputStream contentStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            InputStream resultStream = contentStream;
            if (!isPemFormat(content)) {
                resultStream = Base64.getMimeDecoder().wrap(resultStream);
            }
            cert = (X509Certificate) certFactory.generateCertificate(resultStream);
        } catch (IOException | CertificateException e) {
            throw new DomibusCertificateException("Could not generate certificate", e);
        }
        return cert;
    }

    @Override
    public X509Certificate loadCertificateFromByteArray(byte[] content) {
        if (content == null) {
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
            cert = (X509Certificate) certFactory.generateCertificate(contentStream);
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
        X509Certificate cert = loadCertificateFromString(certificateContent);
        return createTrustStoreEntry(null, cert);
    }

    @Override
    public TrustStoreEntry createTrustStoreEntry(X509Certificate cert, String alias) {
        LOG.debug("Create TrustStore Entry for [{}] = [{}] ", alias, cert);
        return createTrustStoreEntry(alias, cert);
    }

//    @Override
//    public void replaceStore(String fileLocation, String filePassword, String storeName) {
//        LOG.debug("Replacing store [{}] with the one from file [{}]", storeName, fileLocation);
//        Path path = Paths.get(fileLocation);
//        String fileName = path.getFileName().toString();
//        byte[] fileContent = getStoreContentFromFile(fileLocation);
//        replaceStore(fileName, fileContent, filePassword, storeName);
//    }

//    @Override
//    public void replaceStore(String fileName, byte[] fileContent, String filePassword, String storeName) {
//        LOG.debug("Replacing store [{}] with file content", storeName);
//        String storeType = certificateHelper.getStoreType(fileName);
//        return replaceStore(fileContent, filePassword, storeType, storeName);
//    }

//    @Override
//    public void replaceStore(String fileName, byte[] fileContent, String filePassword, KeystorePersistenceInfo persistenceInfo) throws CryptoException {
//        LOG.debug("Replacing store [{}] with file content", persistenceInfo.getName());
//        String storeType = certificateHelper.getStoreType(fileName);
//        replaceStore(fileContent, filePassword, storeType, persistenceInfo);
//    }

    @Override
    public void replaceStore(KeyStoreContentInfo storeInfo, KeystorePersistenceInfo persistenceInfo) {
        String storeName = persistenceInfo.getName();
        KeyStore store = getStore(persistenceInfo);

        LOG.debug("Store [{}] with entries [{}] will be replaced.", storeName, getStoreEntries(store));
        try {
            validateStoreContent(storeInfo);
            keystorePersistenceService.saveStore(storeInfo, persistenceInfo);
            LOG.info("Store [{}] successfully replaced with [{}].", storeName, getStoreEntries(store));

            auditService.addStoreReplacedAudit(storeName);
        } catch (CryptoException exc) {
            throw new CryptoException("Could not replace store " + storeName, exc);
        }
    }

//    @Override
//    public KeyStore getStore(String storeName) {
//        TruststoreEntity entity = getStoreEntity(storeName);
//        return loadStore(entity.getContent(), entity.getPassword(), entity.getType());
//    }

    @Override
    public KeyStore getStore(KeystorePersistenceInfo keystorePersistenceInfo) {
        KeyStoreContentInfo keyStoreInfo = getStoreContent(keystorePersistenceInfo);
        return loadStore(keyStoreInfo);
    }

//    @Override
//    public List<TrustStoreEntry> getStoreEntries(String storeName) {
//        final KeyStore store = getStore(storeName);
//        return getStoreEntries(store);
//    }

    @Override
    public List<TrustStoreEntry> getStoreEntries(KeystorePersistenceInfo keystorePersistenceInfo) {
        KeyStoreContentInfo storeInfo = keystorePersistenceService.loadStore(keystorePersistenceInfo);

        KeyStore store = loadStore(storeInfo);

        return getStoreEntries(store);
    }


    @Override
    public boolean addCertificate(KeystorePersistenceInfo persistenceInfo, byte[] certificateContent, String alias, boolean overwrite) {
        X509Certificate certificate = loadCertificateFromString(new String(certificateContent));
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

//    private void replaceStore(byte[] fileContent, String filePassword, String storeType, KeystorePersistenceInfo persistenceInfo) {
//        String storeName = persistenceInfo.getName();
//        KeyStore store = getStore(persistenceInfo);
//        LOG.debug("Store [{}] with entries [{}] will be replaced.", storeName, getStoreEntries(store));
//        try {
//            validateStoreContent(fileContent, filePassword, storeType, storeName);
//            keystorePersistenceService.saveStore(fileContent, storeType, persistenceInfo);
//            LOG.info("Store [{}] successfully replaced with [{}].", storeName, getStoreEntries(store));
//
//            auditService.addStoreReplacedAudit(storeName);
//        } catch (CryptoException exc) {
//            throw new CryptoException("Could not replace store " + storeName, exc);
//        }
//    }

//    private void validateStoreContent(byte[] fileContent, String filePassword, String storeType, String storeName) {
//        try (ByteArrayInputStream newStoreContent = new ByteArrayInputStream(fileContent)) {
//            validateLoadOperation(newStoreContent, filePassword, storeType);
//
//            KeyStore store = KeyStore.getInstance(storeType);
//            store.load(newStoreContent, filePassword.toCharArray());
//        } catch (CertificateException | NoSuchAlgorithmException | IOException | CryptoException | KeyStoreException e) {
//            throw new CryptoException("Could not replace the store named " + storeName, e);
//        }
//    }

    private void validateStoreContent(KeyStoreContentInfo storeInfo) {
        LOG.debug("Validating store [{}] content type [{}]", storeInfo.getName(), storeInfo.getType());
        try (ByteArrayInputStream storeContent = new ByteArrayInputStream(storeInfo.getContent())) {
            KeyStore store = KeyStore.getInstance(storeInfo.getType());
            store.load(storeContent, storeInfo.getPassword().toCharArray());
            storeContent.reset();
        } catch (CertificateException | NoSuchAlgorithmException | IOException | CryptoException | KeyStoreException e) {
            throw new CryptoException("Could not replace the store named " + storeInfo.getName(), e);
        }
    }

//    protected Long replaceStore(byte[] fileContent, String filePassword, String storeType, String storeName) throws CryptoException {
//        LOG.debug("Replacing the current store [{}] with the provided file content.", storeName);
//
//        TruststoreEntity entity = getStoreEntitySafely(storeName);
//        if (entity != null) {
//            KeyStore store = loadStore(entity.getContent(), entity.getPassword(), entity.getType());
//            LOG.debug("Store [{}] with entries [{}] found and will be replaced.", storeName, getStoreEntries(store));
//            try (ByteArrayOutputStream oldStoreContent = new ByteArrayOutputStream()) {
//                char[] oldPassword = entity.getPassword().toCharArray();
//                store.store(oldStoreContent, oldPassword);
//                try {
//                    return doReplace(fileContent, filePassword, storeType, storeName);
//                } catch (CryptoException ex) {
//                    restoreOriginalStore(storeName, store, oldStoreContent, oldPassword);
//                    throw new CryptoException("Could not replace store " + storeName, ex);
//                }
//            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | ZoneRulesException exc) {
//                throw new CryptoException("Could not replace store " + storeName, exc);
//            }
//        }
//
//        LOG.debug("Store [{}] is not found so it will be set", storeName);
//        return doReplace(fileContent, filePassword, storeType, storeName);
//    }

//    private Long doReplace(byte[] fileContent, String filePassword, String storeType, String storeName) {
//        try (ByteArrayInputStream newStoreContent = new ByteArrayInputStream(fileContent)) {
//            validateLoadOperation(newStoreContent, filePassword, storeType);
//
//            KeyStore store = KeyStore.getInstance(storeType);
//            store.load(newStoreContent, filePassword.toCharArray());
//
//            Long entityId = persistStore(store, filePassword, storeType, storeName);
//            LOG.info("Store [{}] successfully replaced with [{}].", storeName, getStoreEntries(store));
//
//            auditService.addStoreReplacedAudit(storeName, entityId);
//            return entityId;
//        } catch (CertificateException | NoSuchAlgorithmException | IOException | CryptoException | KeyStoreException e) {
//            throw new CryptoException("Could not replace the store named " + storeName, e);
//        }
//    }

//    private void restoreOriginalStore(String storeName, KeyStore store, ByteArrayOutputStream oldStoreContent, char[] oldPassword) {
//        try (InputStream stream = oldStoreContent.toInputStream()) {
//            store.load(stream, oldPassword);
//            LOG.warn("Error occurred so the old store [{}] content with entries [{}] was loaded back.", storeName, getStoreEntries(storeName));
//        } catch (CertificateException | NoSuchAlgorithmException | IOException exc) {
//            throw new CryptoException("Could not replace store and old store was not reverted properly. Please correct the error before continuing.", exc);
//        }
//    }

//    protected byte[] getStoreContentFromFile(String location) {
//        File file = new File(location);
//        Path path = Paths.get(file.getAbsolutePath());
//        try {
//            return Files.readAllBytes(path);
//        } catch (IOException e) {
//            throw new DomibusCertificateException("Could not read store from [" + location + "]");
//        }
//    }

    protected void validateLoadOperation(ByteArrayInputStream storeContent, String password, String type) {
        try {
            KeyStore tempTrustStore = KeyStore.getInstance(type);
            tempTrustStore.load(storeContent, password.toCharArray());
            storeContent.reset();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new DomibusCertificateException("Could not load store: " + e.getMessage(), e);
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

    protected boolean doAddCertificates(KeystorePersistenceInfo persistenceInfo, List<CertificateEntry> certificates, boolean overwrite) {
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
            LOG.debug("The store already contains alias [{}] and the overwrite is false so no adding.", alias);
            return false;
        }
        if (certificateHelper.containsAndIdentical(keystore, alias, certificate)) {
            LOG.info("The store already contains alias [{}] and it is identical so no adding.", alias);
            return false;
        }
        try {
            if (containsAlias) {
                keystore.deleteEntry(alias);
            }
            keystore.setCertificateEntry(alias, certificate);
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
            LOG.debug("The store does not contain alias [{}] so no removing.", alias);
            return false;
        }
        try {
            keystore.deleteEntry(alias);
            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }

//    protected TruststoreEntity getStoreEntity(String storeName) {
//        try {
//            TruststoreEntity entity = truststoreDao.findByName(storeName);
//            if (entity == null) {
//                throw new ConfigurationException("Could not find store entity with name: " + storeName);
//            }
//            String decrypted = decrypt(storeName, entity.getPassword());
//            entity.setPassword(decrypted);
//            return entity;
//        } catch (Exception ex) {
//            LOG.debug("Error while retrieving store entity [{}]", storeName, ex);
//            throw new ConfigurationException("Could not retrieve store entity " + storeName, ex);
//        }
//    }

    protected KeyStore loadStore(InputStream contentStream, String password, String type) {
        KeyStore keystore;
        try {
            keystore = KeyStore.getInstance(type);
            keystore.load(contentStream, password.toCharArray());
        } catch (Exception ex) {
            throw new ConfigurationException("Exception loading store.", ex);
        } finally {
            if (contentStream != null) {
                closeStream(contentStream);
            }
        }
        return keystore;
    }

    protected KeyStore loadStore(KeyStoreContentInfo storeInfo) {
        try (InputStream contentStream = new ByteArrayInputStream(storeInfo.getContent())) {
            return loadStore(contentStream, storeInfo.getPassword(), storeInfo.getType());
        } catch (Exception ex) {
            throw new ConfigurationException("Exception loading store.", ex);
        }
    }

//    protected KeyStore loadStore(byte[] content, String password, String type) {
//        try (InputStream contentStream = new ByteArrayInputStream(content)) {
//            return loadStore(contentStream, password, type);
//        } catch (Exception ex) {
//            throw new ConfigurationException("Exception loading store.", ex);
//        }
//    }

    /**
     * @return EntityId of the {@link TruststoreEntity}
     */
    // used for add/remove certificates ( the persisted store is the same as the one modified)
//    protected Long persistStore(KeyStore store, KeystorePersistenceInfo storeName) throws CryptoException {
//        backupStore(storeName);
//
//        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//            TruststoreEntity entity = truststoreDao.findByName(storeName);
//
//            String password = entity.getPassword();
//            String decryptedPassword = decrypt(storeName, password);
//            store.store(byteStream, decryptedPassword.toCharArray());
//            byte[] content = byteStream.toByteArray();
//
//            entity.setContent(content);
//            truststoreDao.update(entity);
//            return entity.getEntityId();
//        } catch (Exception e) {
//            throw new CryptoException("Could not persist store:", e);
//        }
//    }

//    protected Long persistStore(KeyStore keystore, String password, String storeType, String storeName) throws CryptoException {
//        backupStore(storeName);
//
//        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//            keystore.store(byteStream, password.toCharArray());
//            byte[] content = byteStream.toByteArray();
//            String passToSave = getPassToSave(password, storeName);
//
//            TruststoreEntity entity, existing = truststoreDao.findByNameSafely(storeName);
//            if (existing == null) {
//                entity = new TruststoreEntity();
//                entity.setName(storeName);
//            } else {
//                entity = existing;
//            }
//
//            entity.setContent(content);
//            entity.setPassword(passToSave);
//            entity.setType(storeType);
//
//            if (existing == null) {
//                truststoreDao.create(entity);
//            } else {
//                truststoreDao.update(entity);
//            }
//            return entity.getEntityId();
//        } catch (Exception e) {
//            throw new CryptoException("Could not persist store named " + storeName, e);
//        }
//    }

//    private String getPassToSave(String password, String trustName) {
//        String passToSave = password;
//        Boolean encrypted = domibusPropertyProvider.getBooleanProperty(DOMIBUS_PASSWORD_ENCRYPTION_ACTIVE);
//        if (encrypted) {
//            PasswordEncryptionResult res = passwordEncryptionService.encryptProperty(domainContextProvider.getCurrentDomainSafely(), trustName + ".password", password);
//            passToSave = res.getFormattedBase64EncryptedValue();
//        }
//        return passToSave;
//    }
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

        boolean different = !securityUtilImpl.areKeystoresIdentical(store, storeOnDisk);
        if (different) {
            LOG.info("The store [{}] on disk has different content than the persisted one.", storeName);
        } else {
            LOG.debug("The store [{}] on disk has the same content as the persisted one.", storeName);
        }
        return different;
    }

    protected void closeStream(Closeable stream) {
        try {
            LOG.debug("Closing output stream [{}].", stream);
            stream.close();
        } catch (IOException e) {
            LOG.error("Could not close [{}]", stream, e);
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
            LOG.securityError(SEC_CERTIFICATE_REVOKED, certificate.getCertificateType(), certificate.getAlias(), certificate.getNotAfter());
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

        LOG.debug("Searching for certificate about to expire with notification date smaller than:[{}] and expiration date between current date and current date + offset[{}]->[{}]",
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

//    protected File createFileWithLocation(String location) {
//        return new File(location);
//    }

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

    private String extractFingerprints(final X509Certificate certificate) {
        if (certificate == null)
            return null;

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


