package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.core.pki.PKIUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import mockit.internal.expectations.argumentMatching.StringPrefixMatcher;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Pattern;

import static eu.domibus.core.pki.PKIUtil.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@RunWith(JMockit.class)
public class DomibusCertificateValidatorIT {

    static {
        org.apache.xml.security.Init.init();
    }


    private static final String CERT_FILENAME_SMP = "SMP_test_certificate.crt";
    private static final String CERT_FILENAME_INTERMEDIATE = "Test_intermediate_Issuer_01.crt";
    private static final String CERT_FILENAME_ROOT_CA = "TEST_Root_CA_V01.crt";

    private static final String RESOURCE_PATH = "/eu/domibus/common/services/cert-validator/";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Injectable
    private CertificateService certificateService;

    @Injectable
    private KeyStore trustStore;

    @Injectable
    private String subjectRegularExpression = ".*";

    @Injectable
    private List<String> allowedCertificatePolicyOIDs = Collections.emptyList();

    @Tested
    private DomibusCertificateValidator domibusCertificateValidator;

    PKIUtil pkiUtil = new PKIUtil();


    @Test
    public void testValidateIsPassingValidation() throws PeppolSecurityException, CertificateException {
        new MockUp<DomibusCertificateValidator>() {
            @Mock
            private void validateSMPCertificate(X509Certificate cert) {
            }

        };

        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        domibusCertificateValidator.validate(Service.SMP, certificate);

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
            void verifyTrust(X509Certificate certs) throws WSSecurityException {
            }

            @Mock
            boolean verifyCertificateChain(X509Certificate cert) throws CertificateException {
                return true;
            }
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
            private void verifyTrust(X509Certificate certs) throws WSSecurityException {
            }

            @Mock
            private boolean verifyCertificateChain(X509Certificate certs) {
                return false;
            }
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
            private void verifyTrust(X509Certificate certs) throws WSSecurityException {
                throw new WSSecurityException(WSSecurityException.ErrorCode.SECURITY_ERROR);
            }
        };

