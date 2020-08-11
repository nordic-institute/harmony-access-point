package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.AbstractCryptoServiceSpi;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.ext.services.PkiExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.tsl.TLInfo;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.cglib.core.internal.Function;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Dss implementation to verify the trust of incoming certificates.
 * This class is within external module and is only loaded if the dss module is added
 * in the directory ${domibus.config.location}/extensions/lib/
 */
public class DomibusDssCryptoSpi extends AbstractCryptoServiceSpi {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusDssCryptoSpi.class);

    private static final String CERTPATH = "certpath";

    private Function<Void, CertificateVerifier> certificateVerifierFactory;

    private TSLRepository tslRepository;

    private ValidationReport validationReport;

    private ValidationConstraintPropertyMapper constraintMapper;

    private PkiExtService pkiExtService;

    private DssCache dssCache;

    public DomibusDssCryptoSpi(
            final DomainCryptoServiceSpi defaultDomainCryptoService,
            final Function<Void
                    , CertificateVerifier> certificateVerifierFactory,
            final TSLRepository tslRepository,
            final ValidationReport validationReport,
            final ValidationConstraintPropertyMapper constraintMapper,
            final PkiExtService pkiExtService,
            final DssCache dssCache) {
        super(defaultDomainCryptoService);
        this.certificateVerifierFactory = certificateVerifierFactory;
        this.tslRepository = tslRepository;
        this.validationReport = validationReport;
        this.constraintMapper = constraintMapper;
        this.pkiExtService = pkiExtService;
        this.dssCache = dssCache;
    }


    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        //display some trusted list information.
        logDebugTslInfo();
        //should receive at least one certificates.
        if (ArrayUtils.isEmpty(certs)) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, CERTPATH, new Object[]{"Certificate chain expected with a minimum size of 1 but is empty"});
        }
        StringBuilder cacheKeyBuilder = new StringBuilder();
        for (X509Certificate cert : certs) {
            cacheKeyBuilder.
                    append(cert.getSerialNumber()).
                    append(cert.getIssuerDN() != null ? cert.getIssuerDN().getName() : "");
        }
        String cacheKey = cacheKeyBuilder.toString();
        if (dssCache.isChainValid(cacheKey)) {
            LOG.debug("Certificate with cache key:[{}] validated from dss cache", dssCache);
            return;
        }
        final X509Certificate leafCertificate = getX509LeafCertificate(certs);
        //add signing certificate to DSS.
        final CertificateVerifier certificateVerifier = certificateVerifierFactory.apply(null);
        CertificateSource adjunctCertSource = prepareCertificateSource(certs, leafCertificate);
        certificateVerifier.setAdjunctCertSource(adjunctCertSource);
        LOG.debug("Leaf certificate:[{}] to be validated by dss", leafCertificate.getSubjectDN().getName());
        //add leaf certificate to DSS
        CertificateValidator certificateValidator = prepareCertificateValidator(leafCertificate, certificateVerifier);
        //Validate.
        validate(certificateValidator);
        dssCache.addToCache(cacheKey, true);
        LOG.debug("Certificate:[{}] passed DSS trust validation:", leafCertificate.getSubjectDN());
    }

    protected void validate(CertificateValidator certificateValidator) throws WSSecurityException {
        LOG.trace("Validate certificate");
        CertificateReports reports = certificateValidator.validate();
        LOG.trace("Validate extract constraint mapper");
        final List<ConstraintInternal> constraints = constraintMapper.map();
        LOG.trace("Analysing certificate reports.");
        List<String> invalidConstraints = validationReport.extractInvalidConstraints(reports, constraints);
        if (!invalidConstraints.isEmpty()) {
            LOG.error("Dss triggered and error while validating the certificate chain:[{}]", reports.getXmlSimpleReport());
            validationReport.checkConstraint(invalidConstraints);
        }
        LOG.trace("Incoming message certificate chain has been validated by DSS.");
    }

    protected CertificateValidator prepareCertificateValidator(X509Certificate leafCertificate, CertificateVerifier certificateVerifier) {
        CertificateValidator certificateValidator = CertificateValidator.fromCertificate(new CertificateToken(leafCertificate));
        certificateValidator.setCertificateVerifier(certificateVerifier);
        certificateValidator.setValidationTime(new Date(System.currentTimeMillis()));
        return certificateValidator;
    }


    protected CertificateSource prepareCertificateSource(X509Certificate[] certs, X509Certificate leafCertificate) {
        LOG.debug("Setting up DSS with trust chain");
        final List<X509Certificate> trustChain = new ArrayList<>(Arrays.asList(certs));
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
        Certificate certificate = pkiExtService.extractLeafCertificateFromChain(Arrays.asList(certs));
        if (certificate == null) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, CERTPATH, new Object[]{"Invalid leaf certificate"});
        }
        return (X509Certificate) certificate;
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
