package eu.domibus.ext.rest.error;

import eu.domibus.ext.exceptions.DomibusServiceExtException;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ExtExceptionHandlerAdviceTest {

    @Tested
    ExtExceptionHandlerAdvice extExceptionHandlerAdvice;

    @Injectable
    ExtExceptionHelper extExceptionHelper;


    @Test
    public void test_handleException(final @Mocked Exception exception) {
        //tested method
        extExceptionHandlerAdvice.handleException(exception);

        new FullVerifications() {{
            extExceptionHelper.createResponse(exception);
        }};
    }

    @Test
    public void test_handleDomibusServiceExtException(final @Mocked DomibusServiceExtException domibusServiceExtException) {

        extExceptionHandlerAdvice.handleDomibusServiceExtException(domibusServiceExtException);

        new FullVerifications() {{
            extExceptionHelper.handleExtException(domibusServiceExtException);
        }};
    }

    @Test
    public void test_handleException_Generic(final @Mocked Exception exception) {

        extExceptionHandlerAdvice.handleException(exception);

        new FullVerifications() {{
            extExceptionHelper.createResponse(exception);
        }};
    }
}