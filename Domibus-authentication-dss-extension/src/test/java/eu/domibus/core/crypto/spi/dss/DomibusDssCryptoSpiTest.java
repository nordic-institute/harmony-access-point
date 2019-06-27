package eu.domibus.core.crypto.spi.dss;

import com.google.common.collect.Lists;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.crypto.spi.model.AuthenticationError;
import eu.domibus.core.crypto.spi.model.AuthenticationException;
import eu.domibus.ext.services.PkiExtService;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.x509.CertificateToken;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.core.crypto.spi.dss.ValidationReport.BBB_XCV_CCCBB;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DomibusDssCryptoSpiTest {

    @Test(expected = WSSecurityException.class)
    public void verifyEnmtpytTrustNoChain(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                          @Mocked CertificateVerifier certificateVerifier,
                                          @Mocked TSLRepository tslRepository,
                                          @Mocked ValidationReport validationReport,
                                          @Mocked PkiExtService pkiExtService) throws WSSecurityException {
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport, null, pkiExtService);
        domibusDssCryptoProvider.verifyTrust(new X509Certificate[]{}, true, null, null);
        fail("WSSecurityException expected");
    }

    @Test(expected = WSSecurityException.class)
    public void verifyTrustNoLeafCertificate(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                             @Mocked CertificateVerifier certificateVerifier,
                                             @Mocked TSLRepository tslRepository,
                                             @Mocked ValidationReport validationReport,
                                             @Mocked X509Certificate noLeafCertificate,
                                             @Mocked X509Certificate chainCertificate,
                                             @Mocked PkiExtService pkiExtService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {noLeafCertificate, chainCertificate};

        new Expectations() {{
            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = null;
        }};
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport, null, pkiExtService);
        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
        fail("WSSecurityException expected");
    }

    @Test
    public void verifyTrustNotValid(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                    @Mocked CertificateVerifier certificateVerifier,
                                    @Mocked TSLRepository tslRepository,
                                    @Mocked ValidationConstraintPropertyMapper constraintMapper,
                                    @Mocked X509Certificate untrustedCertificate,
                                    @Mocked X509Certificate chainCertificate,
                                    @Mocked CertificateValidator certificateValidator,
                                    @Mocked CertificateReports reports,
                                    @Mocked PkiExtService pkiExtService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {untrustedCertificate, chainCertificate};
        org.apache.xml.security.Init.init();
        new Expectations() {{
            untrustedCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            chainCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = untrustedCertificate;

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            certificateValidator.validate();
            result = reports;

        }};

        MockUp<ValidationReport> validationReport = new MockUp<ValidationReport>() {
            @Mock
            public List<String> extractInvalidConstraints(final CertificateReports certificateReports, List<ConstraintInternal> constraints) {
                return Lists.newArrayList(BBB_XCV_CCCBB);
            }
        };

        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport.getMockInstance(), constraintMapper, pkiExtService);
        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            fail("AuthenticationException expected");
        } catch (AuthenticationException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof WSSecurityException);
        }
    }

    @Test
    public void verifyValidityNotValid(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                       @Mocked CertificateVerifier certificateVerifier,
                                       @Mocked TSLRepository tslRepository,
                                       @Mocked ValidationConstraintPropertyMapper constraintMapper,
                                       @Mocked X509Certificate invalidCertificate,
                                       @Mocked X509Certificate chainCertificate,
                                       @Mocked CertificateValidator certificateValidator,
                                       @Mocked CertificateReports reports,
                                       @Mocked PkiExtService pkiExtService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {invalidCertificate, chainCertificate};
        org.apache.xml.security.Init.init();
        new Expectations() {{

            invalidCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";


            chainCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = invalidCertificate;

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            certificateValidator.validate();
            result = reports;

        }};

        MockUp<ValidationReport> validationReport = new MockUp<ValidationReport>() {
            @Mock
            public List<String> extractInvalidConstraints(final CertificateReports certificateReports, List<ConstraintInternal> constraints) {
                return Lists.newArrayList("BBB_XCV_ICTIVRSC");
            }
        };

        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport.getMockInstance(), constraintMapper, pkiExtService);
        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            fail("AuthenticationException expected");
        } catch (AuthenticationException e) {
            assertEquals(AuthenticationError.EBMS_0101, e.getAuthenticationError());
        }
    }

    @Test
    public void verifyTrustValid(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                 @Mocked CertificateVerifier certificateVerifier,
                                 @Mocked TSLRepository tslRepository,
                                 @Mocked ValidationConstraintPropertyMapper constraintMapper,
                                 @Mocked X509Certificate validLeafhCertificate,
                                 @Mocked X509Certificate chainCertificate,
                                 @Mocked CertificateValidator certificateValidator,
                                 @Mocked CertificateReports reports,
                                 @Mocked PkiExtService pkiExtService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {validLeafhCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        new Expectations() {{

            validLeafhCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            chainCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = validLeafhCertificate;

            certificateValidator.validate();
            result = reports;

        }};

        MockUp<ValidationReport> validationReport = new MockUp<ValidationReport>() {
            @Mock
            public List<String> extractInvalidConstraints(final CertificateReports certificateReports, List<ConstraintInternal> constraints) {
                return Lists.newArrayList();
            }
        };
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport.getMockInstance(), constraintMapper, pkiExtService);
        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);

    }


}