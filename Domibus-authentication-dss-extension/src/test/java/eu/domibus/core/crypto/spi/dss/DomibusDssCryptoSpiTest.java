package eu.domibus.core.crypto.spi.dss;

import com.google.common.collect.Lists;
import eu.domibus.core.crypto.spi.*;
import eu.domibus.core.crypto.spi.model.AuthenticationError;
import eu.domibus.core.crypto.spi.model.AuthenticationException;
import eu.domibus.ext.services.PkiExtService;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cglib.core.internal.Function;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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
                                          @Mocked Function<Void
                                                  , CertificateVerifier> certificateVerifierFactory,
                                          @Mocked TSLRepository tslRepository,
                                          @Mocked ValidationReport validationReport,
                                          @Mocked PkiExtService pkiExtService) throws WSSecurityException {
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, certificateVerifierFactory, tslRepository, validationReport, null, pkiExtService, null);
        domibusDssCryptoProvider.verifyTrust(new X509Certificate[]{}, true, null, null);
        fail("WSSecurityException expected");
    }

    @Test(expected = WSSecurityException.class)
    public void verifyTrustNoLeafCertificate(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                             @Mocked Function<Void
                                                     , CertificateVerifier> certificateVerifierFactory,
                                             @Mocked TSLRepository tslRepository,
                                             @Mocked ValidationReport validationReport,
                                             @Mocked X509Certificate noLeafCertificate,
                                             @Mocked X509Certificate chainCertificate,
                                             @Mocked PkiExtService pkiExtService,
                                             @Mocked DssCache dssCache) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {noLeafCertificate, chainCertificate};

        new Expectations() {{
            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = null;
        }};
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, certificateVerifierFactory, tslRepository, validationReport, null, pkiExtService, dssCache);
        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
        fail("WSSecurityException expected");
    }

    @Test
    public void verifyTrustNotValid(
                                    @Mocked Function<Void
                                            , CertificateVerifier> certificateVerifierFactory,
                                    @Mocked TSLRepository tslRepository,
                                    @Mocked ValidationConstraintPropertyMapper constraintMapper,
                                    @Mocked X509Certificate untrustedCertificate,
                                    @Mocked X509Certificate chainCertificate,
                                    @Mocked CertificateValidator certificateValidator,
                                    @Mocked CertificateReports reports,
                                    @Mocked PkiExtService pkiExtService,
                                    @Mocked DssCache dssCache) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {untrustedCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        ValidationReport validationReport=new ValidationReport();
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(new FakeDefaultDssCrypto(), certificateVerifierFactory, tslRepository, validationReport, constraintMapper, pkiExtService, dssCache);

        new Expectations(domibusDssCryptoProvider,validationReport) {{
            untrustedCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = untrustedCertificate;

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            certificateValidator.validate();
            result = reports;

            domibusDssCryptoProvider.prepareCertificateSource((X509Certificate[])any,(X509Certificate)any);

            validationReport.extractInvalidConstraints((CertificateReports)any,(List<ConstraintInternal>)any);
            result=Lists.newArrayList(BBB_XCV_CCCBB);

        }};

        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            fail("AuthenticationException expected");
        } catch (AuthenticationException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof WSSecurityException);
        }
    }

    @Test
    public void verifyValidityNotValid(
                                       @Mocked Function<Void
                                               , CertificateVerifier> certificateVerifierFactory,
                                       @Mocked TSLRepository tslRepository,
                                       @Mocked ValidationConstraintPropertyMapper constraintMapper,
                                       @Mocked X509Certificate invalidCertificate,
                                       @Mocked X509Certificate chainCertificate,
                                       @Mocked CertificateValidator certificateValidator,
                                       @Mocked CertificateReports reports,
                                       @Mocked PkiExtService pkiExtService,
                                       @Mocked DssCache dssCache) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {invalidCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        ValidationReport validationReport=new ValidationReport();
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(new FakeDefaultDssCrypto(), certificateVerifierFactory, tslRepository, validationReport, constraintMapper, pkiExtService, dssCache);

        new Expectations(domibusDssCryptoProvider,validationReport) {{

            invalidCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = invalidCertificate;

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            certificateValidator.validate();
            result = reports;

            domibusDssCryptoProvider.prepareCertificateSource((X509Certificate[])any,(X509Certificate)any);

            validationReport.extractInvalidConstraints((CertificateReports)any,(List<ConstraintInternal>)any);
            result=Lists.newArrayList("BBB_XCV_ICTIVRSC");

        }};
        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            fail("AuthenticationException expected");
        } catch (AuthenticationException e) {
            assertEquals(AuthenticationError.EBMS_0101, e.getAuthenticationError());
        }
    }

    @Test
    public void verifyTrustValid(
                                 @Mocked Function<Void
                                         , CertificateVerifier> certificateVerifierFactory,
                                 @Mocked TSLRepository tslRepository,
                                 @Mocked ValidationConstraintPropertyMapper constraintMapper,
                                 @Mocked X509Certificate validLeafhCertificate,
                                 @Mocked X509Certificate chainCertificate,
                                 @Mocked CertificateValidator certificateValidator,
                                 @Mocked CertificateReports reports,
                                 @Mocked PkiExtService pkiExtService,
                                 @Mocked DssCache dssCache) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {validLeafhCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        ValidationReport validationReport=new ValidationReport();
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(new FakeDefaultDssCrypto(), certificateVerifierFactory, tslRepository, validationReport, constraintMapper, pkiExtService, dssCache);

        new Expectations(domibusDssCryptoProvider,validationReport) {{

            validLeafhCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = validLeafhCertificate;

            certificateValidator.validate();
            result = reports;

            domibusDssCryptoProvider.prepareCertificateSource((X509Certificate[])any,(X509Certificate)any);

            validationReport.extractInvalidConstraints((CertificateReports)any,(List<ConstraintInternal>)any);
            result=Lists.newArrayList();

        }};

        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);

    }

    class FakeDefaultDssCrypto implements DomainCryptoServiceSpi{

        @Override
        public X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException {
            return new X509Certificate[0];
        }

        @Override
        public String getX509Identifier(X509Certificate cert) throws WSSecurityException {
            return null;
        }

        @Override
        public PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException {
            return null;
        }

        @Override
        public PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException {
            return null;
        }

        @Override
        public PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException {
            return null;
        }

        @Override
        public void verifyTrust(PublicKey publicKey) throws WSSecurityException {

        }

        @Override
        public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {

        }

        @Override
        public String getDefaultX509Identifier() throws WSSecurityException {
            return null;
        }

        @Override
        public String getPrivateKeyPassword(String alias) {
            return null;
        }

        @Override
        public void refreshTrustStore() {

        }

        @Override
        public void replaceTrustStore(byte[] store, String password) throws CryptoSpiException {

        }

        @Override
        public KeyStore getKeyStore() {
            return null;
        }

        @Override
        public KeyStore getTrustStore() {
            return null;
        }

        @Override
        public X509Certificate getCertificateFromKeyStore(String alias) throws KeyStoreException {
            return null;
        }

        @Override
        public boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException {
            return false;
        }

        @Override
        public boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
            return false;
        }

        @Override
        public void addCertificate(List<CertificateEntrySpi> certificates, boolean overwrite) {

        }

        @Override
        public X509Certificate getCertificateFromTrustStore(String alias) throws KeyStoreException {
            return null;
        }

        @Override
        public boolean removeCertificate(String alias) {
            return false;
        }

        @Override
        public void removeCertificate(List<String> aliases) {

        }

        @Override
        public String getIdentifier() {
            return null;
        }

        @Override
        public void setDomain(DomainSpi domain) {

        }

        @Override
        public void init() {

        }
    }


}