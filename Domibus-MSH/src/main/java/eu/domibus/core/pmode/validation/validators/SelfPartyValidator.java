package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
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
    PModeValidationHelper pModeValidationHelper;

    @Override
    public List<PModeIssue> validate(Configuration pMode) {
        List<PModeIssue> issues = new ArrayList<>();

        if (pMode.getParty() == null) {
            String partyName =  pModeValidationHelper.getAttributeValue(pMode, "partyXml", String.class);
            String message = String.format("Party [%s] not found in business process parties", partyName);
            issues.add(new PModeIssue(message, PModeIssue.Level.ERROR));
        }

        return issues;
    }

}
