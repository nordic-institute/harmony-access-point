package eu.domibus.test.utils

import org.apache.log4j.Logger

import java.nio.file.Files
import java.util.stream.Stream

class FileUtils {
    def static LOG = Logger.getLogger(LogUtils.SOAPUI_LOGGER_NAME)

    /**
     * Method waits for number of lines in file or until max number of retries are reached
     * @param textFile - text file to check number of lines
     * @param lineCount - expected line count
     * @param maxRetries - number of max retries to check (def: 20)
     * @param waitForNextRetry - wait in ms for next retry (def: 5_000 ms)
     */
    static void waitForExpectedLineCount(File textFile, int expectedLineCount, def maxRetries = 20, def waitForNextRetry = 5_000) {
        def numberOfLines = 0;
        for (int i = 0; i < maxRetries; i++) {
            if (textFile.exists()) {
                Stream<String> stream = Files.lines(textFile.toPath())
                numberOfLines = stream.count();
                if (numberOfLines == expectedLineCount) {
                    break;
                }
            }
            LOG.info i + ". Got [" + numberOfLines + "] lines (expected [" + expectedLineCount + "]) wait another [$waitForNextRetry]ms";
            sleep(waitForNextRetry)
        }
    }
}