        new Expectations() {{
            certificateService.isCertificateValid(certificate);
            result = true;

        }};
        // then
        domibusCertificateValidator.validateSMPCertificate(certificate);

    }

    @Test
    public void testGetParentAliasFromTruststoreOK() throws Exception {
        // given - when -
        KeyStore trustStore = buildTruststore(CERT_FILENAME_INTERMEDIATE, CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        String alias = domibusCertificateValidator.getParentAliasFromTruststore(certificate);

        assertEquals(CERT_FILENAME_INTERMEDIATE.toLowerCase(), alias);
    }

    @Test
    public void testGetParentAliasFromTruststoreFails() throws Exception {
        // given - when -
        KeyStore trustStore = buildTruststore(CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);

        String alias = domibusCertificateValidator.getParentAliasFromTruststore(certificate);

        assertNull(alias);
    }

    @Test
    public void testIsSignedByOK() throws Exception {
        // given
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        X509Certificate issuer = getCertificate(CERT_FILENAME_INTERMEDIATE);
        // when
        boolean isSigned = domibusCertificateValidator.isSignedBy(certificate, issuer);
        //then
        assertTrue(isSigned);
    }

    @Test
    public void testIsSignedByNotSigned() throws Exception {
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
        KeyStore trustStore = buildTruststore(CERT_FILENAME_INTERMEDIATE, CERT_FILENAME_ROOT_CA);
        domibusCertificateValidator.setTrustStore(trustStore);
        X509Certificate certificate = getCertificate(CERT_FILENAME_SMP);
        // when-then
        domibusCertificateValidator.verifyTrust(certificate);
    }


    @Test(expected = WSSecurityException.class)
    public void testVerifyTrustIssuerChainCertsInKeyStoreRegExpNOTTRUSTED() throws Exception {
        // given
        KeyStore trustStore = buildTruststore(CERT_FILENAME_INTERMEDIATE, CERT_FILENAME_ROOT_CA);
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

    @Test
    public void testVerifyCertificatePolicyNotTrustedWrongPolicy() throws Exception {
        // given certificate policy
        List<String> allowedCertificatePolicyId = Collections.singletonList("1.3.6.1.4.1.7879.13.25");
        X509Certificate certificate = pkiUtil.createCertificate(BigInteger.TEN, null, allowedCertificatePolicyId);
        KeyStore trustStore = createTruststore(certificate);
        domibusCertificateValidator.setTrustStore(trustStore);
        new Expectations() {{
            certificateService.isCertificateValid(certificate);
            result = true;

        }};
        expectedException.expect(CertificateException.class);
        expectedException.expectMessage(startsWith("Lookup certificate validator failed for C=EU, O=eDelivery, OU=Domibus, CN=test. The certificate chain is not valid"));
        // certificate must have Certificate policy 1.3.6.1
        ReflectionTestUtils.setField(domibusCertificateValidator, "allowedCertificatePolicyOIDs", allowedCertificatePolicyId);
        // when-then
        domibusCertificateValidator.validateSMPCertificate(certificate);
    }

    @Test
    public void testVerifyCertificatePolicyNotTrustedMissingPolicy() throws Exception {
        // given certificate policy
        X509Certificate certificate = pkiUtil.createCertificate(BigInteger.TEN, null, null);
        KeyStore trustStore = createTruststore(certificate);
        domibusCertificateValidator.setTrustStore(trustStore);
        new Expectations() {{
            certificateService.isCertificateValid(certificate);
            result = true;

        }};
        expectedException.expect(CertificateException.class);
        expectedException.expectMessage(startsWith("Lookup certificate validator failed for C=EU, O=eDelivery, OU=Domibus, CN=test. The certificate chain is not valid"));
        // certificate must have Certificate policy 1.3.6.1
        ReflectionTestUtils.setField(domibusCertificateValidator, "allowedCertificatePolicyOIDs", Collections.singletonList("1.3.6.1"));
        // when-then
        domibusCertificateValidator.validateSMPCertificate(certificate);
    }

    @Test
    public void testVerifyCertificatePolicyOKLeaf() throws Exception {
        // given certificate policy
        List<String> allowedCertificatePolicyId = Collections.singletonList("1.3.6.1.4.1.7879.13.25");
        X509Certificate certificate = pkiUtil.createCertificate(BigInteger.TEN, null, allowedCertificatePolicyId);
        KeyStore trustStore = createTruststore(certificate);
        domibusCertificateValidator.setTrustStore(trustStore);
        new Expectations(domibusCertificateValidator) {{
            certificateService.isCertificateValid(certificate);
            result = true;

            domibusCertificateValidator.verifyCertificateChain(certificate);
            result = true;

        }};
        // certificate must have Certificate policy 1.3.6.1
        ReflectionTestUtils.setField(domibusCertificateValidator, "allowedCertificatePolicyOIDs", allowedCertificatePolicyId);
        // when-then
        domibusCertificateValidator.validateSMPCertificate(certificate);
        // no error
    }

    @Test
    public void testValidateOKWholeCertificateChainWithPolicy() throws Exception {
        // given
        Date date = Calendar.getInstance().getTime();
        // crate certificate change
        X509Certificate[] certificates = createCertificateChain(
                new String[]{"CN=CA,O=digit,C=EU", "CN=Issuer,O=digit,C=EU", "CN=Leaf,O=digit,C=EU"},
                asList(asList(CERTIFICATE_POLICY_ANY),
                        asList(CERTIFICATE_POLICY_QCP_NATURAL, CERTIFICATE_POLICY_QCP_LEGAL, CERTIFICATE_POLICY_QCP_NATURAL_QSCD, CERTIFICATE_POLICY_QCP_LEGAL_QSCD),
                        asList(CERTIFICATE_POLICY_QCP_NATURAL)),
                DateUtils.addDays(date, -1), DateUtils.addDays(date, 1));
        // create truststore with issues and CA
        KeyStore truststore = createTruststore(new X509Certificate[]{certificates[1], certificates[2]});
        domibusCertificateValidator.setTrustStore(truststore);
        // set allowed
        ReflectionTestUtils.setField(domibusCertificateValidator, "allowedCertificatePolicyOIDs", asList(CERTIFICATE_POLICY_QCP_NATURAL));
        // leaf certificate
        X509Certificate certificate = certificates[0];

        new Expectations(domibusCertificateValidator) {{
            certificateService.isCertificateValid(certificate);
            result = true;
            domibusCertificateValidator.verifyCertificateChain(certificate);
            result = true;
        }};

        //when
        domibusCertificateValidator.validateSMPCertificate(certificate);
        // then
        new FullVerifications(domibusCertificateValidator) {{
            domibusCertificateValidator.verifyTrust((X509Certificate) any);
            times = 1;
        }};

    }

    @Test
    public void testValidateOKWholeCertificateChainWithMultiplePolicies() throws Exception {
        // given
        Date date = Calendar.getInstance().getTime();
        // crate certificate change
        X509Certificate[] certificates = createCertificateChain(
                new String[]{"CN=CA,O=digit,C=EU", "CN=Issuer,O=digit,C=EU", "CN=Leaf,O=digit,C=EU"},
                asList(asList(CERTIFICATE_POLICY_ANY),
                        asList(CERTIFICATE_POLICY_QCP_NATURAL, CERTIFICATE_POLICY_QCP_LEGAL, CERTIFICATE_POLICY_QCP_NATURAL_QSCD, CERTIFICATE_POLICY_QCP_LEGAL_QSCD),
                        asList(CERTIFICATE_POLICY_QCP_NATURAL)),
                DateUtils.addDays(date, -1), DateUtils.addDays(date, 1));
        // create truststore with issues and CA
        KeyStore truststore = createTruststore(new X509Certificate[]{certificates[1], certificates[2]});
        domibusCertificateValidator.setTrustStore(truststore);
        // set allowed
        ReflectionTestUtils.setField(domibusCertificateValidator, "allowedCertificatePolicyOIDs", asList(CERTIFICATE_POLICY_QCP_NATURAL, CERTIFICATE_POLICY_QCP_LEGAL));
        // leaf certificate
        X509Certificate certificate = certificates[0];
        new Expectations(domibusCertificateValidator) {{
            certificateService.isCertificateValid(certificate);
            result = true;
            domibusCertificateValidator.verifyCertificateChain(certificate);
            result = true;
        }};

        //when
        domibusCertificateValidator.validateSMPCertificate(certificate);
        // then
        new FullVerifications(domibusCertificateValidator) {{
            domibusCertificateValidator.verifyTrust((X509Certificate) any);
            times = 1;
        }};
    }

    @Test
    public void testValidateWholeCertificateChainFailedWithWrongPolicy() throws Exception {
        // given
        Date date = Calendar.getInstance().getTime();
        // crate certificate change
        X509Certificate[] certificates = createCertificateChain(
                new String[]{"CN=CA,O=digit,C=EU", "CN=Issuer,O=digit,C=EU", "CN=Leaf,O=digit,C=EU"},
                asList(asList(CERTIFICATE_POLICY_ANY),
                        asList(CERTIFICATE_POLICY_QCP_NATURAL, CERTIFICATE_POLICY_QCP_LEGAL, CERTIFICATE_POLICY_QCP_NATURAL_QSCD, CERTIFICATE_POLICY_QCP_LEGAL_QSCD),
                        asList(CERTIFICATE_POLICY_QCP_NATURAL)),
                DateUtils.addDays(date, -1), DateUtils.addDays(date, 1));
        // create truststore with issues and CA
        KeyStore truststore = createTruststore(new X509Certificate[]{certificates[1], certificates[2]});
        domibusCertificateValidator.setTrustStore(truststore);
        // set Not allowed policy
        ReflectionTestUtils.setField(domibusCertificateValidator, "allowedCertificatePolicyOIDs", asList(CERTIFICATE_POLICY_QCP_LEGAL));
        // leaf certificate
        X509Certificate certificate = certificates[0];
        new Expectations(domibusCertificateValidator) {{
            certificateService.isCertificateValid(certificate);
            result = true;
        }};

        //when
        CertificateException exception = assertThrows(CertificateException.class, () -> domibusCertificateValidator.validateSMPCertificate(certificate));
        // then
        assertEquals("Certificate is not trusted: C=EU, O=digit, CN=Leaf", exception.getMessage());
    }


    @Test
    public void testValidateWholeCertificateChainFailedWithInvalidPolicyPath() throws Exception {
        // given
        Date date = Calendar.getInstance().getTime();
        // crate certificate change
        X509Certificate[] certificates = createCertificateChain(
                new String[]{"CN=CA,O=digit,C=EU", "CN=Issuer,O=digit,C=EU", "CN=Leaf,O=digit,C=EU"},
                asList(asList(CERTIFICATE_POLICY_ANY),
                        asList(CERTIFICATE_POLICY_QCP_LEGAL, CERTIFICATE_POLICY_QCP_NATURAL_QSCD, CERTIFICATE_POLICY_QCP_LEGAL_QSCD), // missing CERTIFICATE_POLICY_QCP_NATURAL,in issuer
                        asList(CERTIFICATE_POLICY_QCP_NATURAL)),
                DateUtils.addDays(date, -1), DateUtils.addDays(date, 1));
        // create truststore with issues and CA
        KeyStore truststore = createTruststore(new X509Certificate[]{certificates[1], certificates[2]});
        domibusCertificateValidator.setTrustStore(truststore);
        // set Not allowed policy
        ReflectionTestUtils.setField(domibusCertificateValidator, "allowedCertificatePolicyOIDs", asList(CERTIFICATE_POLICY_QCP_LEGAL));
        // leaf certificate
        X509Certificate certificate = certificates[0];

        //when
        CertificateException exception = assertThrows(CertificateException.class, () -> domibusCertificateValidator.validateSMPCertificate(certificate));
        // then
        assertEquals("Lookup certificate validator failed for C=EU, O=digit, CN=Leaf", exception.getMessage());
    }

    private X509Certificate getCertificate(String filename) throws CertificateException {

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        return (X509Certificate) fact.generateCertificate(
                getClass().getResourceAsStream(RESOURCE_PATH + filename));

    }

    private KeyStore buildTruststore(String... filenames) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {

        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = "123456".toCharArray();
        truststore.load(null, password);
        for (String filename : filenames) {
            X509Certificate certificate = getCertificate(filename);
            truststore.setCertificateEntry(filename, certificate);
        }
        return truststore;
    }
}