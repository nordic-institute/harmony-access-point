package eu.domibus.core.plugin.validation;

import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class SubmissionValidatorService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SubmissionValidatorService.class);

    protected SubmissionValidatorListProvider submissionValidatorListProvider;
    protected SubmissionAS4Transformer submissionAS4Transformer;

    public SubmissionValidatorService(SubmissionValidatorListProvider submissionValidatorListProvider,
                                      SubmissionAS4Transformer submissionAS4Transformer) {
        this.submissionValidatorListProvider = submissionValidatorListProvider;
        this.submissionAS4Transformer = submissionAS4Transformer;
    }

    public void validateSubmission(UserMessage userMessage, String backendName, NotificationType notificationType) throws SubmissionValidationException {
        if (NotificationType.MESSAGE_RECEIVED != notificationType) {
            LOG.debug("Validation is not configured to be done for notification of type [{}]", notificationType);
            return;
        }

        SubmissionValidatorList submissionValidatorList = submissionValidatorListProvider.getSubmissionValidatorList(backendName);
        if (submissionValidatorList == null) {
            LOG.debug("No submission validators found for backend [{}]", backendName);
            return;
        }
        LOG.info("Performing submission validation for backend [{}]", backendName);
        Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage);
        List<SubmissionValidator> submissionValidators = submissionValidatorList.getSubmissionValidators();
        for (SubmissionValidator submissionValidator : submissionValidators) {
            submissionValidator.validate(submission);
        }
    }
}
