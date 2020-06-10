package eu.domibus.common.util;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.core.security.ISMPCertificateValidator;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;

import javax.security.auth.x500.X500Principal;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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

    public DomibusCertificateValidator(CertificateService certificateService, KeyStore trustStore, String subjectRegularExpression ) {
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
        LOG.debug("Certificate validator for certificate: [{}]", getSubjectDN(certificate));
        // validate
        if (!certificateService.isCertificateValid(certificate)) {
            throw new CertificateException("Lookup certificate validator failed for " + getSubjectDN(certificate));
        }

        // is certificate  trusted
        try {
            // user merlin trust implementation to check if we trust the certificate
            // because if the proxy issue and domibus custom proxy implementation
            // the crl lists are verified separately
            verifyTrust(certificate);
            verifyCertificateChain(certificate);
        } catch (WSSecurityException e) {
            e.printStackTrace();
        }

        LOG.debug("Certificate validator for certificate: [{}]", getSubjectDN(certificate));
    }


    protected void verifyTrust(X509Certificate certificate) throws WSSecurityException {
        super.verifyTrust(new X509Certificate[]{certificate},
                false, Collections.singleton(subjectRegularExpressionPattern));
    }

    /**
     * Methods validates the certificate chain. For that to happen the certificate itself or partent/direct issuer
     * certificate must be present in truststore
     * @param cert
     * @throws WSSecurityException
     */
    protected boolean verifyCertificateChain(X509Certificate cert) throws CertificateException {
        // get alias
        String alias = null;
        try {
            alias = getTrustStore().getCertificateAlias(cert);
            if (StringUtils.isEmpty(alias)) {
                alias = getParentAliasFromTruststore(cert);
            }
        } catch (KeyStoreException e) {
            throw new CertificateException("Error occured while retrieving alias from truststore for " + getSubjectDN(cert), e);
        }

        return certificateService.isCertificateChainValid(truststore, alias);
    }

    /**
     * Mehod returns alias for parent certificate
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
        while(aliasesEnum.hasMoreElements()) {
            String alias = aliasesEnum.nextElement();
            Certificate candidate = getTrustStore().getCertificate(alias);
            if (candidate instanceof X509Certificate) {
                X500Principal foundRDN = ((X509Certificate) candidate).getSubjectX500Principal();
                Object certName = this.createBCX509Name(foundRDN.getName());
                if (subjectRDN.equals(certName) && isSignedBy(cert,candidate ) ) {
                    LOG.debug("Subject certificate match found using keystore alias {}", alias);
                    return alias;
                }
            }
        }
        LOG.debug("Certificate with subject [{}] not found in truststore", issuerString);
        return null;
    }

    protected String getSubjectDN(X509Certificate cert) {
        if (cert != null && cert.getSubjectDN() != null) {
            return cert.getSubjectDN().getName();
        }
        return null;
    }

    /**
     * Method checks if certificate "signed" is signed by certificate 'signer'
     * @param signed
     * @param signer
     * @return
     * @throws CertificateException
     */
    protected boolean isSignedBy(Certificate signed, Certificate signer) throws CertificateException {
        try {
            signed.verify(signer.getPublicKey());
            return true;
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | java.security.SignatureException e) {
            return false;
        }
    }
}
