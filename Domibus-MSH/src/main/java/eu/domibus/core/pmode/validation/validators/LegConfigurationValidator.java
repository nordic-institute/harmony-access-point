package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Validates
 */
@Component
@Order(4)
public class LegConfigurationValidator implements PModeValidator {

    @Override
    public List<PModeIssue> validate(Configuration pMode) {
        List<PModeIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses().getLegConfigurations().forEach(leg -> {
            //service
            if (leg.getService() == null) {
                String name = getAttributeValue(leg, "serviceXml", String.class);
                createIssue(issues, leg, name, "Service [%s] of leg configuration [%s] not found in business process services.");
            }

            //action
            if (leg.getAction() == null) {
                String name = getAttributeValue(leg, "actionXml", String.class);
                createIssue(issues, leg, name, "Action [%s] of leg configuration [%s] not found in business process actions.");
            }

            //security
            if (leg.getSecurity() == null) {
                String name = getAttributeValue(leg, "securityXml", String.class);
                createIssue(issues, leg, name, "Security [%s] of leg configuration [%s] not found in business process securities.");
            }

            //defaultMpc
            if (leg.getDefaultMpc() == null) {
                String name = getAttributeValue(leg, "defaultMpcXml", String.class);
                createIssue(issues, leg, name, "DefaultMpc [%s] of leg configuration [%s] not found in business process mpc.");
            }

            //receptionAwareness
            if (leg.getReceptionAwareness() == null) {
                String name = getAttributeValue(leg, "receptionAwarenessXml", String.class);
                createIssue(issues, leg, name, "ReceptionAwareness [%s] of leg configuration [%s] not found in business process as4 awarness.");
            }

            //reliability
            if (leg.getReliability() == null) {
                String name = getAttributeValue(leg, "reliabilityXml", String.class);
                createIssue(issues, leg, name, "Reliability [%s] of leg configuration [%s] not found in business process as4 reliability.");
            }

            //errorHandling
            if (leg.getErrorHandling() == null) {
                String name = getAttributeValue(leg, "errorHandlingXml", String.class);
                createIssue(issues, leg, name, "ErrorHandling [%s] of leg configuration [%s] not found in business process error handlings.");
            }

            //splitting
            if (leg.getSplitting() == null) {
                String name = getAttributeValue(leg, "splittingXml", String.class);
                //splitting can be null
                if (StringUtils.isNotEmpty(name)) {
                    createIssue(issues, leg, name, "Splitting [%s] of leg configuration [%s] not found in splitting configurations.");
                }
            }


        });

        return issues;
    }

    private void createIssue(List<PModeIssue> issues, LegConfiguration leg, String name, String s) {
        String message;
        if(StringUtils.isEmpty(name)) {
            message = String.format(s.replaceFirst("\\[%s] ", ""), leg.getName());
        } else {
            message = String.format(s, name, leg.getName());
        }
        issues.add(new PModeIssue(message, PModeIssue.Level.ERROR));
    }

    private <T> T getAttributeValue(Object object, String attribute, Class<T> type) {
        if (object == null) {
            return null;
        }
        Class clazz = object.getClass();
        try {
            Field field = clazz.getDeclaredField(attribute);
            field.setAccessible(true);
            Object val = field.get(object);
            return type.cast(val);
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException | ClassCastException e) {
            return null;
        }
    }
}
