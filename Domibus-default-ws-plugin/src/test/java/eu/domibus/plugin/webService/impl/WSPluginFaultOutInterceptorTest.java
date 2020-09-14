package eu.domibus.plugin.webService.impl;

import eu.domibus.plugin.webService.generated.RetrieveMessageFault;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.service.model.OperationInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.persistence.OptimisticLockException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.1.4
 */
@SuppressWarnings("ThrowableNotThrown")
@RunWith(JMockit.class)
public class WSPluginFaultOutInterceptorTest {

    @Tested
    private WSPluginFaultOutInterceptor wsPluginFaultOutInterceptor;

    @Injectable
    private WebServicePluginExceptionFactory webServicePluginExceptionFactory;

    @Test
    public void handleMessageWithNoException(@Injectable SoapMessage message,
                                             @Injectable SoapFault soapFault) {
        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.getExceptionContent(message);
            result = null;
            times = 1;
        }};

        wsPluginFaultOutInterceptor.handleMessage(message);

        new FullVerifications() {};
    }

    @Test
    public void handleMessageWithException_forbiddenCode(@Injectable SoapMessage message,
                                                         @Injectable SoapFault soapFault) {
        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.getExceptionContent(message);
            //result = soapFault throw the exception instead of returning the object
            returns(soapFault);
            times = 1;

            wsPluginFaultOutInterceptor.soapFaultHasForbiddenCode(soapFault);
            result = true;
            times = 1;

            soapFault.getCause();
            times = 1;

            message.setContent(Exception.class, any);
            times = 1;

            wsPluginFaultOutInterceptor.getMethodName(message);
            result = "Nope";
            times = 1;
        }};

        wsPluginFaultOutInterceptor.handleMessage(message);

        new FullVerifications() {};
    }
    @Test
    public void handleMessageWithException_UnknownMethod(@Injectable SoapMessage message,
                                                         @Injectable SoapFault soapFault) {
        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.getExceptionContent(message);
            //result = soapFault throw the exception instead of returning the object
            returns(soapFault);
            times = 1;

            wsPluginFaultOutInterceptor.soapFaultHasForbiddenCode(soapFault);
            result = false;
            times = 1;

            wsPluginFaultOutInterceptor.getMethodName(message);
            result = "Nope";
            times = 1;
        }};

        wsPluginFaultOutInterceptor.handleMessage(message);

        new FullVerifications() {};
    }

    @Test
    public void handleMessageWithException_RETRIEVE_MESSAGE(@Injectable SoapMessage message,
                                                         @Injectable SoapFault soapFault) {
        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.getExceptionContent(message);
            //result = soapFault throw the exception instead of returning the object
            returns(soapFault);
            times = 1;

            wsPluginFaultOutInterceptor.soapFaultHasForbiddenCode(soapFault);
            result = false;
            times = 1;

            wsPluginFaultOutInterceptor.getMethodName(message);
            result = WebServicePluginOperation.RETRIEVE_MESSAGE;
            times = 1;

            wsPluginFaultOutInterceptor.handleRetrieveMessage(message, soapFault);
            times = 1;
        }};

        wsPluginFaultOutInterceptor.handleMessage(message);

        new FullVerifications() {};
    }

    @Test
    public void soapFaultHasForbiddenCode(@Injectable SoapFault soapFault) {
        new Expectations(){{
            soapFault.getCode();
            result = "TEST";
        }};
        boolean result = wsPluginFaultOutInterceptor.soapFaultHasForbiddenCode(soapFault);

        Assert.assertFalse(result);
    }

    @Test
    public void soapFaultHasForbiddenCode_XML_STREAM_EXC(@Injectable SoapFault soapFault) {
        new Expectations(){{
            soapFault.getCode();
            result = "XML_STREAM_EXC";
        }};
        boolean result = wsPluginFaultOutInterceptor.soapFaultHasForbiddenCode(soapFault);

        assertTrue(result);
    }

    @Test
    public void soapFaultHasForbiddenCode_XML_WRITE_EXC(@Injectable SoapFault soapFault) {
        new Expectations(){{
            soapFault.getCode();
            result = "XML_WRITE_EXC";
        }};
        boolean result = wsPluginFaultOutInterceptor.soapFaultHasForbiddenCode(soapFault);

        assertTrue(result);
    }

    @Test
    public void handleRetrieveMessage(@Injectable SoapMessage message,
                                      @Injectable UnexpectedRollbackException cause) {
        Exception exception = new Exception(cause);

        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.handleRetrieveMessageUnexpectedRollbackException(message, exception, cause);
            times = 1;
        }};

        wsPluginFaultOutInterceptor.handleRetrieveMessage(message, exception);

        new FullVerifications() {};
    }

    @Test
    public void handleRetrieveMessageUnexpectedRollbackException(@Injectable SoapMessage message,
                                                                 @Injectable Exception exception,
                                                                 @Injectable UnexpectedRollbackException cause) {

        String errorMessage = "customMessage";
        new Expectations(wsPluginFaultOutInterceptor) {{
            wsPluginFaultOutInterceptor.getRetrieveMessageErrorMessage(cause, anyString);
            result = errorMessage;

            exception.getMessage();
            result = "Exception Message";

            exception.getStackTrace();
            result = null;

            exception.getCause();
            result = null;

            exception.getSuppressed();
            result = null;

            webServicePluginExceptionFactory.createFault("Error retrieving message");
            result = WebServiceIPluginmpl.WEBSERVICE_OF.createFaultDetail();
        }};

        wsPluginFaultOutInterceptor.handleRetrieveMessageUnexpectedRollbackException(message, exception, cause);

        new FullVerifications() {{
            Fault fault;
            message.setContent(Exception.class, fault = withCapture());

            assertTrue(fault.getCause() instanceof RetrieveMessageFault);
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
        assertTrue(retrieveMessageErrorMessage.contains("An attempt was made to download message"));
        new FullVerifications() {};
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
        new FullVerifications() {};
    }
    @Test
    public void getMethodName_exchangeNull(@Injectable SoapMessage message,
                              @Injectable OperationInfo operationInfo) {
        String methodName = "myMethodName";

        new Expectations() {{
            message.getExchange();
            result = null;
        }};

        String result = wsPluginFaultOutInterceptor.getMethodName(message);
        Assert.assertNull(methodName, result);
        new FullVerifications() {};
    }
    @Test
    public void getMethodName_bindingOperationInfoNull(@Injectable SoapMessage message,
                              @Injectable OperationInfo operationInfo) {
        String methodName = "myMethodName";

        new Expectations() {{
            message.getExchange().getBindingOperationInfo();
            result = null;
        }};

        String result = wsPluginFaultOutInterceptor.getMethodName(message);
        Assert.assertNull(methodName, result);
        new FullVerifications() {};
    }

    @Test
    public void getMethodName_operationInfoNull(@Injectable SoapMessage message,
                              @Injectable OperationInfo operationInfo) {
        String methodName = "myMethodName";

        new Expectations() {{
            message.getExchange().getBindingOperationInfo().getOperationInfo();
            result = null;
        }};

        String result = wsPluginFaultOutInterceptor.getMethodName(message);
        Assert.assertNull(methodName, result);
        new FullVerifications() {};
    }

    @Test
    public void getExceptionContent(@Injectable SoapMessage message,
                                    @Injectable Exception exception) {

        new Expectations(){{
            message.getContent(Exception.class);
            //result = soapFault throw the exception instead of returning the object
            returns(exception);
            times = 1;
        }};

        Exception exceptionContent = wsPluginFaultOutInterceptor.getExceptionContent(message);

        assertEquals(exception, exceptionContent);
        new FullVerifications(){};
    }
}