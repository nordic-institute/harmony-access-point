package eu.domibus.core.plugin.validation;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.logging.IDomibusLogger;
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

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(SubmissionValidatorService.class);

    protected SubmissionValidatorListProvider submissionValidatorListProvider;
    protected SubmissionAS4Transformer submissionAS4Transformer;

    public SubmissionValidatorService(SubmissionValidatorListProvider submissionValidatorListProvider,
                                      SubmissionAS4Transformer submissionAS4Transformer) {
        this.submissionValidatorListProvider = submissionValidatorListProvider;
        this.submissionAS4Transformer = submissionAS4Transformer;
    }

    public void validateSubmission(UserMessage userMessage, List<PartInfo> partInfoList, String backendName) throws SubmissionValidationException {
        SubmissionValidatorList submissionValidatorList = submissionValidatorListProvider.getSubmissionValidatorList(backendName);
        if (submissionValidatorList == null) {
            LOG.debug("No submission validators found for backend [{}]", backendName);
            return;
        }
        LOG.info("Performing submission validation for backend [{}]", backendName);
        Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage, partInfoList);
        List<SubmissionValidator> submissionValidators = submissionValidatorList.getSubmissionValidators();
        for (SubmissionValidator submissionValidator : submissionValidators) {
            submissionValidator.validate(submission);
        }
    }
}
