package eu.domibus.test.utils

import org.apache.log4j.Logger

/**
 * Simple utility methods for calling WS endpoints using the CURL application
 *
 */
class CurlUtils {
    def static LOG = Logger.getLogger(LogUtils.SOAPUI_LOGGER_NAME)
    static def MAX_RESTART_WAIT_TIME = 60_000 // Maximum time to wait to check.
    static def STEP_TIME = 1_000 // Time to wait before re-checking.

    /**
     *  Check if Url iss accessible expecting codes  200|204|302
     * @param url - test endpoint URL
     * @param log
     * @return true if the endpoint returns expected HTTP code
     */
    static boolean urlAccessible(String url, def regExpCodes = /(?s).*HTTP\/\d.\d\s*(200|204|302).*/) {
        def commandString = ["curl", url, "-v"]
        def commandResult = ShellUtils.runCommandInShell(commandString)
        return (commandResult[1] ==~ regExpCodes)
    }

    /**
     * Wait for URL to become accessible.
     * @param url
     * @param log
     * @param maxWaitTime - max wait time in ms (default is 60000)
     * @param stepTime - test interval in ms (default is 1000)
     * @return return true if value becomes accessible in maxWaitTime, else return false
     */
    static boolean waitForUrlAccessible(String url, def maxWaitTime = 60_000, def stepTime = 1_000) {
        def waitTime = maxWaitTime
        while ((waitTime > 0)) {
            waitTime = waitTime - stepTime

            if (urlAccessible(url + "/")) {
                return true;
            }
            LOG.debug("Trying to access url [$url] wait [$stepTime]ms for next retry!")
            sleep(stepTime)
        }
        LOG.warn("  Url [" + url + "]   [][]  was not accessible in [" + maxWaitTime + "s]")
        return true;
    }
}
