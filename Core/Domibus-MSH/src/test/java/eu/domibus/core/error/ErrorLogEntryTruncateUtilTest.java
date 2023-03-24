package eu.domibus.core.error;

import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class ErrorLogEntryTruncateUtilTest {
    @Test
    public void truncate257CharacterLongMessageId() {
        final String messageId = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-";
        final String transformedmessageId = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789";
        final String errorDetail = "error detail";

        ErrorLogEntry errorLogEntry = new ErrorLogEntry(null, messageId, ErrorCode.EBMS_0010, errorDetail);

        new ErrorLogEntryTruncateUtil().truncate(errorLogEntry);

        assertEquals(errorDetail, errorLogEntry.getErrorDetail());
        assertEquals(transformedmessageId, errorLogEntry.getMessageInErrorId());
    }

    @Test
    public void truncate257CharacterErrorDetail() {
        final String errorDetail = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-";
        final String transformedErrorDetail = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789";
        final String messageID = "messageId";

        ErrorLogEntry errorLogEntry = new ErrorLogEntry(null, messageID, ErrorCode.EBMS_0010, errorDetail);

        new ErrorLogEntryTruncateUtil().truncate(errorLogEntry);

        assertEquals(transformedErrorDetail, errorLogEntry.getErrorDetail());
        assertEquals(messageID, errorLogEntry.getMessageInErrorId());
    }

    @Test
    public void truncate257CharacterSignalMessageId() {
        final String signalMessageID = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-";
        final String transformedSignalMessageID = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789";
        final String messageID = "messageId";
        final String errorDetail = "error detail";

        ErrorLogEntry errorLogEntry = new ErrorLogEntry( EbMS3ExceptionBuilder.getInstance()
                .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0006)
                .message(errorDetail)
                .refToMessageId(messageID)
                .signalMessageId(signalMessageID)
                .build());

        new ErrorLogEntryTruncateUtil().truncate(errorLogEntry);

        assertEquals(messageID, errorLogEntry.getMessageInErrorId());
        assertEquals(errorDetail, errorLogEntry.getErrorDetail());
        assertEquals(transformedSignalMessageID, errorLogEntry.getErrorSignalMessageId());
    }

}