package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.backup.BackupService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * Tests that use a non-initialized SUT (the #init() method is intentionally stubbed out).
 *
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DefaultDomainCryptoServiceSpiImplNonInitializedTest {

    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";

    public static final String TRUST_STORE_PASSWORD = "trustStorePassword";

    public static final String TRUST_STORE_TYPE = "trustStoreType";

    public static final String TRUST_STORE_LOCATION = "trustStoreLocation";

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
    private Domain domain;

    @Injectable
    private KeyStore trustStore;

    @Injectable
    private DomibusCoreMapper coreMapper;

    @Injectable
    private BackupService backupService;

    @Injectable
    DomainTaskExecutor domainTaskExecutor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void returnsTheCorrectValidityOfTheCertificateChain() {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new Expectations() {{
            certificateService.isCertificateChainValid(trustStore, "alias");
            result = true;
        }};

        // When
        boolean valid = domainCryptoService.isCertificateChainValid("alias");

        // Then
        Assert.assertTrue("Should have correctly returned the validity of the certificate chain", valid);
    }

    @Test
    public void refreshesTheTrustStoreWithTheLoadedTrustStore(@Mocked java.util.Properties properties) {
        // Given
        new Expectations(domainCryptoService) {{
            domainCryptoService.getTrustStoreProperties();
            result = properties;

            certificateService.getTrustStore(DOMIBUS_TRUSTSTORE_NAME);
            result = trustStore;
        }};

        // When
        domainCryptoService.refreshTrustStore();

        // Then
        new Verifications() {{
            domainCryptoService.setTrustStore(trustStore);
        }};
    }

    @Test
    public void getKeystoreProperties_missingKeystoreTypePropertyConfigurationException() {
        // Given
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_TYPE);
            result = null;
        }};
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Error while trying to load the keystore properties for domain");

        // When
        domainCryptoService.getKeystoreProperties();
    }

    @Test
    public void getKeystoreProperties_missingKeystorePasswordPropertyConfigurationException() {
        // Given
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_PASSWORD);
            result = null;
        }};
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Error while trying to load the keystore properties for domain");

        // When
        domainCryptoService.getKeystoreProperties();
    }

    @Test
    public void getKeystoreProperties_missingKeystorePrivateKeyAliasPropertyConfigurationException() {
        // Given
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS);
            result = null;
        }};
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Error while trying to load the keystore properties for domain");

        // When
        domainCryptoService.getKeystoreProperties();
    }

    @Test
    public void getKeystoreProperties_missingKeystoreLocationPropertyConfigurationException() {
        // Given
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_TYPE);
            result = null;
        }};
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Error while trying to load the keystore properties for domain");

        // When
        domainCryptoService.getKeystoreProperties();
    }

    @Test
    public void getKeystoreProperties_missingTruststoreTypePropertyConfigurationException() {
        // Given
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
            result = null;
        }};
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Error while trying to load the truststore properties for domain");

        // When
        domainCryptoService.getTrustStoreProperties();
    }

    @Test
    public void getKeystoreProperties_missingTruststorePasswordPropertyConfigurationException() {
        // Given
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
            result = null;
        }};
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Error while trying to load the truststore properties for domain");

        // When
        domainCryptoService.getTrustStoreProperties();
    }

    @Test
    public void getKeystoreProperties_missingTruststoreLocationPropertyConfigurationException() {
        // Given
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
            result = null;
        }};
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Error while trying to load the truststore properties for domain");

        // When
        domainCryptoService.getTrustStoreProperties();
    }

}
