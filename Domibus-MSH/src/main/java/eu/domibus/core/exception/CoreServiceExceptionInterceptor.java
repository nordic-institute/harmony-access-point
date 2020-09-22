package eu.domibus.core.exception;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.util.AOPUtil;
import eu.domibus.logging.DomibusLogger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Intercepts the execution of methods and converts thrown exceptions to the exceptions specific to each service
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public abstract class CoreServiceExceptionInterceptor {

    protected AOPUtil aopUtil;

    public CoreServiceExceptionInterceptor(AOPUtil aopUtil) {
        this.aopUtil = aopUtil;
    }

    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        final String methodSignature = aopUtil.getMethodSignature(joinPoint);
        getLogger().debug("Preparing to execute method [{}]", methodSignature);

        try {
            final Object proceed = joinPoint.proceed();
            return proceed;
        } catch (DomibusCoreException e) {
            //nothing to convert; re-throw exception
            throw e;
        } catch (Exception e) {
            //converts internal(core) exceptions to the exceptions specific to each service
            throw convertCoreException(e);
        } finally {
            getLogger().debug("Finished executing method [{}]", methodSignature);
        }
    }

    public abstract Exception convertCoreException(Exception e);

    public abstract DomibusLogger getLogger();
}