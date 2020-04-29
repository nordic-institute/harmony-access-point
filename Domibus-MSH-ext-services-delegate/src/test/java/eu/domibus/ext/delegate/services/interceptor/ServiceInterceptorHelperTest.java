package eu.domibus.ext.delegate.services.interceptor;

import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.ext.exceptions.PModeExtException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ServiceInterceptorHelperTest {

    @Tested
    ServiceInterceptorHelper serviceInterceptorHelper;

    @Test
    public void test_handlePModeValidationException(final @Mocked PModeValidationException coreException) {

        ValidationIssue validationIssue = new ValidationIssue("test 123");
        List<ValidationIssue> validationIssueList = Collections.singletonList(validationIssue);
        final PModeExtException pModeExtException = new PModeExtException("PMode Exception");
        new Expectations() {{
            coreException.getIssues();
            result = validationIssueList;
        }};

        //tested method
        serviceInterceptorHelper.handlePModeValidationException(coreException, pModeExtException);
        Assert.assertTrue(pModeExtException.getErrorMessage().contains("test 123"));
    }
}