package eu.domibus.web.rest;

import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.logging.LoggingException;
import eu.domibus.core.logging.LoggingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.LoggingFilterRequestRO;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * REST resource for setting or retrieving logging levels at runtime
 *
 * @author Catalin Enache
 * since 4.1
 */
@RestController
@RequestMapping(value = "/rest/logging")
@Validated
public class LoggingResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LoggingResource.class);

    @Autowired
    DomainCoreConverter domainConverter;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    protected ErrorHandlerService errorHandlerService;

    @ExceptionHandler({LoggingException.class})
    public ResponseEntity<ErrorRO> handleLoggingException(LoggingException ex) {
        LOG.error(ex.getMessage(), ex);
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    /**
     * It will change the logging level for given name and sets to level desired
     *
     * @param request it contains logger name and level
     * @return response of the operation
     */
    @PostMapping(value = "/loglevel")
    public ResponseEntity<String> setLogLevel(@RequestBody @Valid LoggingLevelRO request) {
        final String name = request.getName();
        final String level = request.getLevel();

        //set log level on current server
        loggingService.setLoggingLevel(name, level);

        //signals to other servers in a cluster environment
        loggingService.signalSetLoggingLevel(name, level);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("Success while setting log level " + level + " for " + name);

    }

    @GetMapping(value = "/loglevel")
    public ResponseEntity<LoggingLevelResultRO> getLogLevel(@Valid LoggingFilterRequestRO request) {
        final LoggingLevelResultRO resultRO = new LoggingLevelResultRO();
        List<LoggingLevelRO> loggingEntries = domainConverter.
                convert(loggingService.getLoggingLevel(request.getLoggerName(), request.isShowClasses()), LoggingLevelRO.class);

        int count = loggingEntries.size();
        int fromIndex = request.getPageSize() * request.getPage();
        int toIndex = fromIndex + request.getPageSize();
        if (toIndex > count) {
            toIndex = count;
        }
        resultRO.setCount(count);
        resultRO.setLoggingEntries(new ArrayList<>(loggingEntries.subList(fromIndex, toIndex)));

        //add the filter
        HashMap<String, Object> filter = new HashMap<>();
        filter.put("loggerName", request.getLoggerName());
        filter.put("showClasses", request.isShowClasses());
        resultRO.setFilter(filter);

        resultRO.setPage(request.getPage());
        resultRO.setPageSize(request.getPageSize());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resultRO);

    }

    /**
     * Reset the logging configuration to default
     *
     * @return string for success or error
     */
    @PostMapping(value = "/reset")
    public ResponseEntity<String> resetLogging() {

        //reset log level on current server
        loggingService.resetLogging();

        // signals to other servers in a cluster environment
        loggingService.signalResetLogging();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("Logging configuration was successfully reset.");
    }

}
