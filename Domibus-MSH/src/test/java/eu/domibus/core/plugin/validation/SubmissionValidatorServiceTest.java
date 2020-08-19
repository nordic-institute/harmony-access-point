package eu.domibus.core.plugin.validation;

import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class SubmissionValidatorServiceTest {

    @Tested
    SubmissionValidatorService submissionValidatorService;

    @Injectable
    SubmissionValidatorListProvider submissionValidatorListProvider;

    @Injectable
    SubmissionAS4Transformer submissionAS4Transformer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testValidateSubmissionForUnsupportedNotificationType(@Injectable final Submission submission,
                                                                     @Injectable final UserMessage userMessage) {
        final String backendName = "customPlugin";
        submissionValidatorService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED_FAILURE);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateSubmissionWhenFirstValidatorThrowsException(@Injectable final Submission submission,
                                                                        @Injectable final UserMessage userMessage,
                                                                        @Injectable final SubmissionValidatorList submissionValidatorList,
                                                                        @Injectable final SubmissionValidator validator1,
                                                                        @Injectable final SubmissionValidator validator2) {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionAS4Transformer.transformFromMessaging(userMessage);
            result = submission;
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = submissionValidatorList;
            submissionValidatorList.getSubmissionValidators();
            result = new SubmissionValidator[]{validator1, validator2};
            validator1.validate(submission);
            result = new SubmissionValidationException("Exception in the validator1");
        }};

        thrown.expect(SubmissionValidationException.class);
        submissionValidatorService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateSubmissionWithAllValidatorsCalled(@Injectable final Submission submission,
                                                              @Injectable final UserMessage userMessage,
                                                              @Injectable final SubmissionValidatorList submissionValidatorList,
                                                              @Injectable final SubmissionValidator validator1,
                                                              @Injectable final SubmissionValidator validator2) {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionAS4Transformer.transformFromMessaging(userMessage);
            result = submission;
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = submissionValidatorList;
            submissionValidatorList.getSubmissionValidators();
            result = new SubmissionValidator[]{validator1, validator2};
        }};

        submissionValidatorService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new FullVerifications() {{
            validator1.validate(submission);
            times = 1;
            validator2.validate(submission);
            times = 1;
        }};
    }

    @Test
    public void testValidateSubmission_noValidator(@Injectable final Submission submission,
                                                   @Injectable final UserMessage userMessage,
                                                   @Injectable final SubmissionValidatorList submissionValidatorList,
                                                   @Injectable final SubmissionValidator validator1,
                                                   @Injectable final SubmissionValidator validator2) {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = null;
        }};

        submissionValidatorService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new FullVerifications() {
        };
    }
}