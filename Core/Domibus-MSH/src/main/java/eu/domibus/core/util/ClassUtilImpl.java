package eu.domibus.core.util;

import eu.domibus.api.util.ClassUtil;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;


/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@Component
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ClassUtilImpl implements ClassUtil {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(ClassUtilImpl.class);

    @Override
    public String getTargetObjectClassCanonicalName(Object object) {
        if (AopUtils.isJdkDynamicProxy(object)) {
            try {
                return ((Advised) object).getTargetSource().getTarget().getClass().getCanonicalName();
            } catch (Exception e) {
                throw new AopInvocationException("Error getting the class canonical name", e);
            }
        } else if (AopUtils.isCglibProxy(object)) {
            return ClassUtils.getUserClass(object).getCanonicalName();
        } else {
            return object.getClass().getCanonicalName();
        }
    }

    @Override
    public Class getTargetObjectClass(Object object) throws ClassNotFoundException {
        final String targetObjectClassCanonicalName = getTargetObjectClassCanonicalName(object);
        return Thread.currentThread().getContextClassLoader().loadClass(targetObjectClassCanonicalName);
    }

    @Override
    public boolean isMethodDefined(Object target, String methodName, Class[] paramTyes) {
        Class<?> clazz;
        try {
            clazz = getTargetObjectClass(target);
        } catch (ClassNotFoundException e) {
            LOG.debug("Could not determine the target class of [{}]", target, e);
            clazz = target.getClass();
        }
        try {
            clazz.getDeclaredMethod(methodName, paramTyes);
        } catch (NoSuchMethodException e) {
            LOG.debug("[{}] is not defined with the specified arguments [{}].", methodName, paramTyes);
            return false;
        }

        return true;
    }
}
