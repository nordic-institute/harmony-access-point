package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
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
 * Validates self party existence  aka checks if configuration/@party is found in businessProcesses/parties collection
 */
@Component
@Order(5)
public class SelfPartyValidator implements PModeValidator {
    @Autowired
    protected PModeValidationHelper pModeValidationHelper;

    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (pMode.getParty() == null) {
            String partyName = pModeValidationHelper.getAttributeValue(pMode, "partyXml", String.class);
            String message = "Party [%s] not found in business process parties";
            if (StringUtils.isEmpty(partyName)) {
                message = message.replaceFirst("\\[%s] ", "");
            } else {
                message = String.format(message, partyName);
            }

            issues.add(new ValidationIssue(message, ValidationIssue.Level.ERROR));
        }

        return issues;
    }

}
