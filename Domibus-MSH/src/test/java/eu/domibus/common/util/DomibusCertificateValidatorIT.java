package eu.domibus.common.util;

import eu.domibus.api.pki.CertificateService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class DomibusCertificateValidatorIT {

    static {
        org.apache.xml.security.Init.init();
    }


    private static final String CERT_FILENAME_SMP = "SMP_test_certificate.crt";
    private static final String CERT_FILENAME_INTERMEDIATE = "Test_intermediate_Issuer_01.crt";
    private static final String CERT_FILENAME_ROOT_CA = "TEST_Root_CA_V01.crt";

    private static final String RESOURCE_PATH = "/eu/domibus/common/services/cert-validator/";


    @Injectable
    private CertificateService certificateService;

    @Injectable
    private KeyStore trustStore;

    @Injectable
    private String subjectRegularExpression=".*";

    @Tested
    private DomibusCertificateValidator domibusCertificateValidator;


    @Test
    public void testValidateIsPassingValidation() throws PeppolSecurityException, CertificateException {
        new MockUp<DomibusCertificateValidator>() {
            @Mock
            private void validateSMPCertificate(X509Certificate cert) {
            }

        };

        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        domibusCertificateValidator.validate(Service.SMP,certificate);

        new Verifications() {{
            domibusCertificateValidator.validateSMPCertificate(certificate);
        }};
    }

    @Test
    public void testValidateSMPCertificate() throws Exception {
        // given
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
         new MockUp<DomibusCertificateValidator>() {
            @Mock
            void verifyTrust(X509Certificate certs)  throws WSSecurityException  {}
            @Mock
            boolean verifyCertificateChain(X509Certificate cert) throws CertificateException {return true;}
        };

        new Expectations() {{
            certificateService.isCertificateValid(certificate);
            result = true;
            domibusCertificateValidator.verifyCertificateChain(certificate);
            result = true;

        }};
        // when
        domibusCertificateValidator.validateSMPCertificate(certificate);

        // then
        new Verifications() {{
            domibusCertificateValidator.verifyTrust(certificate);
            domibusCertificateValidator.verifyCertificateChain(certificate);
        }};

    }


    @Test(expected = CertificateException.class)
    public void testValidateSMPCertificateCainNotValid() throws Exception {
        // given
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        new MockUp<DomibusCertificateValidator>() {
            @Mock
            private void verifyTrust(X509Certificate certs)  throws WSSecurityException  {}
            @Mock
            private boolean verifyCertificateChain(X509Certificate certs){ return false;}
        };

        new Expectations() {{
            certificateService.isCertificateValid(certificate);
            result = true;

        }};
        // then
        domibusCertificateValidator.validateSMPCertificate(certificate);

    }

    @Test(expected = CertificateException.class)
    public void testValidateSMPCertificateNotValid() throws Exception {
        // given
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
         new Expectations() {{
            certificateService.isCertificateValid(certificate);
            result = false;

        }};
        // then
        domibusCertificateValidator.validateSMPCertificate(certificate);

    }


    @Test(expected = CertificateException.class)
    public void testValidateSMPCertificateTrustVerificationFailed() throws Exception {
        // given
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        new MockUp<DomibusCertificateValidator>() {
            @Mock
            private void verifyTrust(X509Certificate certs) throws WSSecurityException { throw new WSSecurityException(WSSecurityException.ErrorCode.SECURITY_ERROR);}
        };

        new Expectations() {{
            certificateService.isCertificateValid(certificate);
            result = true;

        }};
        // then
        domibusCertificateValidator.validateSMPCertificate(certificate);

    }

    @Test
    public void testGetParentAliasFromTruststoreOK() throws  Exception{
        // given - when -
        KeyStore trustStore = buildTruststore( CERT_FILENAME_INTERMEDIATE, CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        String alias = domibusCertificateValidator.getParentAliasFromTruststore(certificate);

        assertEquals(CERT_FILENAME_INTERMEDIATE.toLowerCase(),alias);
    }

    @Test
    public void testGetParentAliasFromTruststoreFails() throws  Exception{
        // given - when -
        KeyStore trustStore = buildTruststore(  CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        String alias = domibusCertificateValidator.getParentAliasFromTruststore(certificate);

        assertNull(alias);
    }

    @Test
    public void testIsSignedByOK() throws Exception  {
        // given
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        X509Certificate issuer = getCertificate(CERT_FILENAME_INTERMEDIATE);
        // when
        boolean isSigned = domibusCertificateValidator.isSignedBy(certificate, issuer);
        //then
        assertTrue(isSigned);
    }

    @Test
    public void testIsSignedByNotSigned() throws Exception  {
        // given
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        X509Certificate issuer = getCertificate(CERT_FILENAME_ROOT_CA);
        // when
        boolean isSigned = domibusCertificateValidator.isSignedBy(certificate, issuer);
        //then
        assertFalse(isSigned);
    }

    @Test
    public void testVerifyTrustAllCertsInKeyStoreTRUSTED() throws Exception {
        // given
        KeyStore trustStore = buildTruststore(CERT_FILENAME_SMP, CERT_FILENAME_INTERMEDIATE, CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        // when-then
        domibusCertificateValidator.verifyTrust(certificate);
    }



    @Test
    public void testVerifyTrustIssuerChainCertsInKeyStoreTRUSTED() throws Exception {
        // given
        KeyStore trustStore = buildTruststore( CERT_FILENAME_INTERMEDIATE, CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        // when-then
        domibusCertificateValidator.verifyTrust(certificate);
    }


    @Test(expected = WSSecurityException.class)
    public void testVerifyTrustIssuerChainCertsInKeyStoreRegExpNOTTRUSTED() throws Exception {
        // given
        KeyStore trustStore = buildTruststore( CERT_FILENAME_INTERMEDIATE, CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        ReflectionTestUtils.setField(domibusCertificateValidator, "subjectRegularExpressionPattern", Pattern.compile("SMP-Not-Exists.*"));
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        // when-then
        domibusCertificateValidator.verifyTrust(certificate);
    }

    @Test
    public void testVerifyTrustCertInTrustStoreTRUSTED() throws Exception {
        // given
        KeyStore trustStore = buildTruststore(CERT_FILENAME_SMP);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        // when-then
        domibusCertificateValidator.verifyTrust(certificate);

    }


    @Test(expected = WSSecurityException.class)
    public void testVerifyTrustOnlyIntermediateNOTTRUSTED() throws Exception {
        // given
        KeyStore trustStore = buildTruststore(CERT_FILENAME_INTERMEDIATE);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        // when - then
        domibusCertificateValidator.verifyTrust(certificate);
    }

    @Test(expected = WSSecurityException.class)
    public void testVerifyTrustOnlyRootCANOTTRUSTED() throws Exception {
        // given
        KeyStore trustStore = buildTruststore(CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        // when - then
        domibusCertificateValidator.verifyTrust(certificate);
    }




    private X509Certificate getCertificate(String filename) throws CertificateException {

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        return (X509Certificate) fact.generateCertificate(
                getClass().getResourceAsStream(RESOURCE_PATH + filename));

    }

    private KeyStore buildTruststore(String ... filenames) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {

        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = "123456".toCharArray();
        truststore.load(null, password);
        for (String filename: filenames) {
            X509Certificate certificate = getCertificate(filename);
            truststore.setCertificateEntry(filename, certificate);
        }
        return truststore;
    }
}