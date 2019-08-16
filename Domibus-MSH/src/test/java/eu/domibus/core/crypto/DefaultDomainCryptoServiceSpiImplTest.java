package eu.domibus.core.crypto;

import com.mchange.io.FileUtils;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomainCoreConverter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Properties;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;
import static org.apache.wss4j.common.ext.WSSecurityException.ErrorCode.SECURITY_ERROR;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class DefaultDomainCryptoServiceSpiImplTest {
    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";

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
    private DomainCoreConverter coreConverter;

    @Injectable
    private Domain domain;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_TYPE);
            result = "keystoreType";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_PASSWORD, true);
            result = "keystorePassword";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS);
            result = "privateKeyAlias";
            domibusPropertyProvider.getProperty(domain, "domibus.security.key.private.password", true);
            result = PRIVATE_KEY_PASSWORD;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_LOCATION);
            result = "keystoreLocation";

            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
            result = "trustStoreLocation";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD, true);
            result = "trustStorePassword";
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
            result = "trustStoreType";
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToLoadMerlinProperties_WSSecurityException(@Mocked Merlin merlin) throws WSSecurityException, IOException {
        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error loading properties");

        new Expectations() {{
            merlin.loadProperties((Properties) any, (ClassLoader) any, null);
            result = new WSSecurityException(SECURITY_ERROR);
        }};

        // When
        domainCryptoService.init();
    }

    @Test
    public void throwsExceptionWhenFailingToLoadMerlinProperties_IOException(@Mocked Merlin merlin) throws WSSecurityException, IOException {
        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error loading properties");

        new Expectations() {{
            merlin.loadProperties((Properties) any, (ClassLoader) any, null);
            result = new IOException();
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
    public void returnsPrivateKeyPasswordAsTheValueOfThePropertyDefinedInTheCurrentDomain(@Mocked Merlin merlin) {
        // Given
        String alias = "alias";

        // When
        String privateKeyPassword = domainCryptoService.getPrivateKeyPassword(alias);

        // Then
        Assert.assertEquals("Should have returned the correct private key password", PRIVATE_KEY_PASSWORD, privateKeyPassword);
    }

    @Test
    public void testBackupTruststore() throws IOException {
        String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
        String TEST_KEYSTORE = "testkeystore.jks";
        File testFile = new File(RESOURCE_PATH + TEST_KEYSTORE);

        String backupname = domainCryptoService.backupTrustStore(testFile);

        File backupFile = new File(backupname);

        Assert.assertEquals(FileUtils.getContentsAsString(testFile), FileUtils.getContentsAsString(backupFile));
        boolean deleted = backupFile.delete();
        Assert.assertTrue(deleted);
    }
}