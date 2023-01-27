package eu.domibus.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Gabriel Maier
 * @since 5.1
 */
public class DomibusLineOfCallerConverterTest {
    private static final String DOMIBUS_LINE_OF_CALLER_CLASS_NAME = DomibusLineOfCallerConverter.class.getName();
    private static final String PATTERN_RULE_REGISTRY = "PATTERN_RULE_REGISTRY";
    private static final ByteArrayOutputStream LOG_OUTPUT_STREAM = new ByteArrayOutputStream();
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusLineOfCallerConverterTest.class);

    @BeforeClass
    public static void validateConfigurationAndAddAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(DomibusLineOfCallerConverterTest.class);
        LoggerContext loggerContext = logger.getLoggerContext();
        assertNotNull(loggerContext);
        Map<String, String> conversionRules = (HashMap<String, String>) loggerContext.getObject(PATTERN_RULE_REGISTRY);
        assertNotNull(conversionRules);
        Optional<String> conversionWord = conversionRules.entrySet()
                .stream()
                .filter(entry -> DOMIBUS_LINE_OF_CALLER_CLASS_NAME.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst();

        assertTrue("The logback config file in the classpath should contain a conversionRule of type " + DOMIBUS_LINE_OF_CALLER_CLASS_NAME,
                conversionWord.isPresent());

        addCapturingAppender(logger, conversionWord.get());
    }

    private static void addCapturingAppender(Logger logger, String lineNumberConversionWord) {
        LoggerContext loggerContext = logger.getLoggerContext();
        PatternLayoutEncoder logLayoutEncoder = new PatternLayoutEncoder();
        logLayoutEncoder.setPattern("%c{1};%" + lineNumberConversionWord + ";%m");
        logLayoutEncoder.setContext(loggerContext);
        logLayoutEncoder.start();
        FileAppender<ILoggingEvent> inMemoryAppender = new FileAppender<ILoggingEvent>() {
            @Override
            public void setOutputStream(OutputStream outputStream) {
                //prevent the appender from overriding the LOG_OUTPUT_STREAM with the disk stream
                if (outputStream instanceof ByteArrayOutputStream) {
                    super.setOutputStream(outputStream);
                }
            }
        };
        inMemoryAppender.setFile("target/out.log");
        inMemoryAppender.setName("inMemoryAppender");
        inMemoryAppender.setContext(loggerContext);
        inMemoryAppender.setEncoder(logLayoutEncoder);
        inMemoryAppender.setImmediateFlush(true);
        inMemoryAppender.setOutputStream(LOG_OUTPUT_STREAM);
        inMemoryAppender.start();
        logger.addAppender(inMemoryAppender);
    }

    @AfterClass
    public static void clearByteArrayStream() throws IOException {
        LOG_OUTPUT_STREAM.close();
    }

    @Test
    public void testDomibusLoggerOutput() throws IOException {
        //given
        LOG_OUTPUT_STREAM.flush();
        LOG_OUTPUT_STREAM.reset();
        //when
        int expectedLineNumber = logTextAndReturnLineNumber();
        //then
        String logOutput = LOG_OUTPUT_STREAM.toString();
        assertFalse("Expected a log message in the output stream", logOutput.isEmpty());
        assertOutputIsCorrect(logOutput, expectedLineNumber, getClass().getSimpleName());
    }

    private void assertOutputIsCorrect(String logOutput, int expectedLineNumber, String expectedClassName) {
        String[] tokens = logOutput.split(";");
        assertTrue("Expecting class name " + expectedClassName + " but actual class name was " + tokens[0],
                tokens[0].endsWith("." + expectedClassName));
        assertEquals(String.valueOf(expectedLineNumber), tokens[1]);
    }

    private int logTextAndReturnLineNumber() {
        int currentLine;
        LOG.info("Expecting line number {}", currentLine = new Throwable().getStackTrace()[0].getLineNumber());   //do not split this line
        return currentLine;
    }

}