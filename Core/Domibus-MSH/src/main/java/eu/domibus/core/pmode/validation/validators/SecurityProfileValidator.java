package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for the Security Profiles section of PMode
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Component
@Order(6)
public class SecurityProfileValidator implements PModeValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityProfileValidator.class);

    final PModeValidationHelper pModeValidationHelper;

    public SecurityProfileValidator(PModeValidationHelper pModeValidationHelper) {
        this.pModeValidationHelper = pModeValidationHelper;
    }

    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        //TODO: validate that the profile exists eg

        return issues;
    }
}
