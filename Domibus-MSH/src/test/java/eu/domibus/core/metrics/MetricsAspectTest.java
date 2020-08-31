package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import junit.framework.TestCase;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class MetricsAspectTest extends TestCase {

    @Injectable
    protected MetricRegistry metricRegistry;

    @Tested
    private MetricsAspect metricsAspect;

    @Test
    public void testGetMetricsName() {
        String metricsName = "MetricsName";
        Assert.assertEquals("java.lang.String.MetricsName_counter", metricsAspect.getMetricsName(String.class, metricsName, "_counter"));
        Assert.assertEquals("MetricsName_counter", metricsAspect.getMetricsName(Void.class, metricsName, "_counter"));
    }

    @Test
    public void surroundWithATimer(@Mocked ProceedingJoinPoint pjp,@Mocked  Timer timer,@Mocked com.codahale.metrics.Timer.Context methodTimer) throws Throwable {
        String metricName = "MetricName";
        Class<String> testClass = String.class;
        new Expectations(){{
            timer.value();
            result= metricName;
            timer.clazz();
            result=testClass;
            metricRegistry.timer("java.lang.String."+metricName+"_timer").time();
            result=methodTimer;
        }};
        metricsAspect.surroundWithATimer(pjp,timer);
        new Verifications(){{
            pjp.proceed();
            methodTimer.stop();
        }};
    }
    @Test
    public void surroundWithATimerInPlugin(@Mocked ProceedingJoinPoint pjp,@Mocked  eu.domibus.ext.domain.metrics.Timer timer,@Mocked com.codahale.metrics.Timer.Context methodTimer) throws Throwable {
        String metricName = "MetricName";
        Class<String> testClass = String.class;
        new Expectations(){{
            timer.value();
            result= metricName;
            timer.clazz();
            result=testClass;
            metricRegistry.timer("java.lang.String."+metricName+"_timer").time();
            result=methodTimer;
        }};
        metricsAspect.surroundWithATimer(pjp,timer);
        new Verifications(){{
            pjp.proceed();
            methodTimer.stop();
        }};
    }

    @Test
    public void surroundWithACounter(@Mocked ProceedingJoinPoint pjp,@Mocked  Counter timer,@Mocked com.codahale.metrics.Counter counter) throws Throwable {
        String metricName = "MetricName";
        Class<String> testClass = String.class;
        new Expectations(){{
            timer.value();
            result= metricName;
            timer.clazz();
            result=testClass;
            com.codahale.metrics.Counter counter = metricRegistry.counter("java.lang.String." + metricName + "_counter");
            result=counter;
        }};
        metricsAspect.surroundWithACounter(pjp,timer);
        new Verifications(){{
            counter.inc();
            pjp.proceed();
            counter.dec();
        }};
    }

    @Test
    public void surroundWithACounterInPlugin(@Mocked ProceedingJoinPoint pjp,@Mocked  eu.domibus.ext.domain.metrics.Counter timer,@Mocked com.codahale.metrics.Counter counter) throws Throwable {
        String metricName = "MetricName";
        Class<String> testClass = String.class;
        new Expectations(){{
            timer.value();
            result= metricName;
            timer.clazz();
            result=testClass;
            com.codahale.metrics.Counter counter = metricRegistry.counter("java.lang.String." + metricName + "_counter");
            result=counter;
        }};
        metricsAspect.surroundWithACounter(pjp,timer);
        new Verifications(){{
            counter.inc();
            pjp.proceed();
            counter.dec();
        }};
    }




}