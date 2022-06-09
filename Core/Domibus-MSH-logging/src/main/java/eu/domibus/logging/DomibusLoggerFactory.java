package eu.domibus.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLoggerFactory {
    private final static Logger LOG = LoggerFactory.getLogger(DomibusLoggerFactory.class);

    private DomibusLoggerFactory() {}

    public static DomibusLogger getLogger(String name) {
        return (DomibusLogger) Proxy.newProxyInstance(
                DomibusLogger.class.getClassLoader(),
                new Class[]{DomibusLogger.class},
                (proxy, method, methodArgs) -> {
                    final DomibusLogger logger = DomibusLoggersCache.getLogger(name);
                    return method.invoke(logger, methodArgs);
                }
        );
    }

    public static DomibusLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}
