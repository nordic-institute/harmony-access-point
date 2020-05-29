package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Attachment;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validator for Payload Profiles section of PMode
 *
 * @author Catalin Enache
 * @since 4.2
 */
@Component
@Order(8)
public class PayloadProfilesValidator implements PModeValidator {

    @Autowired
    PModeValidationHelper pModeValidationHelper;

    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        Set<PayloadProfile> payloadProfiles = pMode.getBusinessProcesses().getPayloadProfiles();
        Set<Payload> validPayloads = pMode.getBusinessProcesses().getPayloads();

        payloadProfiles.forEach(
                payloadProfile -> validatePayloadProfile(payloadProfile, validPayloads, issues));

        return issues;
    }

    protected void validatePayloadProfile(PayloadProfile payloadProfile, Set<Payload> validPayloads, List<ValidationIssue> issues) {

        List<Attachment> attachmentList = pModeValidationHelper.getAttributeValue(payloadProfile, "attachment", List.class);

        // attachments should correspond to existing payloads
        attachmentList.stream()
                .filter(attachment -> validPayloads.stream().noneMatch(payload -> payload.getName().equals(attachment.getName())))
                .forEach(attachment -> createIssue(issues, payloadProfile, attachment.getName(),
                        "Attachment [%s] of payload profile [%s] not found among the defined payloads"));

        //validate max Size
        int maxSize = payloadProfile.getMaxSize();
        if (maxSize <0 ) {
            createIssue(issues, payloadProfile, String.valueOf(maxSize),
                    "the maxSize value [%s] of payload profile [%s] should be neither negative neither a positive value greater than " + Integer.MAX_VALUE);
        }
    }

    protected void createIssue(List<ValidationIssue> issues, PayloadProfile payloadProfile, String name, String message) {
        issues.add(pModeValidationHelper.createValidationIssue(message, name, payloadProfile.getName()));
    }
}
