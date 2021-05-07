package eu.domibus.core.error;

import eu.domibus.api.model.MSHRole;
import eu.domibus.common.ErrorCode;

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

    void createErrorLog(MSHRole mshRole, String messageInErrorId, ErrorCode errorCode, String errorDetail);

    /**
     * delete ErrorLogEntry records not having messageId and older than x days
     */
    public void deleteErrorLogWithoutMessageIds();
}
