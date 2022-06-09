package eu.domibus.logging;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static eu.domibus.logging.IDomibusLogger.MDC_DOMAIN;
import static eu.domibus.logging.IDomibusLogger.MDC_PROPERTY_PREFIX;

public class DomibusLoggersCache {
    public static final String COMMON_PREFIX = "domibus";
    private final static Map<String, IDomibusLogger> cache = new ConcurrentHashMap<>();

    public static IDomibusLogger getLogger(String name){
        String loggerName = getLoggerName(name);
        IDomibusLogger logger = cache.get(loggerName);
        if(logger==null){
            logger = new DomibusLoggerImpl(LoggerFactory.getLogger(loggerName));
            cache.put(loggerName, logger);
        }
        return logger;
    }

    public static String getLoggerName(String name) {
        final String domainName = MDC.get(MDC_PROPERTY_PREFIX + MDC_DOMAIN);
        String loggerNamePrefix = COMMON_PREFIX + ".";
        if(domainName != null){
            loggerNamePrefix = loggerNamePrefix + domainName + ".";
        }
        return loggerNamePrefix + name;
    }
}
