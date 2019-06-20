package eu.domibus.core.crypto.spi.dss;

import com.google.common.collect.Lists;
import eu.europa.esig.dss.jaxb.detailedreport.DetailedReport;
import eu.europa.esig.dss.jaxb.detailedreport.XmlConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    private static final Logger LOG = LoggerFactory.getLogger(ValidationReport.class);
    public static final String INVALID_CONSTRAINT_NAME = "INVALID_CONSTRAINT_NAME";

    public List<String> isValid(final DetailedReport detailedReport, List<ConstraintInternal> constraints) {
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
                LOG.warn("Configured constraint:[{}] was not found in the report, therefore the validation is impossible", constraintInternal.getName());
                return Lists.newArrayList(INVALID_CONSTRAINT_NAME);
            }
            List<String> constraintsWithWrongStatus = allConstraints.stream().
                    filter(xmlConstraint ->
                            xmlConstraint.getName().getNameId().equals(constraintInternal.getName())).
                    filter(xmlConstraint -> {
                        boolean valid = xmlConstraint.getStatus().name().equals(constraintInternal.getStatus());
                        if (valid) {
                            LOG.debug("Checking status  for constraint:[{}] with status:[{}], expected status is:[{}]", xmlConstraint.getName().getNameId(), xmlConstraint.getStatus().name(), constraintInternal.getStatus());
                        } else {
                            LOG.warn("Invalid validation for constraint:[{}] with status:[{}], expected status is:[{}]", xmlConstraint.getName().getNameId(), xmlConstraint.getStatus().name(), constraintInternal.getStatus());
                        }
                        return valid;
                    }).
                    map(xmlConstraint -> xmlConstraint.getName().getNameId()).
                    collect(Collectors.toList());
            if (!constraintsWithWrongStatus.isEmpty()) {
                return constraintsWithWrongStatus;
            }
        }
        return Lists.newArrayList();
    }

}
