package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.backup.BackupService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Properties;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.core.certificate.CertificateTestUtils.loadKeyStoreFromJKSFile;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;
import static org.apache.wss4j.common.ext.WSSecurityException.ErrorCode.SECURITY_ERROR;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
@Ignore("EDELIVERY-8892")
public class DefaultDomainCryptoServiceSpiImplTest {
    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String TEST_KEYSTORE = "testkeystore.jks";
    private static final String TEST_KEYSTORE2 = "expired_gateway_keystore.jks";
    private static final String TEST_KEYSTORE_PASSWORD = "1234";
    private static final String TEST_KEYSTORE2_PASSWORD = "test123";

    @Tested
    private DefaultDomainCryptoServiceSpiImpl domainCryptoService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected CertificateService certificateService;

    @Injectable
    protected SignalService signalService;

    @Injectable
    private X509Certificate x509Certificate;

    @Injectable
    private DomibusCoreMapper coreMapper;

    @Injectable
    private Domain domain;

    @Injectable
    private BackupService backupService;

    @Injectable
    DomainTaskExecutor domainTaskExecutor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private void commonExpectations() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_TYPE);
            result = "keystoreType";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_PASSWORD);
            result = "keystorePassword";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS);
            result = "privateKeyAlias";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD);
            result = PRIVATE_KEY_PASSWORD;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_LOCATION);
            result = "keystoreLocation";

            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
            result = "trustStoreLocation";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
            result = "trustStorePassword";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
            result = "trustStoreType";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_BACKUP_LOCATION);
            result = "trustStoreBackupLocation";
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToLoadMerlinProperties_WSSecurityException() throws WSSecurityException, IOException {
        commonExpectations();

        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error occurred when loading the properties of TrustStore");

        new Expectations(domainCryptoService) {{
            domainCryptoService.loadProperties((Properties) any, (ClassLoader) any, null);
            result = new WSSecurityException(SECURITY_ERROR);
        }};

        // When
        domainCryptoService.init();
    }

    @Test
    public void throwsExceptionWhenFailingToLoadMerlinProperties_IOException() throws WSSecurityException, IOException {
        commonExpectations();

        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error occurred when loading the properties of TrustStore");

        new Expectations(domainCryptoService) {{
            domainCryptoService.loadProperties((Properties) any, (ClassLoader) any, null);
            result = new IOException();
        }};

        // When
        domainCryptoService.init();
    }

    @Test
    public void returnsKeystoreCertificateFromMerlin(@Injectable KeyStore keyStore) throws Exception {
        // Given
        String alias = "alias";
        new Expectations(domainCryptoService) {{
            domainCryptoService.getKeyStore();
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
    public void returnsTrustStoreCertificateFromMerlin(@Injectable KeyStore trustStore) throws Exception {
        // Given
        String alias = "alias";
        new Expectations(domainCryptoService) {{
            domainCryptoService.getTrustStore();
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
    public void replaceTrustStore() {

        byte[] store = "cert content".getBytes();
        String password = "test123";

        domainCryptoService.replaceTrustStore(store, password);

        new Verifications() {{
            certificateService.replaceTrustStore(store, password, DOMIBUS_TRUSTSTORE_NAME);
        }};
    }

    @Test
    public void areKeystoresIdentical() {
        KeyStore store0 = loadKeyStoreFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, TEST_KEYSTORE_PASSWORD);
        KeyStore store1 = loadKeyStoreFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, TEST_KEYSTORE_PASSWORD);
        KeyStore store2 = loadKeyStoreFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE2, TEST_KEYSTORE2_PASSWORD);

        boolean shouldBeTrue = domainCryptoService.areKeystoresIdentical(store0, store1);
        Assert.assertTrue(shouldBeTrue);

        boolean shouldBeFalse = domainCryptoService.areKeystoresIdentical(store1, store2);
        Assert.assertFalse(shouldBeFalse);
    }

}
