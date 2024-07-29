
package eu.domibus.core.exception;

import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.plugin.exception.TransformationException;

/**
 * TODO: add class description
 */
public class MessagingExceptionFactory {


    public static MessagingProcessingException transform(EbMS3Exception originalException) {


        ErrorCode errorCode = originalException.getErrorCodeObject();
        MessagingProcessingException messagingProcessingException;

        String message = ErrorCode.EbMS3ErrorCode.findErrorCodeBy(originalException.getErrorCodeObject().getErrorCodeName()).getShortDescription() + " detail: " + originalException.getErrorDetail();

        switch (errorCode) {
            case EBMS_0007:
                messagingProcessingException = new TransformationException(message, originalException);
                break;
            case EBMS_0001:
            case EBMS_0010:
                messagingProcessingException = new PModeMismatchException(message, originalException);
                break;
            default:
                if (originalException.getCause() != null &&
                        originalException.getCause().getMessage() != null &&
                        originalException.getCause().getMessage().contains("constraint [tb_user_message.UK_USER_MSG_MESSAGE_ID]")) {
                    messagingProcessingException = new DuplicateMessageException(message, originalException);
                } else {
                    messagingProcessingException = new MessagingProcessingException(message, originalException);
                }
        }

        messagingProcessingException.setEbms3ErrorCode(errorCode);
        return messagingProcessingException;
    }

    public static MessagingProcessingException transform(Exception originalException, ErrorCode errorCode) {
        MessagingProcessingException messagingProcessingException = new MessagingProcessingException(originalException.getMessage(), originalException);
        messagingProcessingException.setEbms3ErrorCode(errorCode);
        return messagingProcessingException;
    }

}
