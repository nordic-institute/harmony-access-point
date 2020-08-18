package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * This aspect looks after framework annotation timer and counter, and then configure metrics. In this scenario,
 * drop wizard metrics.
 */
@Aspect
@Component
public class MetricsAspect {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MetricsAspect.class);

    @Autowired
    protected MetricRegistry metricRegistry;

    @Around("@annotation(timer)")
    public Object surroundWithATimer(ProceedingJoinPoint pjp, Timer timer) throws Throwable {
        return createTimer(pjp, timer.value(), timer.clazz());
    }
    @Around("@annotation(timer)")
    public Object surroundWithATimer(ProceedingJoinPoint pjp, eu.domibus.ext.domain.metrics.Timer timer) throws Throwable {
        return createTimer(pjp, timer.value(), timer.clazz());
    }


    protected Object createTimer(ProceedingJoinPoint pjp, String value, Class<?> clazz) throws Throwable {
        com.codahale.metrics.Timer.Context methodTimer = metricRegistry.timer(getMetricsName(clazz, value, "_timer")).time();
        try {
            return pjp.proceed();
        } finally {
            if (methodTimer != null) {
                methodTimer.stop();
            }
        }
    }

    protected String getMetricsName(Class<?> clazz, String name, String suffix) {
        String metricsName = name + suffix;
        if (Void.class.isAssignableFrom(clazz)) {
            return metricsName;
        }
        return name(clazz, metricsName);
    }

    @Around("@annotation(counter)")
    public Object surroundWithACounter(ProceedingJoinPoint pjp, Counter counter) throws Throwable {
        return createCounter(pjp, counter.clazz(), counter.value());
    }

    @Around("@annotation(counter)")
    public Object surroundWithACounter(ProceedingJoinPoint pjp, eu.domibus.ext.domain.metrics.Counter counter) throws Throwable {
        return createCounter(pjp, counter.clazz(), counter.value());
    }

    protected Object createCounter(ProceedingJoinPoint pjp, Class<?> clazz, String value) throws Throwable {
        com.codahale.metrics.Counter methodCounter = metricRegistry.counter(getMetricsName(clazz,value, "_counter"));
        try {
            methodCounter.inc();
            return pjp.proceed();
        } finally {
            methodCounter.dec();
        }
    }

}
