package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Action;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Service;
import eu.domibus.core.ebms3.sender.retry.RetryStrategy;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Validates that all leg configurations of a pMode have service, action, security, etc
 */
@Component
@Order(3)
public class LegConfigurationValidator implements PModeValidator {

    private final PModeValidationHelper pModeValidationHelper;

    Pattern actionPattern, serviceValuePattern, serviceTypePattern;

    public LegConfigurationValidator(PModeValidationHelper pModeValidationHelper, DomibusPropertyProvider domibusPropertyProvider) {
        this.pModeValidationHelper = pModeValidationHelper;

        String actionPatternString = domibusPropertyProvider.getProperty(DOMIBUS_PMODE_VALIDATION_ACTION_PATTERN);
        if (StringUtils.isNotBlank(actionPatternString)) {
            actionPattern = Pattern.compile(actionPatternString);
        }

        String serviceValuePatternString = domibusPropertyProvider.getProperty(DOMIBUS_PMODE_VALIDATION_SERVICE_VALUE_PATTERN);
        if (StringUtils.isNotBlank(serviceValuePatternString)) {
            serviceValuePattern = Pattern.compile(serviceValuePatternString);
        }

        String serviceTypePatternString = domibusPropertyProvider.getProperty(DOMIBUS_PMODE_VALIDATION_SERVICE_TYPE_PATTERN);
        if (StringUtils.isNotBlank(serviceTypePatternString)) {
            serviceTypePattern = Pattern.compile(serviceTypePatternString);
        }
    }

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

        return issues.stream().filter(Objects::nonNull).collect(Collectors.toList());
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
        if (leg.getReceptionAwareness().getRetryTimeout() > 0 && leg.getReceptionAwareness().getRetryCount() <= 0 && leg.getReceptionAwareness().getStrategy() != RetryStrategy.PROGRESSIVE) {
            String name = pModeValidationHelper.getAttributeValue(leg.getReceptionAwareness(), "retryXml", String.class);
            return createIssue(leg, name, "Retry strategy [%s] of leg configuration [%s] not accepted.");
        }

        if (leg.getReceptionAwareness().getStrategy() == RetryStrategy.PROGRESSIVE) {
            // PROGRESSIVE strategy validations:
            int retryTimeout = leg.getReceptionAwareness().getRetryTimeout();
            int initialInterval = leg.getReceptionAwareness().getInitialInterval();
            int multiplyingFactor = leg.getReceptionAwareness().getMultiplyingFactor();
            String name = pModeValidationHelper.getAttributeValue(leg.getReceptionAwareness(), "retryXml", String.class);

            if (retryTimeout < initialInterval) {
                return createIssue(leg, name, "Retry strategy [%s] of leg configuration [%s] not accepted (initialInterval should be less than retryTimeout).");
            }
            if (multiplyingFactor < 1) {
                return createIssue(leg, name, "Retry strategy [%s] of leg configuration [%s] not accepted (multiplyingFactor should be greater than 1).");
            }
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
        Action action = leg.getAction();
        if (action == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "actionXml", String.class);
            return createIssue(leg, name, "Action [%s] of leg configuration [%s] not found in business process actions.");
        }

        if (!matchesPattern(action.getValue(), actionPattern)) {
            String name = action.getName();
            return createIssue(leg, name, "The value of action [%s] of leg configuration [%s] does not conform to the required action pattern.");
        }

        return null;
    }

    protected ValidationIssue validateLegService(LegConfiguration leg) {
        Service service = leg.getService();
        if (service == null) {
            String name = pModeValidationHelper.getAttributeValue(leg, "serviceXml", String.class);
            return createIssue(leg, name, "Service [%s] of leg configuration [%s] not found in business process services.");
        }

        if (!matchesPattern(service.getServiceType(), serviceTypePattern)) {
            String name = service.getName();
            return createIssue(leg, name, "The type of service [%s] of leg configuration [%s] does not conform to the required action pattern.");
        }

        if (!matchesPattern(service.getValue(), serviceValuePattern))  {
            String name = service.getName();
            return createIssue(leg, name, "The value of service [%s] of leg configuration [%s] does not conform to the required action pattern.");
        }

        return null;
    }

    private boolean matchesPattern(String value, Pattern pattern) {
        if (value == null || pattern == null) {
            return true;
        }
        return pattern.matcher(value).matches();
    }

    protected ValidationIssue createIssue(LegConfiguration leg, String name, String message) {
        return pModeValidationHelper.createValidationIssue(message, name, leg.getName());
    }
}
