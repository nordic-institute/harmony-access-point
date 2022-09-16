package eu.domibus.core.plugin.handler;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
@RunWith(JMockit.class)
public class MessageRetrieverImplTest {
    private static final String MESS_ID = UUID.randomUUID().toString();

    private static final String GREEN = "green_gw";
    private static final String RED = "red_gw";
    private static final String AGREEMENT = "";
    private static final String SERVICE = "testService1";
    private static final String ACTION = "TC2Leg1";
    private static final String LEG = "pushTestcase1tc2Action";

    private final String pModeKey = GREEN + PModeConstants.PMODEKEY_SEPARATOR +
            RED + PModeConstants.PMODEKEY_SEPARATOR +
            SERVICE + PModeConstants.PMODEKEY_SEPARATOR +
            ACTION + PModeConstants.PMODEKEY_SEPARATOR +
            AGREEMENT + PModeConstants.PMODEKEY_SEPARATOR +
            LEG;

    @Tested
    MessageRetrieverImpl messageRetriever;

    @Injectable
    protected UserMessageDefaultService userMessageService;

    @Injectable
    private MessagingService messagingService;

    @Injectable
    private UserMessageLogDefaultService userMessageLogService;

    @Injectable
    private ErrorLogService errorLogService;

    @Injectable
    protected ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void testDownloadMessageOK(@Injectable UserMessage userMessage,
                                      @Injectable UserMessageLog userMessageLog) throws Exception {

        new Expectations() {{
            userMessageService.getByMessageId(MESS_ID, MSHRole.RECEIVING);
            result = userMessage;
            userMessageLogService.findById(anyLong);
            result = userMessageLog;
        }};

        messageRetriever.downloadMessage(MESS_ID);

        new Verifications() {{
            userMessageLogService.setMessageAsDownloaded(userMessage, userMessageLog);

        }};
    }

    @Test
    public void testDownloadMessageOK_RetentionNonZero(@Injectable UserMessage userMessage,
                                                       @Injectable final UserMessageLog messageLog) throws Exception {
        new Expectations(messageRetriever) {{
            userMessageService.getByMessageId(MESS_ID, MSHRole.RECEIVING);
            result = userMessage;

            userMessageLogService.findById(anyLong);
            result = messageLog;

        }};

        messageRetriever.downloadMessage(MESS_ID);

        new Verifications() {{
            userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);
        }};
    }

//    @Test
//    public void testDownloadMessageNoMsgFound() {
//        new Expectations() {{
//            userMessageService.getByMessageId(MESS_ID, MSHRole.RECEIVING);
//            result = new eu.domibus.messaging.MessageNotFoundException(MESS_ID);
//        }};
//
//        try {
//            messageRetriever.downloadMessage(MESS_ID);
//            Assert.fail("It should throw " + MessageNotFoundException.class.getCanonicalName());
//        } catch (eu.domibus.messaging.MessageNotFoundException mnfEx) {
//            //OK
//        }
//
//        new Verifications() {{
//            userMessageLogService.findByMessageId(MESS_ID);
//            times = 0;
//        }};
//    }

    @Test
    public void testGetErrorsForMessageOk() {
        new Expectations() {{
            EbMS3Exception ex = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0008)
                    .message("MessageId value is too long (over 255 characters)")
                    .refToMessageId(MESS_ID)
                    .build();
            List<ErrorResult> list = new ArrayList<>();
            ErrorResultImpl errorLogEntry = new ErrorResultImpl();

            errorLogEntry.setErrorCode(ex.getErrorCodeObject());
            errorLogEntry.setErrorDetail(ex.getErrorDetail());
            errorLogEntry.setMessageInErrorId(ex.getRefToMessageId());
            errorLogEntry.setMshRole(eu.domibus.common.MSHRole.RECEIVING);

            list.add(errorLogEntry);

            errorLogService.getErrors(MESS_ID, MSHRole.RECEIVING);
            result = list;

        }};

        final List<? extends ErrorResult> results = messageRetriever.getErrorsForMessage(MESS_ID);

        new Verifications() {{
            errorLogService.getErrors(MESS_ID, MSHRole.RECEIVING);
            Assert.assertNotNull(results);
            ErrorResult errRes = results.iterator().next();
            Assert.assertEquals(ErrorCode.EBMS_0008, errRes.getErrorCode());
        }};

    }

    @Test
    public void browseMessage(@Injectable UserMessage userMessage) {
        String messageId = "123";

        new Expectations(messageRetriever) {{
            userMessageService.getByMessageId(messageId, MSHRole.RECEIVING);
            result = userMessage;
        }};

        messageRetriever.browseMessage(messageId);

        new Verifications() {{
//            messageRetriever.checkMessageAuthorization(userMessage);
            messagingService.getSubmission(userMessage);
        }};
    }

}
