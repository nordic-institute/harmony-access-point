package eu.domibus.ext.rest;

import eu.domibus.ext.exceptions.MessageAcknowledgeExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.MessageAcknowledgeExtService;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 4.2
 * @author Catalin Enache
 */
@RunWith(JMockit.class)
public class MessageAcknowledgementExtResourceTest {

    @Tested
    MessageAcknowledgementExtResource messageAcknowledgementExtResource;

    @Injectable
    MessageAcknowledgeExtService messageAcknowledgeService;

    @Injectable
    ExtExceptionHelper extExceptionHelper;

    @Test
    public void test_handleMessageAcknowledgeExtException(final @Mocked MessageAcknowledgeExtException messageAcknowledgeExtException) {
        //tested method
        messageAcknowledgementExtResource.handleMessageAcknowledgeExtException(messageAcknowledgeExtException);

        new FullVerifications() {{
            extExceptionHelper.handleExtException(messageAcknowledgeExtException);
        }};
    }
}