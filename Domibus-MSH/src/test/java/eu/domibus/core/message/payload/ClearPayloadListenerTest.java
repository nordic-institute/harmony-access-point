package eu.domibus.core.message.payload;

import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Message;

/**
 * @since 4.2
 * @author Catalin Enache
 */
@RunWith(JMockit.class)
public class ClearPayloadListenerTest {

    @Tested
    ClearPayloadListener clearPayloadListener;

    @Injectable
    ClearPayloadMessageService clearPayloadMessageService;

    @Test
    public void test_onMessage(final @Mocked Message message) throws Exception {
        final String messageId = "messageId-123456";

        new Expectations() {{
            message.getStringProperty(MessageConstants.MESSAGE_ID) ;
            result = messageId;
        }};

        clearPayloadListener.onMessage(message);

        new FullVerifications() {{
            clearPayloadMessageService.clearPayloadData(messageId);
        }};
    }

    @Test
    public void test_onMessage_NoMessageId(final @Mocked Message message) throws Exception {
        new Expectations() {{
            message.getStringProperty(MessageConstants.MESSAGE_ID) ;
            result = null;
        }};

        clearPayloadListener.onMessage(message);

        new FullVerifications() {{
        }};
    }
}