package eu.domibus.core.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import eu.domibus.api.logging.LoggingConfigurator;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author  Cosmin Baciu
 * @since 3.3
 */
public class LogbackLoggingConfigurator implements LoggingConfigurator {

    private static final String DEFAULT_LOGBACK_FILE_NAME = "logback.xml";
    private static final String LOGBACK_CONFIGURATION_FILE_PARAM = "logback.configurationFile";

    private static final Logger LOG = LoggerFactory.getLogger(LogbackLoggingConfigurator.class);

    protected String domibusConfigLocation;

    public LogbackLoggingConfigurator(String domibusConfigLocation) {
        this.domibusConfigLocation = domibusConfigLocation;
    }

    @Override
    public void configureLogging() {
        String logbackConfigurationFile = getLoggingConfigurationFile();
        configureLogging(logbackConfigurationFile);
    }

    @Override
    public void configureLogging(String logbackConfigurationFile) {
        if (StringUtils.isEmpty(logbackConfigurationFile)) {
            LOG.warn("Could not configure logging: the provided configuration file is empty");
            return;
        }

        LOG.info("Using the logback configuration file from [" + logbackConfigurationFile + "]");

        if (!new File(logbackConfigurationFile).exists()) {
            LOG.warn("Could not configure logging: the file [" + logbackConfigurationFile + "] does not exists");
            return;
        }

        configureLogback(logbackConfigurationFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLoggingConfigurationFile() {
        String logbackConfigurationFile = getDefaultLogbackConfigurationFile();
        String customLogbackConfigurationFile = System.getProperty(LOGBACK_CONFIGURATION_FILE_PARAM);
        if (StringUtils.isNotEmpty(customLogbackConfigurationFile)) {
            LOG.info("Found custom logback configuration file: [" + customLogbackConfigurationFile + "]");
            logbackConfigurationFile = customLogbackConfigurationFile;
        }
        return logbackConfigurationFile;
    }

    protected void configureLogback(String logbackConfigurationFile) {
        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            configurator.doConfigure(logbackConfigurationFile);
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    protected String getDefaultLogbackConfigurationFile() {
        if(StringUtils.isEmpty(domibusConfigLocation)) {
            LOG.error("The property [" + DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION + "] is not configured" );
            return null;
        }

        return getLogFileLocation(domibusConfigLocation, DEFAULT_LOGBACK_FILE_NAME);
    }

    protected String getLogFileLocation(String domibusConfigLocation, String logFileName) {
        return domibusConfigLocation + File.separator + logFileName;
    }
}
