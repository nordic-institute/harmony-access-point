package eu.domibus.common.util;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.core.security.ISMPCertificateValidator;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;

import javax.security.auth.x500.X500Principal;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author idragusa
 * @since 4.1
 * Provide our own certificate validator to be used by difi client for SMP certificate validation.
 * Default difi certificate validator does not have a way to configure proxy for CRL verification
 */
public class DomibusCertificateValidator extends Merlin implements CertificateValidator, ISMPCertificateValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCertificateValidator.class);
    protected CertificateService certificateService;

    private Pattern subjectRegularExpressionPattern;

    public DomibusCertificateValidator(CertificateService certificateService, KeyStore trustStore, String subjectRegularExpression) {
        this.certificateService = certificateService;

        if (!StringUtils.isEmpty(subjectRegularExpression)) {
            this.subjectRegularExpressionPattern = Pattern.compile(subjectRegularExpression);
        } else {
            this.subjectRegularExpressionPattern = Pattern.compile(".*");
        }
        // init merlin just with truststore.
        setTrustStore(trustStore);
    }

    /**
     * Method used by Peppol Dynamic discovery client for certificate verification.
     *
     * @param certificate
     * @throws CertificateException
     */

    // validate the SMP certificate
    public void validate(Service service, X509Certificate certificate) throws PeppolSecurityException {
        LOG.debug("Certificate validator for certificate: [{}]", getSubjectDN(certificate));
        try {
            validateSMPCertificate(certificate);
        } catch (CertificateException e) {
            throw new PeppolSecurityException("Lookup certificate validator failed for " + getSubjectDN(certificate), e);
        }
    }

    /**
     * Method used by OASIS Dynamic discovery client for certificate verification
     *
     * @param certificate
     * @throws CertificateException
     */
    @Override
    public void validateSMPCertificate(X509Certificate certificate) throws CertificateException {
        String subjectName = getSubjectDN(certificate);
        LOG.debug("Certificate validator for certificate: [{}]", subjectName);
        // validate
        if (!certificateService.isCertificateValid(certificate)) {
            throw new CertificateException("Lookup certificate validator failed for " + subjectName);
        }

        // is certificate  trusted
        try {
            // user merlin trust implementation to check if we trust the certificate
            // because domibus has custom proxy implementation
            // the crl list is verified separately
            verifyTrust(certificate);
        } catch (WSSecurityException ex) {
            throw new CertificateException("Certificate is not trusted: " + subjectName, ex);
        }
        // verify the chain CRL
        if (!verifyCertificateChain(certificate)){
            throw new CertificateException("Lookup certificate validator failed for " + subjectName+". The certificate chain is not valid");
        }

        LOG.debug("The Certificate is valid and trusted: [{}]", subjectName);
    }

    /**
     * Method verifies if certificate is trusted. Input for verifications are
     * truststore and subject regular expression, Method does not verify CRL
     * <p>
     * for tge chain
     *
     * @param certificate
     * @throws WSSecurityException
     */
    protected void verifyTrust(X509Certificate certificate) throws WSSecurityException {
        super.verifyTrust(new X509Certificate[]{certificate},
                false, Collections.singleton(subjectRegularExpressionPattern));
    }

    /**
     * Methods validates the certificate chain. For that to happen the certificate itself or partent/direct issuer
     * certificate must be present in truststore
     *
     * @param cert
     * @throws CertificateException
     */
    protected boolean verifyCertificateChain(X509Certificate cert) throws CertificateException {
        String subjectDN = getSubjectDN(cert);
        LOG.debug("Verify certificate chain for certificate: [{}]", subjectDN);
        String alias;
        try {
            alias = getTrustStore().getCertificateAlias(cert);
            if (StringUtils.isEmpty(alias)) {
                LOG.debug("Certificate with subject [{}] was not found in truststore. Search for the Issuer alias.", subjectDN);
                alias = getParentAliasFromTruststore(cert);
            }
        } catch (KeyStoreException e) {
            throw new CertificateException("Error occurred while reading the truststore for " + getSubjectDN(cert), e);
        }
        LOG.debug("Verify certificate chain for certificate: [{}] starting with alias [{}].", subjectDN, alias);
        return certificateService.isCertificateChainValid(truststore, alias);
    }

    /**
     * Method returns alias for parent certificate
     *
     * @param cert
     * @return String - alias for parent certificate
     * @throws CertificateException
     * @throws KeyStoreException
     */
    public String getParentAliasFromTruststore(X509Certificate cert) throws CertificateException, KeyStoreException {

        // search certificate issuer
        String issuerString = cert.getIssuerX500Principal().getName();
        Object subjectRDN = this.createBCX509Name(issuerString);

        Enumeration<String> aliasesEnum = getTrustStore().aliases();
        while (aliasesEnum.hasMoreElements()) {
            String alias = aliasesEnum.nextElement();
            Certificate candidate = getTrustStore().getCertificate(alias);
            if (!(candidate instanceof X509Certificate)) {
                continue;
            }

            X509Certificate issuer = (X509Certificate) candidate;
            if (verifyIssuer(cert, subjectRDN, issuer)) {
                LOG.debug("Subject certificate match found using keystore alias {}", alias);
                return alias;
            }
        }
        LOG.debug("Certificate with subject [{}] not found in truststore", issuerString);
        return null;
    }

    /**
     * Method verifies if  issuer has the same subject as certificate issuer and also if
     * certificate is signed by issuer.
     *
     * @param certificate
     * @param subjectRDN
     * @param issuer
     * @throws CertificateException
     */
    protected boolean verifyIssuer(X509Certificate certificate, Object subjectRDN, X509Certificate issuer) throws CertificateException {

        String certificateName = certificate.getSubjectDN().getName();
        String issuerName = issuer.getSubjectDN().getName();

        X500Principal foundRDN = issuer.getSubjectX500Principal();
        Object issuerBCX509Name = this.createBCX509Name(foundRDN.getName());
        if (!subjectRDN.equals(issuerBCX509Name)) {
            LOG.trace("Certificate issuer [{}] does not not match [{}].", subjectRDN, issuerBCX509Name);
            return false;
        }

        if (!isSignedBy(certificate, issuer)) {
            LOG.trace("Certificate [{}] is not signed by [{}].", certificateName, issuerName);
            return false;
        }

        LOG.debug("Certificate [{}] has matching issuer [{}].", certificateName, issuerName);
        return true;
    }

    protected String getSubjectDN(X509Certificate cert) {
        if (cert != null && cert.getSubjectDN() != null) {
            return cert.getSubjectDN().getName();
        }
        return null;
    }

    /**
     * Method checks if certificate "signed" is signed by certificate 'signer'
     *
     * @param signed
     * @param signer
     * @return
     * @throws CertificateException
     */
    protected boolean isSignedBy(X509Certificate signed, X509Certificate signer) throws CertificateException {
        String certificateName = signed.getSubjectDN().getName();
        String signerName = signer.getSubjectDN().getName();
        try {
            signed.verify(signer.getPublicKey());
            return true;
        } catch (InvalidKeyException | SignatureException exc) {
            LOG.debug("Certificate [{}] is not signed by expected issuer [{}]", certificateName, signerName);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            // do not throw error and to try also other certificates
            LOG.warn("Unable to verify certificate signature [{}] for issuer certificate [{}] with reason: [{}].",
                    certificateName, signerName, ExceptionUtils.getRootCauseMessage(e));
        }
        return false;
    }
}
