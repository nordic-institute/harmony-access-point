package eu.domibus.logging;

import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

/**
 * Resolves the line number where the logging method is called when using DomibusLogger proxies
 *
 * @author Gabriel Maier
 */
public class DomibusLineOfCallerConverter extends LineOfCallerConverter {

    public static final String LOGGER_FACTORY_CLASS_NAME = DomibusLoggerFactory.class.getName();

    @Override
    public String convert(ILoggingEvent event) {
        String lineNumber = super.convert(event);
        if (CoreConstants.NA.equals(lineNumber) || !lineNumber.startsWith("-")) {
            return lineNumber;
        }

        //because the DomibusLoggers are proxies over actual Loggers we can get incorrect line numbers (negative values)
        //we need to exclude the first few levels of the caller stack to get to the actual invocation of the logger method
        StackTraceElement[] callerStack = event.getCallerData();
        if (callerStack == null) {
            return CoreConstants.NA;
        }
        for (int i = 0; i < callerStack.length; i++) {
            int indexOfMethodCallingTheProxy = i + 2;
            if (callerStack[i].getClassName().startsWith(LOGGER_FACTORY_CLASS_NAME)
                    && indexOfMethodCallingTheProxy < callerStack.length) {
                return String.valueOf(callerStack[indexOfMethodCallingTheProxy].getLineNumber());
            }
        }
        return CoreConstants.NA;
    }
}
