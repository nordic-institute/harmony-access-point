package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Validates that all leg configurations of a pMode have service, action, security, etc
 */
@Component
@Order(3)
public class LegConfigurationValidator implements PModeValidator {

    @Autowired
    PModeValidationHelper pModeValidationHelper;

    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses().getLegConfigurations().forEach(leg -> {
            issues.addAll(validateLeg(leg));
        });

        return issues;
    }

    protected List<ValidationIssue> validateLeg(LegConfiguration leg) {
        List<ValidationIssue> issues = new ArrayList<>();

        issues.add(validateLegService(leg));
        issues.add(validateLegAction(leg));
        issues.add(validateLegSecurity(leg));
        issues.add(validateLegDefaultMpc(leg));
        issues.add(validateLegReceptionAwareness(leg));
        issues.add(validateLegReliability(leg));
        issues.add(validateLegErrorHandling(leg));
        issues.add(validateLegSplitting(leg));
        issues.add(validateLegPayloadProfile(leg));
        issues.add(validateLegPropertySet(leg));

        return issues.stream().filter(issue -> issue != null).collect(Collectors.toList());
    }

    protected ValidationIssue validateLegPropertySet(LegConfiguration leg) {
        if (leg.getPropertySet() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "propertySetXml", String.class);
            //propertySet can be null
            if (StringUtils.isNotEmpty(name)) {
                return createIssue(leg, name, "PropertySet [%s] of leg configuration [%s] not found among known propertySets.");
            }
        }
        return null;
    }

    protected ValidationIssue validateLegPayloadProfile(LegConfiguration leg) {
        if (leg.getPayloadProfile() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "payloadProfileXml", String.class);
            //payload profile can be null
            if (StringUtils.isNotEmpty(name)) {
                return createIssue(leg, name, "PayloadProfile [%s] of leg configuration [%s] not found among payload profiles.");
            }
        }
        return null;
    }

    protected ValidationIssue validateLegSplitting(LegConfiguration leg) {
        if (leg.getSplitting() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "splittingXml", String.class);
            //splitting can be null
            if (StringUtils.isNotEmpty(name)) {
                return createIssue(leg, name, "Splitting [%s] of leg configuration [%s] not found in splitting configurations.");
            }
        }
        return null;
    }

    protected ValidationIssue validateLegErrorHandling(LegConfiguration leg) {
        if (leg.getErrorHandling() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "errorHandlingXml", String.class);
            return createIssue(leg, name, "ErrorHandling [%s] of leg configuration [%s] not found in business process error handlings.");
        }
        return null;
    }

    protected ValidationIssue validateLegReliability(LegConfiguration leg) {
        if (leg.getReliability() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "reliabilityXml", String.class);
            return createIssue(leg, name, "Reliability [%s] of leg configuration [%s] not found in business process as4 reliability.");
        }
        return null;
    }

    protected ValidationIssue validateLegReceptionAwareness(LegConfiguration leg) {
        if (leg.getReceptionAwareness() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "receptionAwarenessXml", String.class);
            return createIssue(leg, name, "ReceptionAwareness [%s] of leg configuration [%s] not found in business process as4 awarness.");
        }
        return null;
    }

    protected ValidationIssue validateLegDefaultMpc(LegConfiguration leg) {
        if (leg.getDefaultMpc() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "defaultMpcXml", String.class);
            return createIssue(leg, name, "DefaultMpc [%s] of leg configuration [%s] not found in business process mpc.");
        }
        return null;
    }

    protected ValidationIssue validateLegSecurity(LegConfiguration leg) {
        if (leg.getSecurity() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "securityXml", String.class);
            return createIssue(leg, name, "Security [%s] of leg configuration [%s] not found in business process securities.");
        }
        return null;
    }

    protected ValidationIssue validateLegAction(LegConfiguration leg) {
        if (leg.getAction() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "actionXml", String.class);
            return createIssue(leg, name, "Action [%s] of leg configuration [%s] not found in business process actions.");
        }
        return null;
    }

    protected ValidationIssue validateLegService(LegConfiguration leg) {
        if (leg.getService() == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "serviceXml", String.class);
            return createIssue(leg, name, "Service [%s] of leg configuration [%s] not found in business process services.");
        }
        return null;
    }

    protected ValidationIssue createIssue(LegConfiguration leg, String name, String message) {
        return pModeValidationHelper.createValidationIssue(message, name, leg.getName());
    }
}
