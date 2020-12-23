package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.CertificateEntrySpi;
import eu.domibus.core.util.backup.BackupService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.apache.wss4j.common.ext.WSSecurityException.ErrorCode.SECURITY_ERROR;

/**
 * @author Thomas Dussart
 * @author Ion Perpegel
 * @since 4.0
 */
@RunWith(JMockit.class)
public class BaseDomainCryptoServiceSpiImplTest {

    @Tested
    private DefaultDomainCryptoServiceSpiImpl domainCryptoService;

    @Injectable
    protected CertificateService certificateService;

    @Injectable
    protected SignalService signalService;

    @Injectable
    private X509Certificate x509Certificate;

    @Injectable
    private DomainCoreConverter coreConverter;

    @Injectable
    private Domain domain;

    @Injectable
    private BackupService backupService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throwsExceptionWhenFailingToLoadMerlinProperties_IOException() throws WSSecurityException, IOException {
        new Expectations(domainCryptoService) {{
            domainCryptoService.getKeystoreProperties();
            result = new Properties();
            domainCryptoService.getTrustStoreProperties();
            result = new Properties();

            domainCryptoService.loadProperties((Properties) any, (ClassLoader) any, null);
            result = new IOException();
        }};

        try {
            domainCryptoService.init();
            Assert.fail();
        } catch (CryptoException ex) {
            Assert.assertTrue(ex.getMessage().contains("Error occurred when loading the properties of TrustStore/KeyStore"));
        }
    }

    @Test
    public void throwsExceptionWhenFailingToLoadMerlinProperties_WSSecurityException(@Mocked Properties keyProps, @Mocked Properties trustProps)
            throws WSSecurityException, IOException {
        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error occurred when loading the properties of TrustStore/KeyStore");

        new Expectations(domainCryptoService) {{
            domainCryptoService.getKeystoreProperties();
            result = keyProps;
            domainCryptoService.getTrustStoreProperties();
            result = trustProps;
            domainCryptoService.loadProperties((Properties) any, (ClassLoader) any, null);
            result = new WSSecurityException(SECURITY_ERROR);
        }};

        // When
        domainCryptoService.init();
    }

    @Test
    public void returnsKeystoreCertificateFromMerlin(@Mocked Merlin merlin, @Injectable KeyStore keyStore) throws Exception {
        // Given
        String alias = "alias";
        new Expectations() {{
            merlin.getKeyStore();
            result = keyStore;
            keyStore.getCertificate(alias);
            result = x509Certificate;
        }};

        // When
        X509Certificate certificateFromKeyStore = domainCryptoService.getCertificateFromKeyStore(alias);

        // Then
        Assert.assertNotNull("Should have returned the keystore certificate from Merlin", certificateFromKeyStore);
    }


    @Test
    public void returnsTrustStoreCertificateFromMerlin(@Mocked Merlin merlin, @Injectable KeyStore trustStore) throws Exception {
        // Given
        String alias = "alias";
        new Expectations() {{
            merlin.getTrustStore();
            result = trustStore;
            trustStore.getCertificate(alias);
            result = x509Certificate;
        }};

        // When
        X509Certificate certificateFromTrustStore = domainCryptoService.getCertificateFromTrustStore(alias);

        // Then
        Assert.assertNotNull("Should have returned the truststore certificate from Merlin", certificateFromTrustStore);
    }

    @Test
    public void testBackupTruststore() throws IOException {
        String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
        String TEST_KEYSTORE = "testkeystore.jks";
        File testFile = new File(RESOURCE_PATH + TEST_KEYSTORE);

        domainCryptoService.backupTrustStore(testFile);

        new Verifications() {{
            backupService.backupFile(testFile);
            times = 1;
        }};
    }

    @Test
    public void testBackupTruststore_shouldNotBackupMissingFile() throws IOException {
        String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
        String TEST_KEYSTORE = "inexistent_testkeystore.jks";
        File testFile = new File(RESOURCE_PATH + TEST_KEYSTORE);

        domainCryptoService.backupTrustStore(testFile);

        new Verifications() {{
            backupService.backupFile((File) any);
            times = 0;
        }};
    }

