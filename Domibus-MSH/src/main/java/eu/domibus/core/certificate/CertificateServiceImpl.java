package eu.domibus.core.certificate;

import com.google.common.collect.Lists;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.TruststoreInfo;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.alerts.configuration.certificate.expired.ExpiredCertificateConfigurationManager;
import eu.domibus.core.alerts.configuration.certificate.expired.ExpiredCertificateModuleConfiguration;
import eu.domibus.core.alerts.configuration.certificate.imminent.ImminentExpirationCertificateConfigurationManager;
import eu.domibus.core.alerts.configuration.certificate.imminent.ImminentExpirationCertificateModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.certificate.crl.CRLService;
import eu.domibus.core.certificate.crl.DomibusCRLException;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.pmode.provider.PModeProvider;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CERTIFICATE_REVOCATION_OFFSET;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_ACTIVE;
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

    private final CRLService crlService;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final CertificateDao certificateDao;

    private final EventService eventService;

    private final PModeProvider pModeProvider;

    private final ImminentExpirationCertificateConfigurationManager imminentExpirationCertificateConfigurationManager;

    private final ExpiredCertificateConfigurationManager expiredCertificateConfigurationManager;

    private final CertificateHelper certificateHelper;

    protected final DomainService domainService;

    protected final DomainTaskExecutor domainTaskExecutor;

    protected final TruststoreDao truststoreDao;

    private final PasswordDecryptionService passwordDecryptionService;

    private final PasswordEncryptionService passwordEncryptionService;

    private final DomainContextProvider domainContextProvider;

    private final DomibusCoreMapper coreMapper;

    public CertificateServiceImpl(CRLService crlService,
                                  DomibusPropertyProvider domibusPropertyProvider,
                                  CertificateDao certificateDao,
                                  EventService eventService,
                                  PModeProvider pModeProvider,
                                  ImminentExpirationCertificateConfigurationManager imminentExpirationCertificateConfigurationManager,
                                  ExpiredCertificateConfigurationManager expiredCertificateConfigurationManager,
                                  CertificateHelper certificateHelper,
                                  DomainService domainService,
                                  DomainTaskExecutor domainTaskExecutor,
                                  TruststoreDao truststoreDao,
                                  PasswordDecryptionService passwordDecryptionService,
                                  PasswordEncryptionService passwordEncryptionService,
                                  DomainContextProvider domainContextProvider, DomibusCoreMapper coreMapper) {
        this.crlService = crlService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateDao = certificateDao;
        this.eventService = eventService;
        this.pModeProvider = pModeProvider;
        this.imminentExpirationCertificateConfigurationManager = imminentExpirationCertificateConfigurationManager;
        this.expiredCertificateConfigurationManager = expiredCertificateConfigurationManager;
        this.certificateHelper = certificateHelper;
        this.domainService = domainService;
        this.domainTaskExecutor = domainTaskExecutor;
        this.truststoreDao = truststoreDao;
        this.passwordDecryptionService = passwordDecryptionService;
        this.passwordEncryptionService = passwordEncryptionService;
        this.domainContextProvider = domainContextProvider;
        this.coreMapper = coreMapper;
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
    public boolean isCertificateChainValid(KeyStore trustStore, String alias) throws DomibusCertificateException {
        X509Certificate[] certificateChain = null;
        try {
            certificateChain = getCertificateChain(trustStore, alias);
        } catch (KeyStoreException e) {
            throw new DomibusCertificateException("Error getting the certificate chain from the truststore for [" + alias + "]", e);
        }
        if (certificateChain == null || certificateChain.length == 0 || certificateChain[0] == null) {
            throw new DomibusCertificateException("Could not find alias in the truststore [" + alias + "]");
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
    public List<X509Certificate> deserializeCertificateChainFromPemFormat(String chain) {
        List<X509Certificate> certificates = new ArrayList<>();
        try (PemReader reader = new PemReader(new StringReader(chain))) {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            PemObject pemObject;
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

        } catch (IOException | CertificateException e) {
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

    @Override
    public void replaceStore(String fileLocation, String filePassword, String trustName) {
        Path path = Paths.get(fileLocation);
        String fileName = path.getFileName().toString();
        byte[] fileContent = getTruststoreContentFromFile(fileLocation);
        replaceStore(fileName, fileContent, filePassword, trustName);
    }

    @Override
    public void replaceStore(String fileName, byte[] fileContent, String filePassword, String trustName) {
        String storeType = certificateHelper.getStoreType(fileName);
        replaceStore(fileContent, filePassword, storeType, trustName);
    }

    @Override
    public KeyStore getTrustStore(String trustName) {
        TruststoreEntity entity = getTruststoreEntity(trustName);
        return loadTrustStore(entity.getContent(), entity.getPassword(), entity.getType());
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries(String name) {
        final KeyStore store = getTrustStore(name);
        return getTrustStoreEntries(store);
    }

    @Override
    public boolean addCertificate(String trustName, byte[] certificateContent, String alias, boolean overwrite) {
        X509Certificate certificate = loadCertificateFromString(new String(certificateContent));
        List<CertificateEntry> certificates = Arrays.asList(new CertificateEntry(alias, certificate));

        return doAddCertificates(trustName, certificates, overwrite);
    }

    @Override
    public boolean addCertificates(String trustName, List<CertificateEntry> certificates, boolean overwrite) {
        return doAddCertificates(trustName, certificates, overwrite);
    }

    @Override
    public boolean removeCertificate(String trustName, String alias) {
        List<String> aliases = Arrays.asList(alias);
        return doRemoveCertificates(trustName, aliases);
    }

    @Override
    public boolean removeCertificates(String trustName, List<String> aliases) {
        return doRemoveCertificates(trustName, aliases);
    }

    @Override
    public byte[] getTruststoreContent(String trustName) {
        TruststoreEntity res = getTruststoreEntity(trustName);
        return res.getContent();
    }

    @Override
    public TruststoreInfo getTruststoreInfo(String trustName) {
        TruststoreEntity entity = getTruststoreEntity(trustName);
        return coreMapper.truststoreEntityToTruststoreInfo(entity);
    }

    protected void replaceStore(byte[] fileContent, String filePassword, String storeType, String trustName) throws CryptoException {
        LOG.debug("Replacing the existing truststore [{}] with the provided one.", trustName);

        TruststoreEntity entity = getTruststoreEntitySafely(trustName);
        if (entity != null) {
            KeyStore truststore = loadTrustStore(entity.getContent(), entity.getPassword(), entity.getType());
            try (ByteArrayOutputStream oldTrustStoreBytes = new ByteArrayOutputStream()) {
                truststore.store(oldTrustStoreBytes, entity.getPassword().toCharArray());

                doReplace(fileContent, filePassword, storeType, trustName, truststore, entity.getPassword(), oldTrustStoreBytes);
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException exc) {
                throw new CryptoException("Could not replace truststore " + trustName, exc);
            }
        } else {
            try {
                KeyStore truststore = KeyStore.getInstance(storeType);

                doReplace(fileContent, filePassword, storeType, trustName, truststore, null, null);
            } catch (KeyStoreException exc) {
                throw new CryptoException("Could not create a store named " + trustName, exc);
            }
        }
    }

    private void doReplace(byte[] fileContent, String filePassword, String storeType, String trustName, KeyStore truststore,
                           String oldPassword, ByteArrayOutputStream oldTrustStoreBytes) {
        try (ByteArrayInputStream newTrustStoreBytes = new ByteArrayInputStream(fileContent)) {
            validateLoadOperation(newTrustStoreBytes, filePassword, storeType);
            truststore.load(newTrustStoreBytes, filePassword.toCharArray());
            LOG.debug("Truststore successfully loaded");

            persistTrustStore(truststore, filePassword, storeType, trustName);
            LOG.debug("Truststore successfully persisted");
        } catch (CertificateException | NoSuchAlgorithmException | IOException | CryptoException e) {
            if (oldTrustStoreBytes != null) {
                try {
                    truststore.load(oldTrustStoreBytes.toInputStream(), oldPassword.toCharArray());
                } catch (CertificateException | NoSuchAlgorithmException | IOException exc) {
                    throw new CryptoException("Could not replace truststore and old truststore was not reverted properly. Please correct the error before continuing.", exc);
                }
            }
            throw new CryptoException("Could not persist the truststore named " + trustName, e);
        }
    }

    protected byte[] getTruststoreContentFromFile(String location) {
        File file = createFileWithLocation(location);
        Path path = Paths.get(file.getAbsolutePath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new DomibusCertificateException("Could not read truststore from [" + location + "]");
        }
    }

    protected void validateLoadOperation(ByteArrayInputStream newTrustStoreBytes, String password, String type) {
        try {
            KeyStore tempTrustStore = KeyStore.getInstance(type);
            tempTrustStore.load(newTrustStoreBytes, password.toCharArray());
            newTrustStoreBytes.reset();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new DomibusCertificateException("Could not load store: " + e.getMessage(), e);
        }
    }

    protected List<TrustStoreEntry> getTrustStoreEntries(final KeyStore trustStore) {
        try {
            List<TrustStoreEntry> trustStoreEntries = new ArrayList<>();
            Integer certificateExpiryAlertDays = domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
            LOG.debug("Certificate imminent expiry alert delay days:[{}]", certificateExpiryAlertDays);
            final Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                final X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                TrustStoreEntry trustStoreEntry = createTrustStoreEntry(alias, certificate);
                if (trustStoreEntry != null) {
                    trustStoreEntry.setCertificateExpiryAlertDays(certificateExpiryAlertDays);
                    trustStoreEntries.add(trustStoreEntry);
                } else {
                    LOG.debug("The given alias:[{}] does not exist or does not contain a certificate.", alias);
                }
            }
            return trustStoreEntries;
        } catch (KeyStoreException e) {
            LOG.warn(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    protected boolean doAddCertificates(String trustName, List<CertificateEntry> certificates, boolean overwrite) {
        KeyStore trustStore = getTrustStore(trustName);

        int addedNr = 0;
        for (CertificateEntry certificateEntry : certificates) {
            boolean added = doAddCertificate(trustStore, certificateEntry.getCertificate(), certificateEntry.getAlias(), overwrite);
            if (added) {
                addedNr++;
            }
        }
        if (addedNr > 0) {
            LOG.trace("Added [{}] certificates so persisting the truststore.", addedNr);
            persistTrustStore(trustStore, trustName);
            return true;
        }
        LOG.trace("Added 0 certificates so exiting without persisting the truststore.");
        return false;
    }

    protected boolean doRemoveCertificates(String trustName, List<String> aliases) {
        KeyStore trustStore = getTrustStore(trustName);

        int removedNr = 0;
        for (String alias : aliases) {
            boolean removed = doRemoveCertificate(trustStore, alias);
            if (removed) {
                removedNr++;
            }
        }
        if (removedNr > 0) {
            LOG.trace("Removed [{}] certificates so persisting the truststore.", removedNr);
            persistTrustStore(trustStore, trustName);
            return true;
        }
        LOG.trace("Removed 0 certificates so exiting without persisting the truststore.");
        return false;
    }

    protected boolean doAddCertificate(KeyStore truststore, X509Certificate certificate, String alias, boolean overwrite) {
        boolean containsAlias;
        try {
            containsAlias = truststore.containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the truststore. This should never happen", e);
        }
        if (containsAlias && !overwrite) {
            LOG.trace("The truststore already contains alias [{}] and the overwrite is false so returning false.", alias);
            return false;
        }
        try {
            if (containsAlias) {
                truststore.deleteEntry(alias);
            }
            truststore.setCertificateEntry(alias, certificate);
            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    protected boolean doRemoveCertificate(KeyStore truststore, String alias) {
        boolean containsAlias;
        try {
            containsAlias = truststore.containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the truststore. This should never happen", e);
        }
        if (!containsAlias) {
            LOG.trace("The truststore does not contain alias [{}] so returning false.", alias);
            return false;
        }
        try {
            truststore.deleteEntry(alias);
            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    protected TruststoreEntity getTruststoreEntity(String trustName) {
        try {
            TruststoreEntity entity = truststoreDao.findByName(trustName);
            String decrypted = passwordDecryptionService.decryptPropertyIfEncrypted(domainContextProvider.getCurrentDomainSafely(), trustName + ".password", entity.getPassword());
            entity.setPassword(decrypted);
            return entity;
        } catch (Exception ex) {
            LOG.debug("Error while retrieving truststore entity [{}]", trustName, ex);
            throw new ConfigurationException("Could not retrieve truststore entity " + trustName, ex);
        }
    }

    protected TruststoreEntity getTruststoreEntitySafely(String trustName) {
        try {
            return getTruststoreEntity(trustName);
        } catch (ConfigurationException ex) {
            return null;
        }
    }

    protected KeyStore loadTrustStore(InputStream contentStream, String password, String type) {
        KeyStore truststore;
        try {
            truststore = KeyStore.getInstance(type);
            truststore.load(contentStream, password.toCharArray());
        } catch (Exception ex) {
            throw new ConfigurationException("Exception loading truststore.", ex);
        } finally {
            if (contentStream != null) {
                closeStream(contentStream);
            }
        }
        return truststore;
    }

    protected KeyStore loadTrustStore(byte[] content, String password, String type) {
        try (InputStream contentStream = new ByteArrayInputStream(content)) {
            return loadTrustStore(contentStream, password, type);
        } catch (Exception ex) {
            throw new ConfigurationException("Exception loading truststore.", ex);
        }
    }

    // used for add/remove certificates ( the persisted store is the same as the one modified)
    protected void persistTrustStore(KeyStore truststore, String trustName) throws CryptoException {
        backupTrustStore(trustName);

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            TruststoreEntity entity = truststoreDao.findByName(trustName);

            String decryptedPassword = passwordDecryptionService.decryptPropertyIfEncrypted(domainContextProvider.getCurrentDomainSafely(), trustName + ".password", entity.getPassword());
            truststore.store(byteStream, decryptedPassword.toCharArray());
            byte[] content = byteStream.toByteArray();

            entity.setContent(content);
            truststoreDao.update(entity);
        } catch (Exception e) {
            throw new CryptoException("Could not persist truststore:", e);
        }
    }

    protected void persistTrustStore(KeyStore truststore, String password, String storeType, String trustName) throws CryptoException {
        backupTrustStore(trustName);

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            truststore.store(byteStream, password.toCharArray());
            byte[] content = byteStream.toByteArray();
            String passToSave = getPassToSave(password, trustName);

            TruststoreEntity entity, existing = truststoreDao.findByNameSafely(trustName);
            if (existing == null) {
                entity = new TruststoreEntity();
                entity.setName(trustName);
            } else {
                entity = existing;
            }

            entity.setContent(content);
            entity.setPassword(passToSave);
            entity.setType(storeType);

            if (existing == null) {
                truststoreDao.create(entity);
            } else {
                truststoreDao.update(entity);
            }
        } catch (Exception e) {
            throw new CryptoException("Could not persist truststore:", e);
        }
    }

    private String getPassToSave(String password, String trustName) {
        String passToSave = password;
        Boolean encrypted = domibusPropertyProvider.getBooleanProperty(DOMIBUS_PASSWORD_ENCRYPTION_ACTIVE);
        if (encrypted) {
            PasswordEncryptionResult res = passwordEncryptionService.encryptProperty(domainContextProvider.getCurrentDomainSafely(), trustName + ".password", password);
            passToSave = res.getFormattedBase64EncryptedValue();
        }
        return passToSave;
    }

    protected void backupTrustStore(String trustName) {
        TruststoreEntity entity = truststoreDao.findByNameSafely(trustName);
        if (entity != null) {
            TruststoreEntity backup = new TruststoreEntity();
            backup.setName(entity.getName() + ".backup." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            backup.setType(entity.getType());
            backup.setPassword(entity.getPassword());
            backup.setContent(entity.getContent());

            truststoreDao.create(backup);
        } else {
            LOG.info("Could not find a store with the name [{}] so no backup performed.", trustName);
        }
    }

    @Override
    public void persistTruststoresIfApplicable(String name, boolean optional,
                                               Supplier<Optional<String>> filePathSupplier, Supplier<String> typeSupplier, Supplier<String> passwordSupplier,
                                               List<Domain> domains) {
        LOG.debug("Persisting the truststore [{}] for all domains if not yet exists", name);
        for (Domain domain : domains) {
            try {
                domainTaskExecutor.submit(() -> persistCurrentDomainTruststoreIfApplicable(name, optional, filePathSupplier, typeSupplier, passwordSupplier), domain);
            } catch (DomibusCertificateException dce) {
                LOG.warn("The truststore [{}] for domain [{}] could not be persisted!", name, domain, dce);
            }
        }
        LOG.debug("Finished persisting the truststore [{}] for all domains if not yet exists", name);
    }

    private void persistCurrentDomainTruststoreIfApplicable(String name, boolean optional,
                                                            Supplier<Optional<String>> filePathSupplier, Supplier<String> typeSupplier, Supplier<String> passwordSupplier) {
        try {
            if (truststoreDao.existsWithName(name)) {
                LOG.debug("The store [{}] is already persisted; exiting", name);
                return;
            }

            Optional<String> filePath = filePathSupplier.get();
            if (!filePath.isPresent()) {
                if (optional) {
                    LOG.info("The store location of [{}] is missing (and optional) so exiting.", name);
                    return;
                }
                throw new DomibusCertificateException(String.format("Truststore with type [%s] is missing and is not optional.", name));
            }

            byte[] content = getTruststoreContentFromFile(filePath.get());

            TruststoreEntity entity = new TruststoreEntity();
            entity.setName(name);
            entity.setType(typeSupplier.get());
            entity.setPassword(passwordSupplier.get());
            entity.setContent(content);

            truststoreDao.create(entity);
        } catch (Exception ex) {
            LOG.error(String.format("The truststore [%s], whose file location is [%s], could not be persisted! " +
                    "Please check that the truststore file is present and the location property is set accordingly.", name, filePathSupplier.get()), ex);
        }
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
        final ExpiredCertificateModuleConfiguration configuration = expiredCertificateConfigurationManager.getConfiguration();
        final boolean activeModule = configuration.isActive();
        LOG.debug("Certificate expired alert module activated:[{}]", activeModule);
        if (!activeModule) {
            LOG.info("Certificate Expired Module is not active; returning.");
            return;
        }
        final String accessPoint = getAccessPointName();
        final Integer revokedDuration = configuration.getExpiredDuration();
        final Integer revokedFrequency = configuration.getExpiredFrequency();
        Date endNotification = Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusDays(revokedDuration).toInstant());
        Date notificationDate = Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusDays(revokedFrequency).toInstant());

        LOG.debug("Searching for expired certificate with notification date smaller than:[{}] and expiration date > current date - offset[{}]->[{}]", notificationDate, revokedDuration, endNotification);
        certificateDao.findExpiredToNotifyAsAlert(notificationDate, endNotification).forEach(certificate -> {
            certificate.setAlertExpiredNotificationDate(Date.from(ZonedDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()));
            certificateDao.saveOrUpdate(certificate);
            final String alias = certificate.getAlias();
            final String accessPointOrAlias = accessPoint == null ? alias : accessPoint;
            eventService.enqueueCertificateExpiredEvent(accessPointOrAlias, alias, certificate.getNotAfter());
        });
    }

    private String getAccessPointName() {
        String partyName = null;
        if (pModeProvider.isConfigurationLoaded()) {
            partyName = pModeProvider.getGatewayParty().getName();
        }
        return partyName;
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
        final ImminentExpirationCertificateModuleConfiguration configuration = imminentExpirationCertificateConfigurationManager.getConfiguration();
        final Boolean activeModule = configuration.isActive();
        LOG.debug("Certificate Imminent expiration alert module activated:[{}]", activeModule);
        if (BooleanUtils.isNotTrue(activeModule)) {
            LOG.info("Imminent Expiration Certificate Module is not active; returning.");
            return;
        }
        final String accessPoint = getAccessPointName();
        final Integer imminentExpirationDelay = configuration.getImminentExpirationDelay();
        final Integer imminentExpirationFrequency = configuration.getImminentExpirationFrequency();

        final Date today = Date.from(ZonedDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant());
        final Date maxDate = Date.from(ZonedDateTime.now(ZoneOffset.UTC).plusDays(imminentExpirationDelay).toInstant());
        final Date notificationDate = Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusDays(imminentExpirationFrequency).toInstant());

        LOG.debug("Searching for certificate about to expire with notification date smaller than:[{}] and expiration date between current date and current date + offset[{}]->[{}]",
                notificationDate, imminentExpirationDelay, maxDate);
        certificateDao.findImminentExpirationToNotifyAsAlert(notificationDate, today, maxDate).forEach(certificate -> {
            certificate.setAlertImminentNotificationDate(today);
            certificateDao.saveOrUpdate(certificate);
            final String alias = certificate.getAlias();
            final String accessPointOrAlias = accessPoint == null ? alias : accessPoint;
            eventService.enqueueImminentCertificateExpirationEvent(accessPointOrAlias, alias, certificate.getNotAfter());
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

    protected File createFileWithLocation(String location) {
        return new File(location);
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

    @Override
    public void removeTruststore(String truststoreName, Domain domain) {
        domainTaskExecutor.submit(() -> doRemoveTruststore(truststoreName, domain), domain);
    }

    private void doRemoveTruststore(String truststoreName, Domain domain) {
        TruststoreEntity entity = truststoreDao.findByNameSafely(truststoreName);
        if (entity == null) {
            LOG.warn("Could not find store named [{}] for domain [{}] to delete", truststoreName, domain);
            return;
        }
        truststoreDao.delete(entity);
    }
}


