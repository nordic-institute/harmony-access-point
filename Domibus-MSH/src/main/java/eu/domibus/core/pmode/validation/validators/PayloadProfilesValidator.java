package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Attachment;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
                payloadProfile -> issues.addAll(validatePayloadProfile(payloadProfile, validPayloads)));

        return issues;
    }

    protected List<ValidationIssue> validatePayloadProfile(PayloadProfile payloadProfile, Set<Payload> validPayloads) {
        List<ValidationIssue> issues = new ArrayList<>();
        List<Attachment> attachmentList = pModeValidationHelper.getAttributeValue(payloadProfile, "attachment", List.class);

        // attachments should correspond to existing payloads
        if (!CollectionUtils.isEmpty(attachmentList)) {
            attachmentList.stream()
                    .filter(attachment -> validPayloads.stream().noneMatch(payload -> StringUtils.equalsIgnoreCase(payload.getName(), attachment.getName())))
                    .forEach(attachment -> issues.add(createIssue(payloadProfile, attachment.getName(),
                            "Attachment [%s] of payload profile [%s] not found among the defined payloads")));
        }

        //validate max Size
        long maxSize = payloadProfile.getMaxSize();
        if (maxSize < 0) {
            issues.add(createIssue(payloadProfile, String.valueOf(maxSize),
                    "the maxSize value [%s] of payload profile [%s] should be neither negative neither a positive value greater than " + Long.MAX_VALUE));
        }

        return issues;
    }

    protected ValidationIssue createIssue(PayloadProfile payloadProfile, String name, String message) {
        return pModeValidationHelper.createValidationIssue(message, name, payloadProfile.getName());
    }
}