    @Test
    public void refreshTrustStore(@Injectable KeyStore trustStore) {
        // Given
        new Expectations(domainCryptoService) {{
            domainCryptoService.loadTrustStore();
            result = trustStore;
            domainCryptoService.setTrustStore(trustStore);
        }};

        // When
        domainCryptoService.refreshTrustStore();

        new Verifications() {{
            KeyStore res = domainCryptoService.loadTrustStore();
            domainCryptoService.setTrustStore(res);
        }};
    }

    @Test
    public void replaceTrustStore(@Mocked KeyStore trustStore,
                                  @Mocked ByteArrayOutputStream oldTrustStoreBytes,
                                  @Mocked ByteArrayInputStream newTrustStoreBytes)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

        byte[] store = new byte[]{0, 1, 2};
        String trustPass = "trustPass";
        String password = "pass";
        String type = "type";

        new Expectations(domainCryptoService) {{
            new ByteArrayOutputStream();
            result = oldTrustStoreBytes;
            new ByteArrayInputStream(store);
            result = newTrustStoreBytes;
            domainCryptoService.getTrustStorePassword();
            result = trustPass;
            domainCryptoService.getTruststore();
            result = trustStore;
            trustStore.store(oldTrustStoreBytes, trustPass.toCharArray());
            domainCryptoService.getTrustStoreType();
            result = type;
            certificateService.validateLoadOperation(newTrustStoreBytes, password, type);
            trustStore.load(newTrustStoreBytes, password.toCharArray());
            domainCryptoService.persistTrustStore();
        }};

        domainCryptoService.replaceTrustStore(store, password);

