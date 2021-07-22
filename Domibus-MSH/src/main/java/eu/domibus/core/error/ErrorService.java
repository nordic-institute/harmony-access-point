package eu.domibus.core.error;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.core.ebms3.EbMS3Exception;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ErrorService {
    /**
     * persist an error log.
     *
     * @param errorLogEntry the error.
     */
    void createErrorLog(ErrorLogEntry errorLogEntry);

    void createErrorLog(String messageInErrorId, ErrorCode errorCode, String errorDetail);


    void createErrorLog(EbMS3Exception ebms3Exception);

    /**
     * delete ErrorLogEntry records not having messageId and older than x days
     */
    public void deleteErrorLogWithoutMessageIds();

    List<? extends ErrorResult> getErrors(String messageId);
}
