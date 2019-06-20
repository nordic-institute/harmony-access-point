package eu.domibus.core.crypto.spi.dss;

import com.google.common.collect.Lists;
import eu.domibus.core.crypto.spi.AbstractCryptoServiceSpi;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.europa.esig.dss.jaxb.detailedreport.DetailedReport;
import eu.europa.esig.dss.tsl.TLInfo;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.x509.CertificateSource;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonCertificateSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Dss implementation to verify the trust of incoming certificates.
 * This class is within external module and is only loaded if the dss module is added
 * in the directory ${domibus.config.location}/extensions/lib/
 */
public class DomibusDssCryptoSpi extends AbstractCryptoServiceSpi {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusDssCryptoSpi.class);

    private static final String CERTPATH = "certpath";

    private CertificateVerifier certificateVerifier;

    private TSLRepository tslRepository;

    private ValidationReport validationReport;

    private ValidationConstraintPropertyMapper constraintMapper;

    public DomibusDssCryptoSpi(
            final DomainCryptoServiceSpi defaultDomainCryptoService,
            final CertificateVerifier certificateVerifier,
            final TSLRepository tslRepository,
            final ValidationReport validationReport,
            final ValidationConstraintPropertyMapper constraintMapper
    ) {
        super(defaultDomainCryptoService);
        this.certificateVerifier = certificateVerifier;
        this.tslRepository = tslRepository;
        this.validationReport = validationReport;
        this.constraintMapper = constraintMapper;
    }

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        //display some trusted list information.
        logDebugTslInfo();
        //should receive at least two certificates.
        if (ArrayUtils.isEmpty(certs)) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, CERTPATH, new Object[]{"Certificate chain expected with a minimum size of 1 but is empty"});
        }
        final X509Certificate leafCertificate = getX509LeafCertificate(certs);
        //add signing certificate to DSS.
        CertificateSource adjunctCertSource = prepareCertificateSource(certs, leafCertificate);
        certificateVerifier.setAdjunctCertSource(adjunctCertSource);
        LOG.debug("Leaf certificate:[{}] to be validated by dss", leafCertificate.getSubjectDN().getName());
        //add leaf certificate to DSS
        CertificateValidator certificateValidator = prepareCertificateValidator(leafCertificate);
        //Validate.
        validate(certificateValidator);
        LOG.debug("Certificate:[{}] passed DSS trust validation:", leafCertificate.getSubjectDN());
    }

    protected void validate(CertificateValidator certificateValidator) throws WSSecurityException {
        CertificateReports reports = certificateValidator.validate();
        LOG.debug("Detail report:[{}]", reports.getXmlDetailedReport());
        LOG.debug("Simple report:[{}]", reports.getXmlSimpleReport());
        LOG.debug("Diagnostic data:[{}]", reports.getXmlDiagnosticData());
        final DetailedReport detailedReport = reports.getDetailedReportJaxb();
        final List<ConstraintInternal> constraints = constraintMapper.map();
        final boolean valid = validationReport.isValid(detailedReport, constraints);
        if (!valid) {
            LOG.error("Dss triggered and error while validating the certificate chain:[{}]", reports.getXmlSimpleReport());
            throw new WSSecurityException(
                    WSSecurityException.ErrorCode.FAILURE, new CertPathValidatorException("Path does not chain with any of the trust anchors", null, null, -1, PKIXReason.NO_TRUST_ANCHOR), "certpath"
            );
        }
        LOG.trace("Incoming message certificate chain has been validated by DSS.");
    }

    protected CertificateValidator prepareCertificateValidator(X509Certificate leafCertificate) {
        CertificateValidator certificateValidator = CertificateValidator.fromCertificate(new CertificateToken(leafCertificate));
        certificateValidator.setCertificateVerifier(certificateVerifier);
        certificateValidator.setValidationTime(new Date(System.currentTimeMillis()));
        return certificateValidator;
    }


    protected CertificateSource prepareCertificateSource(X509Certificate[] certs, X509Certificate leafCertificate) {
        LOG.debug("Setting up DSS with trust chain");
        final List<X509Certificate> trustChain = Lists.newArrayList(Arrays.asList(certs));
        trustChain.remove(leafCertificate);
        CertificateSource adjunctCertSource = new CommonCertificateSource();
        for (X509Certificate x509TrustCertificate : trustChain) {
            CertificateToken certificateToken = new CertificateToken(x509TrustCertificate);
            adjunctCertSource.addCertificate(certificateToken);
            LOG.debug("Trust certificate:[{}] added to DSS", x509TrustCertificate.getSubjectDN().getName());
        }
        return adjunctCertSource;
    }

    protected X509Certificate getX509LeafCertificate(X509Certificate[] certs) throws WSSecurityException {
        LOG.debug("Getting leaf certificate out of a list of certificate with size:[{}]", certs.length);
        if (certs.length == 1) {
            return certs[0];
        }
        final List<X509Certificate> leafCertificate = Arrays.stream(certs).
                filter(x509Certificate -> x509Certificate.getBasicConstraints() == -1).collect(Collectors.toList());
        if (leafCertificate.size() != 1) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, CERTPATH, new Object[]{"Invalid leaf certificate"});
        }
        return leafCertificate.get(0);
    }

    protected void logDebugTslInfo() {
        if (LOG.isDebugEnabled()) {
            final Map<String, TLInfo> summary = tslRepository.getSummary();
            for (Map.Entry<String, TLInfo> stringTLInfoEntry : summary.entrySet()) {
                LOG.debug("Key:[{}], info:[{}]", stringTLInfoEntry.getKey(), stringTLInfoEntry.getValue());
            }
        }
    }


    @Override
    public String getIdentifier() {
        return "DSS_AUTHENTICATION_SPI";
    }
}
