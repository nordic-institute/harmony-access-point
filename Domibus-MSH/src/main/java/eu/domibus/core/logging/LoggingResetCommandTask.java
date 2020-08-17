package eu.domibus.core.logging;

import eu.domibus.api.cluster.Command;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class LoggingResetCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(LoggingResetCommandTask.class);

    protected LoggingService loggingService;

    public LoggingResetCommandTask(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.LOGGING_RESET, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Resetting logging command");

        loggingService.resetLogging();
    }
}
