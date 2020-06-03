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
            //service
            if (leg.getService() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "serviceXml", String.class);
                createIssue(issues, leg, name, "Service [%s] of leg configuration [%s] not found in business process services.");
            }

            //action
            if (leg.getAction() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "actionXml", String.class);
                createIssue(issues, leg, name, "Action [%s] of leg configuration [%s] not found in business process actions.");
            }

            //security
            if (leg.getSecurity() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "securityXml", String.class);
                createIssue(issues, leg, name, "Security [%s] of leg configuration [%s] not found in business process securities.");
            }

            //defaultMpc
            if (leg.getDefaultMpc() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "defaultMpcXml", String.class);
                createIssue(issues, leg, name, "DefaultMpc [%s] of leg configuration [%s] not found in business process mpc.");
            }

            //receptionAwareness
            if (leg.getReceptionAwareness() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "receptionAwarenessXml", String.class);
                createIssue(issues, leg, name, "ReceptionAwareness [%s] of leg configuration [%s] not found in business process as4 awarness.");
            }

            //reliability
            if (leg.getReliability() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "reliabilityXml", String.class);
                createIssue(issues, leg, name, "Reliability [%s] of leg configuration [%s] not found in business process as4 reliability.");
            }

            //errorHandling
            if (leg.getErrorHandling() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "errorHandlingXml", String.class);
                createIssue(issues, leg, name, "ErrorHandling [%s] of leg configuration [%s] not found in business process error handlings.");
            }

            //splitting
            if (leg.getSplitting() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "splittingXml", String.class);
                //splitting can be null
                if (StringUtils.isNotEmpty(name)) {
                    createIssue(issues, leg, name, "Splitting [%s] of leg configuration [%s] not found in splitting configurations.");
                }
            }

            //payload profile
            if (leg.getPayloadProfile() == null) {
                String name = pModeValidationHelper.getAttributeValue(leg, "payloadProfileXml", String.class);
                //payload profile can be null
                if (StringUtils.isNotEmpty(name)) {
                    createIssue(issues, leg, name, "PayloadProfile [%s] of leg configuration [%s] not found among payload profiles.");
                }
            }
        });

        return issues;
    }

    private void createIssue(List<ValidationIssue> issues, LegConfiguration leg, String name, String message) {
        issues.add(pModeValidationHelper.createValidationIssue(message, name, leg.getName()));
    }

}
