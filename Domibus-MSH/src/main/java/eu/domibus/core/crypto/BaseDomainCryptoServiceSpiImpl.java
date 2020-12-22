package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.*;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.backup.BackupService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public abstract class BaseDomainCryptoServiceSpiImpl extends Merlin implements DomainCryptoServiceSpi {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BaseDomainCryptoServiceSpiImpl.class);

    protected Domain domain;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected SignalService signalService;

    @Autowired
    protected DomainCoreConverter domainCoreConverter;

    @Autowired
    protected BackupService backupService;

    @Override
    public void init() {
        LOG.debug("Initializing the certificate provider");

        final Properties allProperties = new Properties();
        allProperties.putAll(getKeystoreProperties());
        allProperties.putAll(getTrustStoreProperties());
        try {
            super.loadProperties(allProperties, Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error occurred when loading the properties of TrustStore/KeyStore: " + e.getMessage(), e);
        }
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
    public abstract String getPrivateKeyPassword(String alias);

    @Override
    public synchronized void refreshTrustStore() {
        final KeyStore trustStore = loadTrustStore();
        setTrustStore(trustStore);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    public synchronized void replaceTrustStore(byte[] store, String password) throws CryptoSpiException {
        LOG.debug("Replacing the existing trust store file [{}] with the provided one", getTrustStoreLocation());

        ByteArrayOutputStream oldTrustStoreBytes = new ByteArrayOutputStream();
        try {
            getTruststore().store(oldTrustStoreBytes, getTrustStorePassword().toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException exc) {
            closeOutputStream(oldTrustStoreBytes);
            throw new CryptoSpiException("Could not replace truststore", exc);
        }
        try (ByteArrayInputStream newTrustStoreBytes = new ByteArrayInputStream(store)) {
            certificateService.validateLoadOperation(newTrustStoreBytes, password, getTrustStoreType());
            getTruststore().load(newTrustStoreBytes, password.toCharArray());
            LOG.debug("Truststore successfully loaded");
            persistTrustStore();
            LOG.debug("Truststore successfully persisted");
        } catch (CertificateException | NoSuchAlgorithmException | IOException | CryptoException e) {
            LOG.error("Could not replace truststore", e);
            try {
                getTruststore().load(oldTrustStoreBytes.toInputStream(), getTrustStorePassword().toCharArray());
                signalTrustStoreUpdate();
            } catch (CertificateException | NoSuchAlgorithmException | IOException exc) {
                throw new CryptoSpiException("Could not replace truststore and old truststore was not reverted properly. Please correct the error before continuing.", exc);
            }
            throw new CryptoSpiException(e);
        } finally {
            closeOutputStream(oldTrustStoreBytes);
        }
    }

    protected KeyStore getTruststore() {
        return truststore;
    }

    private void closeOutputStream(ByteArrayOutputStream outputStream) {
        try {
            LOG.debug("Closing output stream [{}].", outputStream);
            outputStream.close();
        } catch (IOException e) {
            LOG.error("Could not close [{}]", outputStream, e);
        }
    }

    protected synchronized void persistTrustStore() throws CryptoException {
        String trustStoreLocation = getTrustStoreLocation();
        LOG.debug("TrustStoreLocation is: [{}]", trustStoreLocation);
        File trustStoreFile = createTrustStoreFile(trustStoreLocation);
        if (!parentFileExists(trustStoreFile)) {
            LOG.debug("Creating directory [" + trustStoreFile.getParentFile() + "]");
            try {
                FileUtils.forceMkdir(trustStoreFile.getParentFile());
            } catch (IOException e) {
                throw new CryptoException("Could not create parent directory for truststore", e);
            }
        }
        // keep old truststore in case it needs to be restored, truststore_name.backup-yyyy-MM-dd_HH_mm_ss.SSS
        backupTrustStore(trustStoreFile);

        LOG.debug("TrustStoreFile is: [{}]", trustStoreFile.getAbsolutePath());
        try (FileOutputStream fileOutputStream = createFileOutputStream(trustStoreFile)) {
            getTruststore().store(fileOutputStream, getTrustStorePassword().toCharArray());
        } catch (FileNotFoundException ex) {
            LOG.error("Could not persist truststore:", ex);
            //we address this exception separately
            //we swallow it here because it contains information we do not want to display to the client: the full internal file path of the truststore.
            throw new CryptoException("Could not persist truststore: Is the truststore readonly?");
        } catch (NoSuchAlgorithmException | IOException | CertificateException | KeyStoreException e) {
            throw new CryptoException("Could not persist truststore:", e);
        }

        signalTrustStoreUpdate();
    }

    // extracted these methods for testing purposes
    protected boolean parentFileExists(File trustStoreFile) {
        return trustStoreFile.getParentFile().exists();
    }

    protected FileOutputStream createFileOutputStream(File trustStoreFile) throws FileNotFoundException {
        return new FileOutputStream(trustStoreFile);
    }

    protected File createTrustStoreFile(String trustStoreLocation) {
        return new File(trustStoreLocation);
    }

    protected void backupTrustStore(File trustStoreFile) throws CryptoException {
        if (trustStoreFile == null || StringUtils.isEmpty(trustStoreFile.getAbsolutePath())) {
            LOG.warn("Truststore file was null, nothing to backup!");
            return;
        }
        if (!trustStoreFile.exists()) {
            LOG.warn("Truststore file [{}] does not exist, nothing to backup!", trustStoreFile);
            return;
        }

        try {
            backupService.backupFile(trustStoreFile);
        } catch (IOException e) {
            throw new CryptoException("Could not create backup file for truststore", e);
        }
    }


    @Override
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException {
        LOG.debug("Checking certificate validation for [{}]", alias);
        KeyStore trustStore = getTrustStore();
        return certificateService.isCertificateChainValid(trustStore, alias);
    }

    @Override
    public synchronized boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        boolean added = doAddCertificate(certificate, alias, overwrite);
        if (added) {
            persistTrustStore();
        }
        return added;
    }

    @Override
    public synchronized void addCertificate(List<CertificateEntrySpi> certificates, boolean overwrite) {
        certificates.forEach(certEntry ->
                doAddCertificate(certEntry.getCertificate(), certEntry.getAlias(), overwrite));
        persistTrustStore();
    }

    private boolean doAddCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        boolean containsAlias;
        try {
            containsAlias = getTrustStore().containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the truststore. This should never happen", e);
        }
        if (containsAlias && !overwrite) {
            return false;
        }
        try {
            if (containsAlias) {
                getTrustStore().deleteEntry(alias);
            }
            getTrustStore().setCertificateEntry(alias, certificate);

            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    protected KeyStore loadTrustStore() {
        String trustStoreLocation = getTrustStoreLocation();
        if (trustStoreLocation != null) {
            trustStoreLocation = trustStoreLocation.trim();

            try (InputStream is = loadInputStream(this.getClass().getClassLoader(), trustStoreLocation)) {
                String passwd = getTrustStorePassword();
                if (passwd != null) {
                    passwd = passwd.trim();
                    passwd = decryptPassword(passwd, passwordEncryptor);
                }
                String type = getTrustStoreType();
                if (type != null) {
                    type = type.trim();
                }
                final KeyStore trustStore = load(is, passwd, null, type);
                LOG.debug("The TrustStore {} of type {} has been loaded", trustStoreLocation, type);
                return trustStore;
            } catch (WSSecurityException | IOException e) {
                throw new CryptoException("Error loading truststore", e);
            }
        }
        throw new CryptoException("Could not load truststore, truststore location is empty");
    }

    protected Properties getKeystoreProperties() {
        final String keystoreType = getKeystoreType();
        final String keystorePassword = getKeystorePassword();
        final String privateKeyAlias = getPrivateKeyAlias();
        final String keystoreLocation = getKeystoreLocation();

        if (StringUtils.isAnyEmpty(keystoreType, keystorePassword, privateKeyAlias, keystoreLocation)) {
            LOG.error("One of the keystore property values is null for domain [{}]: keystoreType=[{}], keystorePassword, privateKeyAlias=[{}], keystoreLocation=[{}]",
                    domain, keystoreType, privateKeyAlias, keystoreLocation);
            throw new ConfigurationException("Error while trying to load the keystore properties for domain " + domain);
        }

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystoreType);
        final String keyStorePasswordProperty = Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD; //NOSONAR
        result.setProperty(keyStorePasswordProperty, keystorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, privateKeyAlias);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_FILE, keystoreLocation);

        Properties logProperties = new Properties();
        logProperties.putAll(result);
        logProperties.remove(keyStorePasswordProperty);
        LOG.debug("Keystore properties for domain [{}] are [{}]", domain, logProperties);

        return result;
    }

    protected abstract String getKeystoreLocation();

    protected abstract String getPrivateKeyAlias();

    protected abstract String getKeystorePassword();

    protected abstract String getKeystoreType();

    protected Properties getTrustStoreProperties() {
        final String trustStoreType = getTrustStoreType();
        final String trustStorePassword = getTrustStorePassword();
        final String trustStoreLocation = getTrustStoreLocation();

        if (StringUtils.isAnyEmpty(trustStoreType, trustStorePassword, trustStoreLocation)) {
            LOG.error("One of the truststore property values is null for domain [{}]: trustStoreType=[{}], trustStorePassword, trustStoreLocation=[{}]",
                    domain, trustStoreType, trustStoreLocation);
            throw new ConfigurationException("Error while trying to load the truststore properties for domain " + domain);
        }

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_TYPE, trustStoreType);
        final String trustStorePasswordProperty = Merlin.PREFIX + Merlin.TRUSTSTORE_PASSWORD; //NOSONAR
        result.setProperty(trustStorePasswordProperty, trustStorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.LOAD_CA_CERTS, "false");
        result.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_FILE, trustStoreLocation);

        Properties logProperties = new Properties();
        logProperties.putAll(result);
        logProperties.remove(trustStorePasswordProperty);
        LOG.debug("Truststore properties for domain [{}] are [{}]", domain, logProperties);

        return result;
    }

    protected abstract String getTrustStoreLocation();

    protected abstract String getTrustStorePassword();

    public abstract String getTrustStoreType();

    protected void signalTrustStoreUpdate() {
        // Sends a signal to all the servers from the cluster in order to trigger the refresh of the trust store
        signalService.signalTrustStoreUpdate(domain);
    }

    @Override
    public boolean removeCertificate(String alias) {
        boolean removed = doRemoveCertificate(alias);
        if (removed) {
            persistTrustStore();
        }
        return removed;
    }

    @Override
    public void removeCertificate(List<String> aliases) {
        aliases.forEach(this::doRemoveCertificate);
        persistTrustStore();
    }

    @Override
    public abstract String getIdentifier();

    @Override
    public void setDomain(DomainSpi domain) {
        this.domain = domainCoreConverter.convert(domain, Domain.class);
    }

    @Override
    public byte[] getTruststoreContent() {
        String location = getTrustStoreLocation();
        File file = new File(location);
        Path path = Paths.get(file.getAbsolutePath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new DomibusCertificateSpiException("Could not read truststore from [" + location + "]");
        }
    }

    private synchronized boolean doRemoveCertificate(String alias) {
        boolean containsAlias;
        try {
            containsAlias = getTrustStore().containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the truststore. This should never happen", e);
        }
        if (!containsAlias) {
            return false;
        }
        try {
            getTrustStore().deleteEntry(alias);
            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }
}
