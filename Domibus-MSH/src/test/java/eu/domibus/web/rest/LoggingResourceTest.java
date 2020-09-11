package eu.domibus.web.rest;

import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.logging.LoggingException;
import eu.domibus.core.logging.LoggingService;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.LoggingFilterRequestRO;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResultRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * JUnit for {@link LoggingResource}
 *
 * @author Catalin Enache
 * @since 4.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class LoggingResourceTest {

    @Tested
    LoggingResource loggingResource;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private LoggingService loggingService;

    @Injectable
    protected ErrorHandlerService errorHandlerService;

    @Before
    public void setUp() {
        loggingResource = new LoggingResource(domainConverter, loggingService, errorHandlerService);
    }

    @Test
    public void testSetLogLevel(final @Mocked LoggingLevelRO loggingLevelRO) {
        final String name = "eu.domibus";
        final String level = "DEBUG";

        new Expectations() {{
            loggingLevelRO.getName();
            result = name;

            loggingLevelRO.getLevel();
            result = level;

        }};

        //tested method
        loggingResource.setLogLevel(loggingLevelRO);

        new Verifications() {{
            loggingService.setLoggingLevel(name, level);
            times = 1;

            loggingService.signalSetLoggingLevel(name, level);
            times = 1;
        }};
    }

    @Test
    public void testGetLogLevel(final @Mocked List<LoggingEntry> loggingEntryList) {
        final String name = "eu.domibus";
        final boolean showClasses = false;

        final List<LoggingLevelRO> loggingLevelROList = new ArrayList<>();
        LoggingLevelRO loggingLevelRO1 = new LoggingLevelRO();
        loggingLevelRO1.setLevel("INFO");
        loggingLevelRO1.setName("eu.domibus");
        loggingLevelROList.add(loggingLevelRO1);
        LoggingLevelRO loggingLevelRO2 = new LoggingLevelRO();
        loggingLevelRO2.setLevel("INFO");
        loggingLevelRO2.setName("eu.domibus.common");
        loggingLevelROList.add(loggingLevelRO2);
        LoggingLevelRO loggingLevelRO3 = new LoggingLevelRO();
        loggingLevelRO3.setLevel("INFO");
        loggingLevelRO3.setName("eu.domibus.common.model");
        loggingLevelROList.add(loggingLevelRO3);

        new Expectations(loggingResource) {{
            loggingService.getLoggingLevel(name, showClasses);
            result = loggingEntryList;

            domainConverter.convert(loggingEntryList, LoggingLevelRO.class);
            result = loggingLevelROList;

        }};

        final ResponseEntity<LoggingLevelResultRO> result =
                loggingResource.getLogLevel(new LoggingFilterRequestRO() {{
                    setLoggerName(name);
                    setShowClasses(showClasses);
                    setPageSize(2);
                    setAsc(false);
                }});

        Assert.assertNotNull(result.getBody());
        Assert.assertNotNull(result.getBody().getLoggingEntries());
        List<LoggingLevelRO> loggingEntries = result.getBody().getLoggingEntries();
        Assert.assertFalse(loggingEntries.isEmpty());
        Assert.assertEquals(2, loggingEntries.size());
    }

    @Test
    public void testResetLogging() {

        //tested method
        loggingResource.resetLogging();

        new Verifications() {{
            loggingService.resetLogging();
        }};

    }

    @Test
    public void testHandleLoggingException() {

        final LoggingException loggingException = new LoggingException("error while setting log level");

        new Expectations(loggingResource) {{
            errorHandlerService.createResponse(loggingException, HttpStatus.BAD_REQUEST);
            result = new ResponseEntity<>(new ErrorRO("[DOM_001]:error while setting log level"), null, HttpStatus.BAD_REQUEST);
        }};

        //tested method
        final ResponseEntity<ErrorRO> result = loggingResource.handleLoggingException(loggingException);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(loggingException.getMessage(), result.getBody().getMessage());
    }
}