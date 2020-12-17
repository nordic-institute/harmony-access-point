package eu.domibus.core.crypto.spi;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.RegexUtil;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.pull.PullContext;
import eu.domibus.core.crypto.spi.model.AuthorizationError;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.RegexUtilImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.core.certificate.CertificateTestUtils.loadCertificateFromJKSFile;
import static org.junit.Assert.assertNotNull;

/**
 * @author idragusa
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DefaultAuthorizationServiceSpiImplTest {
    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/core/security/";
    private static final String TEST_KEYSTORE = "testauthkeystore.jks";
    private static final String TEST_TRUSTSTORE = "testauthtruststore.jks";
    private static final String ALIAS_CN_AVAILABLE = "blue_gw";
    private static final String ALIAS_TEST_AUTH = "test_auth";
    private static final String TEST_KEYSTORE_PASSWORD = "test123";


    @Tested
    DefaultAuthorizationServiceSpiImpl defaultAuthorizationServiceSpi;

    @Injectable
    private CertificateService certificateService;

    @Injectable
    MessageExchangeService messageExchangeService;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    RegexUtil regexUtil;

    @Test
    public void testGetIdentifier() {
        Assert.assertTrue(DefaultAuthorizationServiceSpiImpl.DEFAULT_IAM_AUTHORIZATION_IDENTIFIER.equals(defaultAuthorizationServiceSpi.getIdentifier()));
    }

    @Test
    public void testFormatting() {
        X509Certificate certificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);

        String excMessage = "Signing certificate and truststore certificate do not match.";
        excMessage += String.format("Truststore certificate: %s", certificate.toString());

        System.out.println(excMessage);

        String excMessage2 = String.format("Sender alias verification failed. Signing certificate CN does not contain the alias (%s): %s ", ALIAS_CN_AVAILABLE, certificate);
        System.out.println(excMessage2);

        String excMessage3 = String.format("Certificate subject [%s] does not match the regular expression configured [%s]", ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        System.out.println(excMessage3);
    }

    @Test
    public void authorizeAgainstCertificateCNMatchTestDisabled() {
        X509Certificate signingCertificate = null;
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK);
            result = false;
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstCertificateCNMatch(signingCertificate, "nobodywho");
    }

    @Test
    public void authorizeAgainstCertificateCNMatchTestNullCert() {
        X509Certificate signingCertificate = null;
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK);
            result = true;
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstCertificateCNMatch(signingCertificate, "nobodywho");
    }

    @Test(expected = AuthorizationException.class)
    public void authorizeAgainstCertificateCNMatchTestExc() {
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        ;
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK);
            result = true;
        }};
        try {
            defaultAuthorizationServiceSpi.authorizeAgainstCertificateCNMatch(signingCertificate, "nobodywho");
        } catch (AuthorizationException exc) {
            Assert.assertEquals(AuthorizationError.AUTHORIZATION_REJECTED, exc.getAuthorizationError());
            throw exc;
        }
    }

    @Test
    public void authorizeAgainstCertificateCNMatchTestOk() {
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        ;
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK);
            result = true;
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstCertificateCNMatch(signingCertificate, ALIAS_CN_AVAILABLE);
    }

    @Test
    public void authorizeAgainstCertificateSubjectExpressionTestOk() {
        RegexUtil regexUtilLocal = new RegexUtilImpl();
        String regExp = ".*TEST.EU.*";
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_TEST_AUTH, TEST_KEYSTORE_PASSWORD);
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
            result = regExp;
            regexUtil.matches(regExp, signingCertificate.getSubjectDN().getName());
            result = regexUtilLocal.matches(regExp, signingCertificate.getSubjectDN().getName());
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstCertificateSubjectExpression(signingCertificate);
    }

    @Test
    public void authorizeAgainstCertificateSubjectExpressionTestNullCert() {
        X509Certificate signingCertificate = null;

        defaultAuthorizationServiceSpi.authorizeAgainstCertificateSubjectExpression(signingCertificate);
        new Verifications() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
            times = 0;
        }};

    }

    @Test(expected = AuthorizationException.class)
    public void authorizeAgainstCertificateSubjectExpressionTestException() {
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        new Expectations() {{
            domibusPropertyProvider.getProperty( DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
            result = "TEST.EU";
        }};

        try {
            defaultAuthorizationServiceSpi.authorizeAgainstCertificateSubjectExpression(signingCertificate);
        } catch (AuthorizationException exc) {
            Assert.assertEquals(AuthorizationError.AUTHORIZATION_REJECTED, exc.getAuthorizationError());
            throw exc;
        }
    }

    @Test
    public void authorizeAgainstCertificateSubjectExpressionTestDisable() {
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        new Expectations() {{
            domibusPropertyProvider.getProperty( DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
            result = "";
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstCertificateSubjectExpression(signingCertificate);
    }

    @Test
    public void authorizeAgainstTruststoreAliasTestDisable() {
        X509Certificate signingCertificate = null;
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS);
            result = false;
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstTruststoreAlias(signingCertificate, "nobodywho");
    }

    @Test
    public void authorizeAgainstTruststoreAliasTestNullCert() {
        X509Certificate signingCertificate = null;
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS);
            result = true;
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstTruststoreAlias(signingCertificate, "nobodywho");
    }


    @Test
    public void authorizeAgainstTruststoreAliasTestOK() throws KeyStoreException {
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS);
            result = true;
            certificateService.getPartyX509CertificateFromTruststore(ALIAS_CN_AVAILABLE);
            result = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_TRUSTSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstTruststoreAlias(signingCertificate, ALIAS_CN_AVAILABLE);
    }


    @Test
    public void authorizeAgainstTruststoreAliasTestNullTruststore() throws KeyStoreException {
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS);
            result = true;
            certificateService.getPartyX509CertificateFromTruststore(ALIAS_CN_AVAILABLE);
            result = null;
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstTruststoreAlias(signingCertificate, ALIAS_CN_AVAILABLE);
    }


    @Test(expected = AuthorizationException.class)
    public void authorizeAgainstTruststoreAliasTestNotOK() throws KeyStoreException {
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_TEST_AUTH, TEST_KEYSTORE_PASSWORD);
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS);
            result = true;
            certificateService.getPartyX509CertificateFromTruststore(ALIAS_CN_AVAILABLE);
            result = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_TRUSTSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        }};

        defaultAuthorizationServiceSpi.authorizeAgainstTruststoreAlias(signingCertificate, ALIAS_CN_AVAILABLE);
    }

    @Test
    public void doAuthorizeTest() throws KeyStoreException {
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_TEST_AUTH, TEST_KEYSTORE_PASSWORD);
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS);
            result = false;
            domibusPropertyProvider.getProperty( DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
            result = "";
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK);
            result = false;
        }};

        defaultAuthorizationServiceSpi.doAuthorize(signingCertificate, ALIAS_TEST_AUTH);

        new Verifications() {{
            defaultAuthorizationServiceSpi.authorizeAgainstTruststoreAlias(signingCertificate, ALIAS_TEST_AUTH);
            times = 1;
            defaultAuthorizationServiceSpi.authorizeAgainstCertificateSubjectExpression(signingCertificate);
            times = 1;
            defaultAuthorizationServiceSpi.authorizeAgainstCertificateCNMatch(signingCertificate, ALIAS_TEST_AUTH);
            times = 1;
        }};
    }

    @Test
    public void authorizeUserMessageTest() {

        UserMessagePmodeData userMessagePmodeData = new UserMessagePmodeData("service", "action", ALIAS_TEST_AUTH);
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_TEST_AUTH, TEST_KEYSTORE_PASSWORD);
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS);
            result = false;
            domibusPropertyProvider.getProperty( DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
            result = "";
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK);
            result = false;
        }};

        defaultAuthorizationServiceSpi.authorize(null, signingCertificate, null, userMessagePmodeData);

        new Verifications() {{
            defaultAuthorizationServiceSpi.doAuthorize(signingCertificate, ALIAS_TEST_AUTH);
            times = 1;
        }};
    }

    @Test
    public void authorizePullTest() throws EbMS3Exception {
        final String testMpc = "mpc_for_test";
        String testQualifiedMpc = "qualified_mpc_for_test";
        PullRequestPmodeData pullRequestPmodeData = new PullRequestPmodeData(testMpc);
        X509Certificate signingCertificate = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_TEST_AUTH, TEST_KEYSTORE_PASSWORD);
        Process process = new Process();
        Party party = new Party();
        party.setName("initiator");
        process.addInitiator(party);
        PullContext pullContext = new PullContext(process, new Party(), testQualifiedMpc);
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS);
            result = false;
            domibusPropertyProvider.getProperty( DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
            result = "";
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK);
            result = false;
            pModeProvider.findMpcUri(testMpc);
            result = testQualifiedMpc;
            messageExchangeService.extractProcessOnMpc(testQualifiedMpc);
            result = pullContext;
        }};

        defaultAuthorizationServiceSpi.authorize(null, signingCertificate, null, pullRequestPmodeData);

        new Verifications() {{
            defaultAuthorizationServiceSpi.doAuthorize(signingCertificate, ALIAS_TEST_AUTH);
            times = 1;
        }};
    }

    @Test(expected = AuthorizationException.class)
    public void authorizePullTestInitiatorException() throws EbMS3Exception {
        final String testMpc = "mpc_for_test";
        String testQualifiedMpc = "qualified_mpc_for_test";
        PullRequestPmodeData pullRequestPmodeData = new PullRequestPmodeData(testMpc);
        Process process = new Process();
        PullContext pullContext = new PullContext(process, new Party(), testQualifiedMpc);
        new Expectations() {{
            pModeProvider.findMpcUri(testMpc);
            result = testQualifiedMpc;
            messageExchangeService.extractProcessOnMpc(testQualifiedMpc);
            result = pullContext;
        }};

        defaultAuthorizationServiceSpi.authorize(null, null, null, pullRequestPmodeData);

        new Verifications() {{
            defaultAuthorizationServiceSpi.doAuthorize(null, ALIAS_TEST_AUTH);
            times = 1;
        }};
    }

    @Test(expected = AuthorizationException.class)
    public void authorizePullTestPullContextException() throws EbMS3Exception {
        final String testMpc = "mpc_for_test";
        String testQualifiedMpc = "qualified_mpc_for_test";
        PullRequestPmodeData pullRequestPmodeData = new PullRequestPmodeData(testMpc);
        new Expectations() {{
            pModeProvider.findMpcUri(testMpc);
            result = testQualifiedMpc;
            messageExchangeService.extractProcessOnMpc(testQualifiedMpc);
            result = null;
        }};

        defaultAuthorizationServiceSpi.authorize(null, null, null, pullRequestPmodeData);

        new Verifications() {{
            defaultAuthorizationServiceSpi.doAuthorize(null, ALIAS_TEST_AUTH);
            times = 1;
        }};
    }

    @Test(expected = AuthorizationException.class)
    public void authorizePullTestNullmpc() {
        PullRequestPmodeData pullRequestPmodeData = new PullRequestPmodeData(null);
        defaultAuthorizationServiceSpi.authorize(null, null, null, pullRequestPmodeData);
    }

}