        new Verifications() {{
            trustStore.store(oldTrustStoreBytes, trustPass.toCharArray());
            certificateService.validateLoadOperation(newTrustStoreBytes, password, type);
            domainCryptoService.getTruststore().load(newTrustStoreBytes, password.toCharArray());
            domainCryptoService.persistTrustStore();
        }};
    }

    @Test
    public void persistTrustStore(@Mocked KeyStore trustStore, @Injectable FileOutputStream fileOutputStream)
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {

        String trustStoreLocation = "trustLoc";
        String trustPass = "trustPass";
        File trustStoreFile = Mockito.mock(File.class);
        Mockito.when(trustStoreFile.exists()).thenReturn(true);

        new Expectations(domainCryptoService) {{
            domainCryptoService.getTrustStoreLocation();
            result = trustStoreLocation;
            domainCryptoService.createFileWithLocation(trustStoreLocation);
            result = trustStoreFile;
            domainCryptoService.parentFileExists(trustStoreFile);
            result = true;
            domainCryptoService.backupTrustStore(trustStoreFile);
            domainCryptoService.createFileOutputStream(trustStoreFile);
            result = fileOutputStream;
            domainCryptoService.getTrustStorePassword();
            result = trustPass;
            domainCryptoService.getTruststore();
            result = trustStore;
            trustStore.store(fileOutputStream, trustPass.toCharArray());
            domainCryptoService.signalTrustStoreUpdate();
        }};

        domainCryptoService.persistTrustStore();

        new Verifications() {{
            domainCryptoService.backupTrustStore(trustStoreFile);
            domainCryptoService.signalTrustStoreUpdate();
        }};
    }

    @Test
    public void isCertificateChainValid() {
        String alias = "alias";
        new Expectations(domainCryptoService) {{
            KeyStore trustStore = domainCryptoService.getTrustStore();
            result = trustStore;
            certificateService.isCertificateChainValid(trustStore, alias);
            result = true;
        }};

        // When
        boolean result = domainCryptoService.isCertificateChainValid(alias);

        Assert.assertTrue(result);
    }

    @Test
    public void addCertificate(@Injectable X509Certificate certificate) {
        String alias = "alias";
        boolean overwrite = true;

        new Expectations(domainCryptoService) {{
            domainCryptoService.doAddCertificate(certificate, alias, overwrite);
            result = true;
            domainCryptoService.persistTrustStore();
        }};

        // When
        boolean result = domainCryptoService.addCertificate(certificate, alias, overwrite);

        Assert.assertTrue(result);

        new Verifications() {{
            domainCryptoService.persistTrustStore();
        }};
    }

    @Test
    public void addCertificates(@Mocked X509Certificate certificate1, @Mocked X509Certificate certificate2) {
        boolean overwrite = true;
        String alias1 = "alias1", alias2 = "alias2";
        List<CertificateEntrySpi> certificates = Arrays.asList(
                new CertificateEntrySpi(alias1, certificate1), new CertificateEntrySpi(alias2, certificate2));

        new Expectations(domainCryptoService) {{
            domainCryptoService.doAddCertificate((X509Certificate) any, anyString, overwrite);
            domainCryptoService.persistTrustStore();
        }};

        domainCryptoService.addCertificate(certificates, overwrite);

        new Verifications() {{
            domainCryptoService.doAddCertificate(certificate1, alias1, overwrite);
            domainCryptoService.doAddCertificate(certificate2, alias2, overwrite);
            domainCryptoService.persistTrustStore();
        }};
    }

    @Test
    public void removeCertificate() {
        String alias = "alias";

        new Expectations(domainCryptoService) {{
            domainCryptoService.doRemoveCertificate(alias);
            result = true;
            domainCryptoService.persistTrustStore();
        }};

        boolean result = domainCryptoService.removeCertificate(alias);

        Assert.assertTrue(result);

        new Verifications() {{
            domainCryptoService.persistTrustStore();
        }};
    }

    @Test
    public void removeCertificates() {
        String alias1 = "alias1", alias2 = "alias2";
        List<String> aliases = Arrays.asList(alias1, alias2);

        new Expectations(domainCryptoService) {{
            domainCryptoService.doRemoveCertificate(anyString);
            result = true;
            domainCryptoService.persistTrustStore();
        }};

        domainCryptoService.removeCertificate(aliases);

        new Verifications() {{
            domainCryptoService.doRemoveCertificate(alias1);
            domainCryptoService.doRemoveCertificate(alias2);
            domainCryptoService.persistTrustStore();
        }};
    }

    @Test
    public void doRemoveCertificate(@Injectable KeyStore trustStore) throws KeyStoreException {
        String alias = "alias1";

        new Expectations(domainCryptoService) {{
            domainCryptoService.getTrustStore();
            result = trustStore;
            trustStore.containsAlias(alias);
            result = true;
            trustStore.deleteEntry(alias);
        }};

        // When
        Boolean result = domainCryptoService.doRemoveCertificate(alias);

        Assert.assertTrue(result);

        new Verifications() {{
            trustStore.deleteEntry(alias);
        }};
    }

    @Test
    public void doAddCertificate(@Mocked KeyStore trustStore, @Injectable X509Certificate certificate) throws KeyStoreException {
        String alias = "alias";
        boolean overwrite = true;

        new Expectations(domainCryptoService) {{
            domainCryptoService.getTrustStore();
            result = trustStore;
            trustStore.containsAlias(alias);
            result = false;
            domainCryptoService.getTrustStore().setCertificateEntry(alias, certificate);
        }};

        boolean result = domainCryptoService.doAddCertificate(certificate, alias, overwrite);

        Assert.assertTrue(result);

        new Verifications() {{
            trustStore.deleteEntry(alias);
            times = 0;
        }};
    }

    @Test
    public void loadTrustStore(@Mocked KeyStore trustStore, @Mocked InputStream is)
            throws WSSecurityException, IOException {

        String trustStoreLocation = "trustStoreLocation";
        String trustStorePassword = "pass", decryptedPass = "pass2";
        String type = "jks";

        new Expectations(domainCryptoService) {{
            domainCryptoService.getTrustStoreLocation();
            result = trustStoreLocation;
            domainCryptoService.loadInputStream(domainCryptoService.getClass().getClassLoader(), trustStoreLocation);
            result = is;
            domainCryptoService.getTrustStorePassword();
            result = trustStorePassword;
            domainCryptoService.doDecriptPassword(trustStorePassword);
            result = decryptedPass;
            domainCryptoService.getTrustStoreType();
            result = type;
            domainCryptoService.doLoad(is, decryptedPass, type);
            result = trustStore;
        }};

        KeyStore result = domainCryptoService.loadTrustStore();

        Assert.assertEquals(trustStore, result);
    }
}