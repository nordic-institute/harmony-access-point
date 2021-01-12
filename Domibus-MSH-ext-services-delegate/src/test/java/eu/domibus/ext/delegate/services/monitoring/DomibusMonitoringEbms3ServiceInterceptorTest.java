package eu.domibus.ext.delegate.services.monitoring;

import eu.domibus.api.monitoring.DomibusMonitoringService;
import eu.domibus.api.util.AOPUtil;
import eu.domibus.ext.domain.monitoring.MonitoringInfoDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusMonitoringEbms3ServiceInterceptorTest {

    @Tested
    DomibusMonitoringServiceInterceptor domibusMonitoringServiceInterceptor;

    @Injectable
    DomibusMonitoringService domibusMonitoringService;

    @Injectable
    AOPUtil aopUtil;

    @Test
    public void testIntercept(@Injectable final ProceedingJoinPoint joinPoint) throws Throwable {
        // Given
        final MonitoringInfoDTO monitoringInfoDTO = new MonitoringInfoDTO();

        new Expectations() {{
            joinPoint.proceed();
            result = monitoringInfoDTO;
        }};

        // When
        final Object interceptedResult = domibusMonitoringServiceInterceptor.intercept(joinPoint);

        // Then
        Assert.assertEquals(monitoringInfoDTO, interceptedResult);
    }

    @Test
    public void testInterceptWhenExtExceptionIsRaised(@Injectable final ProceedingJoinPoint joinPoint) throws Throwable {
        // Given
        final DomibusMonitoringExtException domibusMonitoringExtException = new DomibusMonitoringExtException(DomibusErrorCode.DOM_001, "test");

        new Expectations() {{
            joinPoint.proceed();
            result = domibusMonitoringExtException;
        }};

        // When
        try {
            domibusMonitoringServiceInterceptor.intercept(joinPoint);
        } catch (DomibusMonitoringExtException e) {
            // Then
            Assert.assertTrue(domibusMonitoringExtException == e);
            return;
        }
        Assert.fail();
    }

    @Test
    public void testInterceptWhenCoreExceptionIsRaised(@Injectable final ProceedingJoinPoint joinPoint) throws Throwable {
        // Given
        final DomibusMonitoringExtException domibusMonitoringExtException = new DomibusMonitoringExtException(DomibusErrorCode.DOM_001, "test");

        new Expectations() {{
            joinPoint.proceed();
            result = domibusMonitoringExtException;
        }};

        // When
        try {
            domibusMonitoringServiceInterceptor.intercept(joinPoint);
        } catch (DomibusMonitoringExtException e) {
            Assert.assertTrue(domibusMonitoringExtException == e);
            return;
        }
        Assert.fail();
    }
}
