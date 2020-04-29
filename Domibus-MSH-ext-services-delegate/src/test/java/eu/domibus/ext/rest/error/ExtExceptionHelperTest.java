package eu.domibus.ext.rest.error;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.ext.exceptions.PModeExtException;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

/**
 * @since 4.2
 * @author Catalin Enache
 */
@RunWith(JMockit.class)
public class ExtExceptionHelperTest {

    @Tested
    ExtExceptionHelper extExceptionHelper;

    @Test
    public void test_handleExtException() {
                final DomibusCoreException domibusCoreException = new DomibusCoreException("core test exception");
        final DomibusServiceExtException extException = new DomibusServiceExtException(DomibusErrorCode.DOM_003, "ext core exception", domibusCoreException);

        new Expectations(extExceptionHelper) {{
        }};

        //tested method
        extExceptionHelper.handleExtException(extException);

        new FullVerifications(extExceptionHelper) {{
            HttpStatus httpStatusActual;
            extExceptionHelper.createResponseFromCoreException((Throwable) any, httpStatusActual = withCapture());
            Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpStatusActual);
        }};
    }

    @Test
    public void test_createResponse(final @Mocked Throwable throwable) {

        //tested method
        ResponseEntity<ErrorDTO> result = extExceptionHelper.createResponse(throwable);
        Assert.assertNotNull(result.getBody());
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        new FullVerifications(extExceptionHelper) {{
            boolean showErrorDetailsActual;
            extExceptionHelper.createResponse(throwable, HttpStatus.INTERNAL_SERVER_ERROR, anyBoolean);
        }};
    }

    @Test
    public void test_getPModeValidationMessage(final @Mocked PModeValidationException pModeValidationException) {
        ValidationIssue validationIssue = new ValidationIssue("test 123");
        List<ValidationIssue> validationIssueList = Collections.singletonList(validationIssue);
        final PModeExtException pModeExtException = new PModeExtException("PMode Exception");
        new Expectations() {{
            pModeValidationException.getIssues();
            result = validationIssueList;
        }};

        //tested method
        String errorMessage = extExceptionHelper.getPModeValidationMessage(pModeValidationException);
        Assert.assertTrue(errorMessage.contains("test 123"));
    }

    @Test
    public void test_createResponseFromPModeValidationException(final @Mocked PModeValidationException pModeValidationException) {
        final String errorMessage = "[DOM_003]:PMode validation failed. Validation issues: Initiator party [blue_gw2] of process [tc1Process] not found in business process parties";

        new Expectations(extExceptionHelper) {{
            extExceptionHelper.getPModeValidationMessage(pModeValidationException);
            result =  errorMessage;
        }};

        extExceptionHelper.createResponseFromPModeValidationException(pModeValidationException);

        new FullVerifications() {{
            String errorMessageActual;
            extExceptionHelper.createResponse(errorMessageActual = withCapture(), ExtExceptionHelper.HTTP_STATUS_INVALID_REQUEST);
            Assert.assertEquals(errorMessage, errorMessageActual);
        }};
    }

}