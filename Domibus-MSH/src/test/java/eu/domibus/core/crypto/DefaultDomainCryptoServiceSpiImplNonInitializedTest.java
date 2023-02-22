package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.certificate.CertificateHelper;
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

    @Injectable
    CertificateHelper certificateHelper;

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

}
