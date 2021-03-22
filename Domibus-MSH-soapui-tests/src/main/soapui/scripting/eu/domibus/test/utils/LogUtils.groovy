package eu.domibus.test.utils;

/**
 * This is utility class for Logging
 *
 */
class LogUtils {

    static def final DEFAULT_LOG_LEVEL = 0;

    /**
     * Method check the log level and logs the message
     * @param logMsg - message to be logged
     * @param log  - logger objects
     * @param logLevel - if log level is 1 or true, then message is logged with info level, else message is ignored
     */
    static def void debugLog(logMsg, log,  logLevel = DEFAULT_LOG_LEVEL) {
        if (logLevel.toString() == "1" || logLevel.toString() == "true") log.info(logMsg)
    }
}