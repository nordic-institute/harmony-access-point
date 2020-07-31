package eu.domibus.core.certificate.crl;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.pki.PKIUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Cosmin Baciu on 07-Jul-16.
 */
@RunWith(JMockit.class)
public class CRLServiceImplTest {

    @Tested
    CRLServiceImpl crlService;

    @Injectable
    CRLUtil crlUtil;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    X509CRL x509CRL;

    @Injectable
    DomibusCacheService domibusCacheService;

    PKIUtil pkiUtil = new PKIUtil();

    @Before
    public void init() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    //    @Test
    public void testCreateCertificate() throws Exception {
        BigInteger serial = new BigInteger("0400000000011E44A5E404", 16);
        X509Certificate certificate = pkiUtil.createCertificate(serial, Arrays.asList("test.crl", "test1.crl"));
        System.out.println(certificate);
        FileUtils.writeByteArrayToFile(new File("c:\\work\\certificates_self_signed\\mycertificate.cer"), certificate.getEncoded());
    }

    //    @Test
    public void testGenerateCRL() throws Exception {
        X509CRL crl = pkiUtil.createCRL(Arrays.asList(new BigInteger[]{new BigInteger("0400000000011E44A5E405", 16), new BigInteger("0400000000011E44A5E404", 16)}));
        FileUtils.writeByteArrayToFile(new File("c:\\work\\certificates_self_signed\\mycrl.crl"), crl.getEncoded());
    }

    @Test
    public void testIsCertificateRevoked(@Injectable final X509Certificate certificate) throws Exception {
        BigInteger serial = new BigInteger("0400000000011E44A5E404", 16);
        final String crlUrl1 = "http://domain1.crl";
        final List<String> crlUrlList = Arrays.asList(crlUrl1);

        //stubbing static method
        new MockUp<CRLUrlType>() {
            @Mock
            boolean isURLSupported(final String crlURL) {
                return true;
            }
        };

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList, crlUrlList);

            crlService.isCertificateRevoked(certificate, crlUrl1);
            returns(false, true);
        }};
        //certificate is valid
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);

        //certificate is revoked
        certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertTrue(certificateRevoked);
    }

    @Test
    public void testIsCertificateRevokedWithNotSupportedCRLURLs(@Injectable final X509Certificate certificate) {
        final String crlUrl1 = "ldap2://domain1.crl";
        final String crlUrl2 = "ldap2://domain2.crl";
        final List<String> crlUrlList = Arrays.asList(crlUrl1, crlUrl2);

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);
        }};
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);
    }

    @Test
    public void testIsCertificateRevokedWithAllProtocolsExcluded(@Injectable final X509Certificate certificate) {
        final String crlUrl1 = "ftp://domain1.crl"; // excluded
        final String crlUrl2 = "ldap2://domain2.crl"; // unknown
        final List<String> crlUrlList = Arrays.asList(crlUrl1, crlUrl2);

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);

            domibusPropertyProvider.getProperty(CRLServiceImpl.CRL_EXCLUDED_PROTOCOLS);
            returns("ftp,http");
        }};
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);
    }

    @Test
    public void testIsCertificateRevokedWithEmptySupportedCrlDistributionPoints(@Injectable final X509Certificate certificate) {
        new Expectations(crlService) {{
            final String crlUrl1 = "http://domain1.crl"; // excluded
            final String crlUrl2 = "ldap://domain2.crl"; // excluded
            final List<String> crlUrlList = Arrays.asList(crlUrl1, crlUrl2);
            crlUtil.getCrlDistributionPoints(certificate);
            result = crlUrlList;
            crlService.getSupportedCrlDistributionPoints(crlUrlList);
            result = new ArrayList<>();
        }};
        //when
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        //then
        assertFalse("No supported CRL distribution point found for certificate ", certificateRevoked);
    }

    @Test(expected = DomibusCRLException.class)
    public void testIsCertificateRevokedWithCRLNotDownloaded(@Injectable final X509Certificate certificate) throws Exception {
        final String crlUrl1 = "ftp://domain1.crl";
        final List<String> crlUrlList = Arrays.asList(crlUrl1);

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);

            crlUtil.downloadCRL(crlUrl1);
            result = new DomibusCRLException();
        }};
        crlService.isCertificateRevoked(certificate);
    }

    @Test
    public void testIsCertificateRevokedWithSomeProtocolsExcluded(@Injectable final X509Certificate certificate) throws Exception {
        final String crlUrl1 = "ftp://domain1.crl";
        final String crlUrl2 = "http://domain2.crl";
        final List<String> crlUrlList = Arrays.asList(crlUrl1, crlUrl2);

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);

            domibusPropertyProvider.getProperty(CRLServiceImpl.CRL_EXCLUDED_PROTOCOLS);
            returns("ftp");
        }};

        crlService.isCertificateRevoked(certificate);

        new Verifications() {{
            crlService.isCertificateRevoked(certificate, crlUrl1);
            times = 0;

            crlService.isCertificateRevoked(certificate, crlUrl2);
            times = 1;
        }};
    }

    @Test
    public void testIsCertificateRevokedWhenCertificateHasNoCRLURLs(@Injectable final X509Certificate certificate) throws Exception {
        final List<String> crlUrlList = new ArrayList<>();

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);
        }};
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);
    }

    @Test
    public void testIsCertificateRevokedWithCRLExtractedFromCertificate() throws Exception {
        final String crlUrlString = "file://test";
        final X509CRL x509CRL = pkiUtil.createCRL(Arrays.asList(new BigInteger[]{new BigInteger("0400000000011E44A5E405", 16), new BigInteger("0400000000011E44A5E404", 16)}));
        new Expectations() {{
            crlUtil.downloadCRL(crlUrlString);
            result = x509CRL;
        }};
        X509Certificate certificate = pkiUtil.createCertificate(new BigInteger("0400000000011E44A5E405", 16), null);
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate, crlUrlString);
        assertTrue(certificateRevoked);
    }

    @Test
    public void reset() {

        crlService.reset();

        new Verifications() {{
            domibusCacheService.clearCache(domibusCacheService.CRL_BY_CERT);
        }};
    }
}