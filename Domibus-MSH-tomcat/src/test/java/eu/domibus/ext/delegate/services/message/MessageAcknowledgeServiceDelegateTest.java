package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.message.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.ext.delegate.mapper.MessageExtMapper;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author  migueti, Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageAcknowledgeServiceDelegateTest {

    @Tested
    MessageAcknowledgeServiceDelegate messageAcknowledgeServiceDelegate;

    @Injectable
    MessageAcknowledgeService messageAcknowledgeService;

    @Injectable
    MessageExtMapper messageExtMapper;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    UserMessageSecurityService userMessageSecurityService;

    @Test
    public void testAcknowledgeMessageDelivered()  {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");

        final MessageAcknowledgement messageAcknowledgement = new MessageAcknowledgement();

        new Expectations(messageAcknowledgeServiceDelegate) {{
            messageAcknowledgeService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);
            result = messageAcknowledgement;

        }};

        messageAcknowledgeServiceDelegate.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);

        new Verifications() {{
            messageAcknowledgeService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);
            messageExtMapper.messageAcknowledgementToMessageAcknowledgementDTO(messageAcknowledgement);
        }};
    }

    @Test
    public void testAcknowledgeMessageDeliveredWithNoProperties()  {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());

        new Expectations(messageAcknowledgeServiceDelegate) {{
            messageAcknowledgeService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);
        }};

        messageAcknowledgeServiceDelegate.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp);
    }

    @Test
    public void testGetAcknowledgeMessages() {
        final String messageId = "1";
        final List<MessageAcknowledgement> messageAcknowledgements = new ArrayList<>();
        messageAcknowledgements.add(new MessageAcknowledgement());
        messageAcknowledgements.add(new MessageAcknowledgement());


        new Expectations(messageAcknowledgeServiceDelegate) {{
            messageAcknowledgeService.getAcknowledgedMessages(messageId);
            result = messageAcknowledgements;

        }};

        messageAcknowledgeServiceDelegate.getAcknowledgedMessages(messageId);

        new Verifications() {{
            messageExtMapper.messageAcknowledgementToMessageAcknowledgementDTO(messageAcknowledgements);
        }};
    }
}
