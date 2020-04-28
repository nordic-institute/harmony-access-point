package eu.domibus.ext.rest.error;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.*;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @since 4.2
 * @author Catalin Enache
 */
@RunWith(JMockit.class)
public class ExtExceptionHandlerAdviceTest {

    @Tested
    ExtExceptionHandlerAdvice extExceptionHandlerAdvice;

    @Test
    public void test_handlePModeExtServiceException(final @Mocked PModeExtException pModeExtException) {

        //tested method
        ResponseEntity<ErrorDTO> result  = extExceptionHandlerAdvice.handlePModeExtServiceException(pModeExtException);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(ExtExceptionHandlerAdvice.HTTP_STATUS_INVALID_REQUEST, result.getStatusCode());

        new FullVerifications() {{
            extExceptionHandlerAdvice.handlePModeExtException(pModeExtException);
        }};
    }

    @Test
    public void test_handlePartyExtServiceException(final @Mocked PartyExtServiceException partyExtServiceException) {

        //tested method
        ResponseEntity<ErrorDTO> result = extExceptionHandlerAdvice.handlePartyExtServiceException(partyExtServiceException);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        new FullVerifications() {{
            extExceptionHandlerAdvice.handleExtException(partyExtServiceException);
        }};
    }

    @Test
    public void test_handleMessageAcknowledgeExtException(final @Mocked MessageAcknowledgeExtException messageAcknowledgeExtException) {

        //tested method
        ResponseEntity<ErrorDTO> result = extExceptionHandlerAdvice.handleMessageAcknowledgeExtException(messageAcknowledgeExtException);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        new FullVerifications() {{
            extExceptionHandlerAdvice.handleExtException(messageAcknowledgeExtException);
        }};
    }

    @Test
    public void test_handleDomibusMonitoringExtException(final @Mocked DomibusMonitoringExtException domibusMonitoringExtException) {
        //tested method
        ResponseEntity<ErrorDTO> result = extExceptionHandlerAdvice.handleDomibusMonitoringExtException(domibusMonitoringExtException);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        new FullVerifications() {{
            extExceptionHandlerAdvice.handleExtException(domibusMonitoringExtException);
        }};
    }

    @Test
    public void test_handleMessageMonitorExtException(final @Mocked MessageMonitorExtException messageMonitorExtException) {
        //tested method
        ResponseEntity<ErrorDTO> result = extExceptionHandlerAdvice.handleMessageMonitorExtException(messageMonitorExtException);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        new FullVerifications() {{
            extExceptionHandlerAdvice.handleExtException(messageMonitorExtException);
        }};
    }

    @Test
    public void test_handleException(final @Mocked Exception exception) {

        //tested method
        ResponseEntity<ErrorDTO> result = extExceptionHandlerAdvice.handleException(exception);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        new FullVerifications(extExceptionHandlerAdvice) {{
            extExceptionHandlerAdvice.createResponse(exception);
        }};
    }

    @Test
    public void test_handleExtException() {

        final DomibusCoreException domibusCoreException = new DomibusCoreException("core test exception");
        final DomibusServiceExtException extException = new DomibusServiceExtException(DomibusErrorCode.DOM_003, "ext core exception", domibusCoreException);

        new Expectations(extExceptionHandlerAdvice) {{
        }};

        //tested method
        extExceptionHandlerAdvice.handleExtException(extException);

        new FullVerifications(extExceptionHandlerAdvice) {{
            HttpStatus httpStatusActual;
            extExceptionHandlerAdvice.createResponseFromCoreException((Throwable) any, httpStatusActual = withCapture());
            Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpStatusActual);
        }};
    }

    @Test
    public void test_handlePModeExtException(final @Mocked PModeExtException pModeExtException) {
        final String errorMessage = "PMode validation failed";
        new Expectations() {{
            pModeExtException.getErrorMessage();
            result = errorMessage;
        }};

        //tested method
        ResponseEntity<ErrorDTO> result = extExceptionHandlerAdvice.handlePModeExtException(pModeExtException);
        Assert.assertEquals(ExtExceptionHandlerAdvice.HTTP_STATUS_INVALID_REQUEST, result.getStatusCode());
        Assert.assertTrue(result.getBody().getMessage().contains(errorMessage));

        new FullVerifications(extExceptionHandlerAdvice) {{
            new ResponseEntity(any, new HttpHeaders(), ExtExceptionHandlerAdvice.HTTP_STATUS_INVALID_REQUEST);
        }};
    }

    @Test
    public void test_createResponse(final @Mocked Throwable throwable) {

        //tested method
        ResponseEntity<ErrorDTO> result = extExceptionHandlerAdvice.createResponse(throwable);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        new FullVerifications(extExceptionHandlerAdvice) {{
            boolean showErrorDetailsActual;
            extExceptionHandlerAdvice.createResponse(throwable, HttpStatus.INTERNAL_SERVER_ERROR, anyBoolean);
        }};
    }

}