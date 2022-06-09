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

    public static IDomibusLogger getLogger(String name) {
        return (IDomibusLogger) Proxy.newProxyInstance(
                IDomibusLogger.class.getClassLoader(),
                new Class[]{IDomibusLogger.class},
                (proxy, method, methodArgs) -> {
                    final IDomibusLogger logger = DomibusLoggersCache.getLogger(name);
                    return method.invoke(logger, methodArgs);
                }
        );
    }

    public static IDomibusLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}
