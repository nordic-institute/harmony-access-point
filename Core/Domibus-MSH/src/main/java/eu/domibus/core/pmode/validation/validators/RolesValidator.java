package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author musatmi
 * @since 3.3
 */
@Component
@Order(4)
public class RolesValidator implements PModeValidator {

    final PModeValidationHelper pModeValidationHelper;

    public RolesValidator(PModeValidationHelper pModeValidationHelper) {
        this.pModeValidationHelper = pModeValidationHelper;
    }

    @Override
    public List<ValidationIssue> validate(Configuration configuration) {
        List<ValidationIssue> issues = new ArrayList<>();

        final BusinessProcesses businessProcesses = configuration.getBusinessProcesses();

        if(businessProcesses.getRoles() != null) {
            Roles roles = pModeValidationHelper.getAttributeValue(businessProcesses, "rolesXml", Roles.class);
            List<String> names = roles.getRole().stream().map(el -> el.getName().toLowerCase()).collect(Collectors.toList());
            Set<String> duplicates = names.stream().filter(name -> Collections.frequency(names, name) > 1).collect(Collectors.toSet());
            if (duplicates.size() > 0) {
                issues.add(new ValidationIssue("Business process roles contain duplicate names case insensitive:" + duplicates, ValidationIssue.Level.ERROR));
            }
        }

        for (Process process : businessProcesses.getProcesses()) {
            final Role initiatorRole = process.getInitiatorRole();
            final Role responderRole = process.getResponderRole();

            if (initiatorRole != null && initiatorRole.equals(responderRole)) {
                String errorMessage = "For the business process [" + process.getName() + "], the initiator role name and the responder role name are identical [" + initiatorRole.getName() + "]";
                issues.add(new ValidationIssue(errorMessage, ValidationIssue.Level.WARNING));
            }
            if (initiatorRole != null && responderRole != null
                    && StringUtils.equalsIgnoreCase(initiatorRole.getValue(), responderRole.getValue())) {
                String errorMessage = "For the business process [" + process.getName() + "], the initiator role value and the responder role value are identical [" + initiatorRole.getValue() + "]";
                issues.add(new ValidationIssue(errorMessage, ValidationIssue.Level.WARNING));
            }
        }

        return Collections.unmodifiableList(issues);
    }

}
