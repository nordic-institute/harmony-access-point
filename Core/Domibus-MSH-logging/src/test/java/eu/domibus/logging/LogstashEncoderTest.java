package eu.domibus.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.*;

public class LogstashEncoderTest {

    private Logger testLogger;
    private ByteArrayOutputStream outputStream;
    private OutputStreamAppender<ILoggingEvent> appender;
    private LogstashEncoder encoder;

    @Before
    public void setUp() {
        testLogger = (Logger) LoggerFactory.getLogger("test-logger");
        testLogger.setLevel(Level.INFO);

        encoder = new LogstashEncoder();
        encoder.setContext(testLogger.getLoggerContext());
        encoder.start();

        outputStream = new ByteArrayOutputStream();
        appender = new OutputStreamAppender<>();
        appender.setContext(testLogger.getLoggerContext());
        appender.setEncoder(encoder);
        appender.setOutputStream(outputStream);
        appender.start();

        testLogger.addAppender(appender);

        MDC.put("d_user", "user123");
        MDC.put("d_domain", "domainA");
    }

    @After
    public void tearDown() {
        testLogger.detachAppender(appender);
        appender.stop();
        encoder.stop();
        MDC.clear();
    }

    @Test
    public void testLoggingEventCompositeJsonEncoder() throws IOException {
        String message = "Test log message";
        testLogger.info(message);

        String json = outputStream.toString().trim();
        assertFalse(json.isEmpty());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> logged = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});

        assertNotNull(logged.get("@timestamp"));
        assertEquals(message, logged.get("message"));
        assertEquals("INFO", logged.get("level"));
        assertEquals("test-logger", logged.get("logger_name"));
        assertEquals("user123", logged.get("d_user"));
        assertEquals("domainA", logged.get("d_domain"));
    }
}
