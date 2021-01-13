package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.AbstractCryptoServiceSpi;
import eu.domibus.core.util.backup.BackupService;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.security.cert.X509Certificate;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class DefaultDomainCryptoEbms3ServiceSpiImplTest {
    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";
    private static final String KEYSTORE_TYPE = "keystoreType";
    private static final String KEYSTORE_PASSWORD = "keystorePassword";
    private static final String PRIVATE_KEY_ALIAS = "privateKeyAlias";
    private static final String KEYSTORE_LOCATION = "keystoreLocation";
    private static final String TRUST_STORE_LOCATION = "trustStoreLocation";
    private static final String TRUST_STORE_PASSWORD = "trustStorePassword";
    private static final String TRUST_STORE_TYPE = "trustStoreType";

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

    @Injectable
    private BackupService backupService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_TYPE);
            result = KEYSTORE_TYPE;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_PASSWORD);
            result = KEYSTORE_PASSWORD;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS);
            result = PRIVATE_KEY_ALIAS;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD);
            result = PRIVATE_KEY_PASSWORD;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEYSTORE_LOCATION);
            result = KEYSTORE_LOCATION;

            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
            result = TRUST_STORE_LOCATION;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
            result = TRUST_STORE_PASSWORD;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
            result = TRUST_STORE_TYPE;
        }};
    }

    @Test
    public void getIdentifier() {
        // When
        String identifier = domainCryptoService.getIdentifier();

        // Then
        Assert.assertEquals(AbstractCryptoServiceSpi.DEFAULT_AUTHENTICATION_SPI, identifier);
    }

    @Test
    public void returnsPrivateKeyPasswordAsTheValueOfThePropertyDefinedInTheCurrentDomain() {
        // Given
        String alias = "alias";

        // When
        String privateKeyPassword = domainCryptoService.getPrivateKeyPassword(alias);

        // Then
        Assert.assertEquals("Should have returned the correct private key password", PRIVATE_KEY_PASSWORD, privateKeyPassword);
    }

    @Test
    public void getKeystoreLocation() {
        // When
        String location = domainCryptoService.getKeystoreLocation();

        // Then
        Assert.assertEquals(KEYSTORE_LOCATION, location);
    }

    @Test
    public void getPrivateKeyAlias() {
        // When
        String privateKeyAlias = domainCryptoService.getPrivateKeyAlias();

        // Then
        Assert.assertEquals(PRIVATE_KEY_ALIAS, privateKeyAlias);
    }

    @Test
    public void getKeystorePassword() {
        // When
        String keystorePassword = domainCryptoService.getKeystorePassword();

        // Then
        Assert.assertEquals(KEYSTORE_PASSWORD, keystorePassword);
    }

    @Test
    public void getKeystoreType() {
        // When
        String keystoreType = domainCryptoService.getKeystoreType();

        // Then
        Assert.assertEquals(KEYSTORE_TYPE, keystoreType);
    }

    @Test
    public void getTrustStoreLocation() {
        // When
        String trustStoreLocation = domainCryptoService.getTrustStoreLocation();

        // Then
        Assert.assertEquals(TRUST_STORE_LOCATION, trustStoreLocation);
    }

    @Test
    public void getTrustStorePassword() {
        // When
        String trustStorePassword = domainCryptoService.getTrustStorePassword();

        // Then
        Assert.assertEquals(TRUST_STORE_PASSWORD, trustStorePassword);
    }

    @Test
    public void getTrustStoreType() {
        // When
        String trustStoreType = domainCryptoService.getTrustStoreType();

        // Then
        Assert.assertEquals(TRUST_STORE_TYPE, trustStoreType);
    }

}