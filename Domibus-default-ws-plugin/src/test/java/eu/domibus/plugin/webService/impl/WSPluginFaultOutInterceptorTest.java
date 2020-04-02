package eu.domibus.plugin.webService.impl;

import eu.domibus.plugin.webService.generated.RetrieveMessageFault;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.service.model.OperationInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.persistence.OptimisticLockException;

/**
 * @author Cosmin Baciu
 * @since 4.1.4
 */
@RunWith(JMockit.class)
public class WSPluginFaultOutInterceptorTest {

    @Tested
    WSPluginFaultOutInterceptor wsPluginFaultOutInterceptor;

    @Injectable
    BackendWebServiceExceptionFactory backendWebServiceExceptionFactory;

    @Test
    public void handleMessageWithNoException(@Injectable SoapMessage message) {
        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.getExceptionContent(message);
            result = null;
        }};

        wsPluginFaultOutInterceptor.handleMessage(message);

        new Verifications() {{
            wsPluginFaultOutInterceptor.getMethodName(message);
            times = 0;
        }};
    }

    @Test
    public void handleRetrieveMessage(@Injectable SoapMessage message,
                                      @Injectable UnexpectedRollbackException cause) {
        Exception exception = new Exception(cause);

        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.handleRetrieveMessageUnexpectedRollbackException(message, exception, cause);
        }};

        wsPluginFaultOutInterceptor.handleRetrieveMessage(message, exception);

        new Verifications() {{
            wsPluginFaultOutInterceptor.handleRetrieveMessageUnexpectedRollbackException(message, exception, cause);
        }};
    }

    @Test
    public void handleRetrieveMessageUnexpectedRollbackException(@Injectable SoapMessage message,
                                                                 @Injectable Exception exception,
                                                                 @Injectable UnexpectedRollbackException cause) {

        String errorMessage = "customMessage";
        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.getRetrieveMessageErrorMessage(cause, anyString);
            result = errorMessage;
        }};

        wsPluginFaultOutInterceptor.handleRetrieveMessageUnexpectedRollbackException(message, exception, cause);

        new Verifications() {{
            Fault fault = null;
            message.setContent(Exception.class, fault = withCapture());

            Assert.assertTrue(fault.getCause() instanceof RetrieveMessageFault);
        }};
    }

    @Test
    public void getRetrieveMessageErrorMessage(@Injectable UnexpectedRollbackException unexpectedRollbackException) {
        String messageId = "123";

        new Expectations() {{
            unexpectedRollbackException.contains(OptimisticLockException.class);
            result = true;
        }};

        String retrieveMessageErrorMessage = wsPluginFaultOutInterceptor.getRetrieveMessageErrorMessage(unexpectedRollbackException, messageId);
        Assert.assertTrue(retrieveMessageErrorMessage.contains("An attempt was made to download message"));
    }

    @Test
    public void getMethodName(@Injectable SoapMessage message,
                              @Injectable OperationInfo operationInfo) {
        String methodName = "myMethodName";

        new Expectations() {{
            message.getExchange().getBindingOperationInfo().getOperationInfo();
            result = operationInfo;

            operationInfo.getInputName();
            result = methodName;
        }};

        String result = wsPluginFaultOutInterceptor.getMethodName(message);
        Assert.assertEquals(methodName, result);
    }
}