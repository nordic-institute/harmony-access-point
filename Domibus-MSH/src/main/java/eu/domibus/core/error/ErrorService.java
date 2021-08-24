package eu.domibus.core.error;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.core.ebms3.EbMS3Exception;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ErrorService {

    void createErrorLogSending(String messageInErrorId, ErrorCode errorCode, String errorDetail, UserMessage userMessage);

    /**
     * create ErrorLog with {@link eu.domibus.api.model.MSHRole#SENDING}
     * @param exception
     * @param userMessage
     */
    void createErrorLogSending(EbMS3Exception exception, UserMessage userMessage);

    void createErrorLog(EbMS3Exception exception, MSHRole mshRole);

    void createErrorLog(Ebms3Messaging ebms3Messaging, MSHRole mshRole);

    /**
     * delete ErrorLogEntry records not having messageId and older than x days
     */
    void deleteErrorLogWithoutMessageIds();

    int deleteErrorLogsByMessageIdInError(List<String> messageIds);

    List<ErrorLogEntry> getErrorsForMessage(String messageId);

    List<ErrorResult> getErrors(String messageId);

    List<ErrorLogEntry> findPaged(int from, int max, String sortColumn, boolean asc, Map<String, Object> filters);

    long countEntries(Map<String, Object> filters);
}
