package eu.domibus.jms.spi;

import org.springframework.jms.core.JmsOperations;

import javax.jms.Destination;
import javax.jms.Topic;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Handle JMS operation for a specific implementation (Weblogic, WildFly and Tomcat)
 *
 * @author Cosmin Baciu
 * @since 3.2
 */
public interface InternalJMSManager {

    String QUEUE = "Queue";

    String PROP_MAX_BROWSE_SIZE = "domibus.jms.queue.maxBrowseSize";

    /** in multi-tenancy mode domain admins should not see any count of messages so we set this value */
    long NB_MESSAGES_ADMIN = -1L;

    String JMS_PRIORITY = "JMSPriority";

    Map<String, InternalJMSDestination> findDestinationsGroupedByFQName();

    void sendMessage(InternalJmsMessage message, String destination);

    void sendMessage(InternalJmsMessage message, Destination destination);

    void sendMessage(InternalJmsMessage message, String destination, JmsOperations jmsOperations);

    void sendMessage(InternalJmsMessage message, Destination destination, JmsOperations jmsOperations);

    void sendMessageToTopic(InternalJmsMessage internalJmsMessage, Topic destination);

    void sendMessageToTopic(InternalJmsMessage internalJmsMessage, Topic destination, boolean excludeOrigin);

    int deleteMessages(String source, String[] messageIds);

    int moveMessages(String source, String destination, String[] messageIds);

    InternalJmsMessage getMessage(String source, String messageId);

    List<InternalJmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);

    List<InternalJmsMessage> browseClusterMessages(String source, String selector);

    InternalJmsMessage consumeMessage(String source, String customMessageId);

    /**
     * Get the number of messages in a JMS Destination
     * @param internalJMSDestination internal representation of a JMS Destination
     * @return number of messages in a JMS queue (destination)
     */
    long getDestinationCount(InternalJMSDestination internalJMSDestination);

}
