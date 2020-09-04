package eu.domibus.core.crypto.spi.dss;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.policy.ValidationPolicyFacade;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.tsl.TSLValidationResult;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.xades.XPathQueryHolder;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidator;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class copied from dss to inject a spring bean certificate verifier.
 */
public class DomibusTSLValidator implements Callable<TSLValidationResult> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusTSLValidator.class);

    private final DSSDocument trustedList;

    private final String countryCode;

    private final List<CertificateToken> potentialSigners;

    private CertificateVerifier certificateVerifier;

    /**
     * Constructor used to instantiate a validator for a TSL
     *
     * @param trustedList         the DSSDocument with a trusted list (not LOTL)
     * @param countryCode         the country code
     * @param potentialSigners
     * @param certificateVerifier certificate verifier prototype
     */
    public DomibusTSLValidator(
            DSSDocument trustedList,
            String countryCode,
            List<CertificateToken> potentialSigners,
            CertificateVerifier certificateVerifier) {
        this.trustedList = trustedList;
        this.countryCode = countryCode;
        this.potentialSigners = potentialSigners;
        this.certificateVerifier = certificateVerifier;
    }

    @Override
    public TSLValidationResult call() throws Exception {
        certificateVerifier.setTrustedCertSource(buildTrustedCertificateSource(potentialSigners));

        XMLDocumentValidator xmlDocumentValidator = new XMLDocumentValidator(trustedList);
        xmlDocumentValidator.setCertificateVerifier(certificateVerifier);
        xmlDocumentValidator.setValidationLevel(ValidationLevel.BASIC_SIGNATURES); // Timestamps,... are ignored
        // To increase the security: the default {@code XPathQueryHolder} is
        // used.
        List<XPathQueryHolder> xPathQueryHolders = xmlDocumentValidator.getXPathQueryHolder();
        xPathQueryHolders.clear();
        xPathQueryHolders.add(new XPathQueryHolder());

        Reports reports = xmlDocumentValidator.validateDocument(ValidationPolicyFacade.newFacade().getTrustedListValidationPolicy());

        SimpleReport simpleReport = reports.getSimpleReport();
        Indication indication = simpleReport.getIndication(simpleReport.getFirstSignatureId());
        boolean isValid = Indication.TOTAL_PASSED.equals(indication);

        TSLValidationResult result = new TSLValidationResult();
        result.setCountryCode(countryCode);
        result.setIndication(indication);
        result.setSubIndication(simpleReport.getSubIndication(simpleReport.getFirstSignatureId()));

        if (!isValid) {
            LOG.info("The TSL signature is not valid : {}", reports.getXmlSimpleReport());
        }

        return result;
    }

    private CommonTrustedCertificateSource buildTrustedCertificateSource(List<CertificateToken> potentialSigners) {
        CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource();
        if (Utils.isCollectionNotEmpty(potentialSigners)) {
            for (CertificateToken potentialSigner : potentialSigners) {
                commonTrustedCertificateSource.addCertificate(potentialSigner);
            }
        }
        return commonTrustedCertificateSource;
    }
}
