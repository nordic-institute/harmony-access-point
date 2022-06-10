package eu.domibus.logging;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static eu.domibus.logging.DomibusLogger.MDC_DOMAIN;
import static eu.domibus.logging.DomibusLogger.MDC_PROPERTY_PREFIX;

public class DomibusLoggersCache {
    private final static Map<String, DomibusLogger> cache = new ConcurrentHashMap<>();

    public static DomibusLogger getLogger(String name){
        String loggerName = getLoggerName(name);
        DomibusLogger logger = cache.get(loggerName);
        if(logger==null){
            logger = new DomibusLoggerImpl(LoggerFactory.getLogger(loggerName));
            cache.put(loggerName, logger);
        }
        return logger;
    }

    public static String getLoggerName(String name) {
        String domainName = MDC.get(MDC_PROPERTY_PREFIX + MDC_DOMAIN);
        if(domainName == null){
            domainName = "";
        } else {
            domainName = domainName + ".";
        }
        return domainName + name;
    }
}
