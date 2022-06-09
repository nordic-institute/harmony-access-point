package eu.domibus.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Aspect responsible for cleaning the MDC keys after the execution of a method.
 * The MDC keys are cleaned only if before the method execution were not already set.
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
@Aspect
public class MDCCleanAspect {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(MDCCleanAspect.class);

    @Around(value = "execution(public * eu.domibus..*(..)) && @annotation(MDCKey)")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        final String targetLocation = getTargetLocation(method);
        LOG.debug("Preparing to execute method [{}]", targetLocation);

        List<String> mdcKeysToClean = null;
        try {
            MDCKey annotation = getAnnotation(method);
            if (annotation != null) {
                List<String> keysToClean = Arrays.asList(annotation.value());
                if (annotation.cleanOnStart()) {
                    cleanMDCKeys(targetLocation, keysToClean);
                }
                mdcKeysToClean = getMDCKeysToClean(targetLocation, keysToClean);
            }

            return joinPoint.proceed();
        } finally {
            LOG.debug("Finished executing method [{}]", targetLocation);
            cleanMDCKeys(targetLocation, mdcKeysToClean);
        }
    }

    protected List<String> getMDCKeysToClean(String locationIdentifier, List<String> strings) {
        final List<String> candidatesMDCKeysToClean = new ArrayList<>(strings);
        LOG.debug("[{}]: found candidate MDC keys for cleaning [{}]", locationIdentifier, candidatesMDCKeysToClean);
        final List<String> mdcKeysToClean = getMDCKeysToClean(candidatesMDCKeysToClean);
        if (!mdcKeysToClean.isEmpty()) {
            LOG.debug("[{}]: the MDC keys [{}] will be cleaned", locationIdentifier, mdcKeysToClean);
        }
        return mdcKeysToClean;
    }

    protected String getTargetLocation(Method method) {
        return method.toString();
    }

    private MDCKey getAnnotation(Method method) {
        final MDCKey annotation = method.getAnnotation(MDCKey.class);
        if (annotation == null) {
            LOG.debug("No annotation present on method [{}]", method);
            return null;
        }
        return annotation;
    }

    protected List<String> getMDCKeysToClean(List<String> candidatesMDCKeysToClean) {
        List<String> result = new ArrayList<>();
        for (String key : candidatesMDCKeysToClean) {
            //if the MDC key was not previously set then we need to clean it
            if (LOG.getMDC(key) == null) {
                result.add(key);
            } else {
                LOG.trace("Will not delete [{}]", key);
            }
        }
        return result;
    }

    protected void cleanMDCKeys(String locationIdentifier, List<String> keysToClean) {
        if (keysToClean == null) {
            LOG.debug("[{}]: no MDC keys to clean", locationIdentifier);
            return;
        }
        for (String key : keysToClean) {
            LOG.debug("[{}]: removing MDC key [{}]", locationIdentifier, key);
            LOG.removeMDC(key);
        }
    }
}