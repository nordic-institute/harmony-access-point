package eu.domibus.plugin.validation;

import eu.domibus.plugin.Submission;
import org.springframework.stereotype.Component;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
@Component("payloadsRequiredSubmissionValidator")
public class PayloadsRequiredSubmissionValidator implements SubmissionValidator {

    @Override
    public void validate(Submission submission) throws SubmissionValidationException {
        if (submission.getPayloads() == null || submission.getPayloads().isEmpty()) {
            throw new SubmissionValidationException("No payloads found. At least one payload is required");
        }
    }
}
