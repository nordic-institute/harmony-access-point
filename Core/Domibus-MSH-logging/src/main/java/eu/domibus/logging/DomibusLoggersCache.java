package eu.domibus.logging;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static eu.domibus.logging.DomibusLogger.MDC_DOMAIN;
import static eu.domibus.logging.DomibusLogger.MDC_PROPERTY_PREFIX;

public class DomibusLoggersCache {
    private final static Map<String, DomibusLogger> cache = new ConcurrentHashMap<>();

    /**
     * Flag indicating whether Domibus is running in single tenancy mode or not. Initially, {@code true} by default.
     */
    private static final AtomicBoolean SINGLE_TENANCY_MODE = new AtomicBoolean(true);

    public static DomibusLogger getLogger(String name) {
        String loggerName = getLoggerName(name);
        DomibusLogger logger = cache.get(loggerName);
        if (logger == null) {
            logger = new DomibusLogger(LoggerFactory.getLogger(loggerName));
            cache.put(loggerName, logger);
        }
        return logger;
    }

    public static String getLoggerName(String name) {
        String domainName = MDC.get(MDC_PROPERTY_PREFIX + MDC_DOMAIN);
        if (domainName == null || SINGLE_TENANCY_MODE.get()) {
            domainName = "";
        } else {
            domainName = domainName + ".";
        }
        return domainName + name;
    }

    public static void setSingleTenancyMode(boolean mode) {
        SINGLE_TENANCY_MODE.set(mode);
    }
}
