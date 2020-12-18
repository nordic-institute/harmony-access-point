package eu.domibus.core.error;

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

    /**
     * delete ErrorLogEntry records not having messageId and older than x days
     */
    public void deleteErrorLogWithoutMessageIds();
}
