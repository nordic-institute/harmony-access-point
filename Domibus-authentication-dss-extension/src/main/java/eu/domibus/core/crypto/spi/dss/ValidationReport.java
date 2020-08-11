package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.model.AuthenticationError;
import eu.domibus.core.crypto.spi.model.AuthenticationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.detailedreport.jaxb.XmlConstraint;
import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.stereotype.Component;

import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXReason;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * When DSS validates, it does under the form of report (etsi validation) and it is up to the user
 * to decide the level of validation needed.
 * <p>
 * This class extracts all the constraints from the report and perform a validation by comparing the result of the
 * report with the constraints configured in the property file.
 * <p>
 * The default configuration only checks the trust anchor and the validity dates.
 */
@Component
public class ValidationReport {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ValidationReport.class);

    public static final String INVALID_CONSTRAINT_NAME = "INVALID_CONSTRAINT_NAME";

    public static final String BBB_XCV_CCCBB = "BBB_XCV_CCCBB";

    public List<String> extractInvalidConstraints(final CertificateReports certificateReports, List<ConstraintInternal> constraints) {
        LOG.debug("Detail report:[{}]", certificateReports.getXmlDetailedReport());
        LOG.debug("Simple report:[{}]", certificateReports.getXmlSimpleReport());
        LOG.debug("Diagnostic data:[{}]", certificateReports.getXmlDiagnosticData());
        XmlDetailedReport detailedReport = certificateReports.getDetailedReportJaxb();
        if (constraints == null || constraints.isEmpty()) {
            throw new IllegalStateException("A minimum set of constraints should be set.");
        }
        //Load constraint from certificate element and prepare the all constraint list..
        final List<XmlConstraint> allConstraints = new ArrayList<>();
        if (detailedReport.
                getCertificate() != null) {
            allConstraints.addAll(detailedReport.
                    getCertificate().
                    getConstraint());
            //Add constraint from xmlValidationCertificateQualification
            allConstraints.addAll(detailedReport.
                    getCertificate().
                    getValidationCertificateQualification().
                    stream().
                    flatMap(xmlValidationCertificateQualification ->
                            xmlValidationCertificateQualification.getConstraint().stream()).
                    collect(Collectors.toList()));
        }
        if (detailedReport.
                getBasicBuildingBlocks() != null) {
            //Add constraint from XCV
            allConstraints.addAll(detailedReport.
                    getBasicBuildingBlocks().
                    stream().
                    filter(xmlBasicBuildingBlocks -> xmlBasicBuildingBlocks.getXCV() != null).
                    flatMap(xmlBasicBuildingBlocks -> xmlBasicBuildingBlocks.getXCV().getConstraint().stream()).
                    collect(Collectors.toList()));
            //Add constraint from Sub XCV
            allConstraints.addAll(detailedReport.
                    getBasicBuildingBlocks().
                    stream().
                    filter(xmlBasicBuildingBlocks -> xmlBasicBuildingBlocks.getXCV() != null).
                    flatMap(xmlBasicBuildingBlocks -> xmlBasicBuildingBlocks.getXCV().getSubXCV().stream()).
                    flatMap(xmlSubXCV -> xmlSubXCV.getConstraint().stream()).
                    collect(Collectors.toList()));
        }

        if (LOG.isDebugEnabled()) {
            constraints.forEach(
                    constraint -> LOG.debug("Configured constraint:[{}], status:[{}]", constraint.getName(), constraint.getStatus()));
            LOG.debug("Report constraints list:");
            allConstraints.
                    forEach(xmlConstraint -> LOG.debug("    Constraint:[{}], status:[{}]", xmlConstraint.getName().getNameId(), xmlConstraint.getStatus()));
        }
        for (ConstraintInternal constraintInternal : constraints) {
            final long count = allConstraints.stream().
                    filter(xmlConstraint -> constraintInternal.getName().equals(xmlConstraint.getName().getNameId())).count();
            if (count == 0) {
                LOG.error("Configured constraint:[{}] was not found in the report, therefore the validation is impossible", constraintInternal.getName());
                return Arrays.asList(INVALID_CONSTRAINT_NAME);
            }
            List<String> constraintsWithWrongStatus = allConstraints.stream().
                    filter(xmlConstraint ->
                            xmlConstraint.getName().getNameId().equals(constraintInternal.getName())).
                    filter(xmlConstraint -> {
                        boolean sameStatus = xmlConstraint.getStatus().name().equals(constraintInternal.getStatus());
                        if (sameStatus) {
                            LOG.debug("Checking status  for constraint:[{}] with status:[{}], expected status is:[{}]", xmlConstraint.getName().getNameId(), xmlConstraint.getStatus().name(), constraintInternal.getStatus());
                        } else {
                            LOG.warn("Invalid validation for constraint:[{}] with status:[{}], expected status is:[{}]", xmlConstraint.getName().getNameId(), xmlConstraint.getStatus().name(), constraintInternal.getStatus());
                        }
                        return !sameStatus;
                    }).
                    map(xmlConstraint -> xmlConstraint.getName().getNameId()).
                    collect(Collectors.toList());
            if (!constraintsWithWrongStatus.isEmpty()) {
                return constraintsWithWrongStatus;
            }
        }
        return Collections.emptyList();
    }

    public void checkConstraint(final List<String> constraintNames) {
        if (constraintNames.isEmpty()) {
            LOG.trace("No constraint fault to report here.");
            return;
        }
        String constraintName = constraintNames.get(0);
        switch (constraintName) {
            case ValidationReport.INVALID_CONSTRAINT_NAME:
                throw new AuthenticationException(AuthenticationError.UNKNOWN, "Invalid constraint name configured within DSS module");
            case BBB_XCV_CCCBB:
                WSSecurityException certpathException = new WSSecurityException(
                        WSSecurityException.ErrorCode.FAILURE, new CertPathValidatorException("Path does not chain with any of the trust anchors", null, null, -1, PKIXReason.NO_TRUST_ANCHOR), "certpath"
                );
                throw new AuthenticationException(certpathException);
            default:
                throw new AuthenticationException(AuthenticationError.EBMS_0101, "Certificate validation failed in DSS module");
        }


    }

}
