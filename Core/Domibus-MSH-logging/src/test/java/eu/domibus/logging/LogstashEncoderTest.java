package eu.domibus.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class LogstashEncoderTest {

    @Test
    public void testLogstashEncoder() throws IOException {
        Logger logger = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);

        LogstashEncoder logstashEncoder = new LogstashEncoder();
        logstashEncoder.setContext(logger.getLoggerContext());
        logstashEncoder.start();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamAppender<ILoggingEvent> outputStreamAppender = new OutputStreamAppender<>();
        outputStreamAppender.setContext(logger.getLoggerContext());
        outputStreamAppender.setEncoder(logstashEncoder);
        outputStreamAppender.setOutputStream(outputStream);
        outputStreamAppender.start();

        logger.addAppender(outputStreamAppender);

        String testMessage = "Test log message";
        logger.info(testMessage);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> loggedMessage = objectMapper.readValue(outputStream.toString(), new TypeReference<Map<String, Object>>() {});
        assertNotNull(loggedMessage.get("@timestamp"));
        assertEquals(testMessage, loggedMessage.get("message"));
        assertEquals("INFO", loggedMessage.get("level"));
    }
}
