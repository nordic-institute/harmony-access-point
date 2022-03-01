package eu.domibus.plugin.ws;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.OutputStreamAppender;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * @author idragusa
 * @since 5.0
 */
@Component
public class LoggerUtil {
    ByteArrayOutputStream logging = new ByteArrayOutputStream();
    Logger logger;
    public static final String APPENDER_FOR_TESTING = "AppenderForTesting";

    public void addByteArrayOutputStreamAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        // Define that pattern that is used for logging.
        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%level|%msg%n");
        encoder.setContext(loggerContext);
        encoder.start();

        // Define where the logging should go
        final OutputStreamAppender appender = new OutputStreamAppender();
        appender.setEncoder(encoder);
        appender.setOutputStream(logging);
        appender.setContext(loggerContext);
        appender.setName(APPENDER_FOR_TESTING);
        appender.start();
        logger.addAppender(appender);
    }

    public void cleanupByteArrayOutputStreamAppender() {
        if (this.logger != null) {
            this.logger.detachAppender(APPENDER_FOR_TESTING);
        }
    }

    public boolean verifyLogging(String toContain) {
        final String logging = this.logging.toString();
        return StringUtils.contains(logging, toContain);
    }
}