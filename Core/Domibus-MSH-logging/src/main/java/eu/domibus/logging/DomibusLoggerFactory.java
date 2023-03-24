package eu.domibus.logging;

import eu.domibus.logging.exception.DomibusLoggingException;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLoggerFactory {
    private final static Logger LOG = LoggerFactory.getLogger(DomibusLoggerFactory.class);

    private DomibusLoggerFactory() {
    }

    public static DomibusLogger getLogger(String name) {
        try {
            return getNewProxyInstance(name);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 InstantiationException e) {
            throw new DomibusLoggingException("Failed to create DomibusLogger proxy", e);
        }
    }

    private static DomibusLogger getNewProxyInstance(String name) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(DomibusLogger.class);
        factory.setFilter(method -> true);
        MethodHandler handler = (self, method, proceed, methodArgs) -> {
            final DomibusLogger logger = DomibusLoggersCache.getLogger(name);
            return method.invoke(logger, methodArgs);
        };
        return (DomibusLogger) factory.create(new Class<?>[]{Logger.class}, new Object[]{null}, handler);
    }


    public static DomibusLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}
