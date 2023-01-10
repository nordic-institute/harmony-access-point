package eu.domibus.logging;

import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;

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
        if (CallerData.NA.equals(lineNumber) || !lineNumber.startsWith("-")) {
            return lineNumber;
        }

        //because the DomibusLoggers are proxies over actual Loggers we can get incorrect line numbers (negative values)
        //we need to exclude the first few levels of the caller stack to get to the actual invocation of the logger method
        StackTraceElement[] callerDataStack = event.getCallerData();
        if (callerDataStack == null) {
            return CallerData.NA;
        }
        for (int i = 0; i < callerDataStack.length; i++) {
            if (callerDataStack[i].getClassName().startsWith(LOGGER_FACTORY_CLASS_NAME)
                    && i + 2 < callerDataStack.length) {
                return String.valueOf(callerDataStack[i + 2].getLineNumber());
            }
        }
        return CallerData.NA;
    }
}
