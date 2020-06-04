package eu.domibus.api.jms;

import org.springframework.jms.core.JmsOperations;

import javax.jms.Queue;
import javax.jms.Topic;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

/**
 * Handle all JMS operations
 *
 * @author Cosmin Baciu
 * @see JmsMessage
 * @see JMSDestination
 * @since 3.2
 */
public interface JMSManager {

    /**
     * Operation to get all destinations available on the JMS server
     *
     * @return a map where the key is the fully qualified name of the real JMS destination and the value is a JMSDestination object.
     */
    SortedMap<String, JMSDestination> getDestinations();

    /**
     * Retrieves the JMS message having a specific id  from the provided source.
     *
     * @param source    a JMS source
     * @param messageId JMS message's identifier
     * @return a JmsMessage
     */
    JmsMessage getMessage(String source, String messageId);

    /**
     * Operation to browse a JMS source with restrictions given by the selector.
     *
     * @param source   queue or topic
     * @param selector selector
     * @return a list of JmsMessage
     */
    List<JmsMessage> browseClusterMessages(String source, String selector);

    /**
     * Operation to browse a JMS source with restrictions given by the parameters.
     *
     * @param source   queue or topic
     * @param jmsType  type of the JMS message
     * @param fromDate starting date
     * @param toDate   ending date
     * @param selector selector
     * @return a list of JmsMessage
     */
    List<JmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);

    void sendMessageToQueue(JmsMessage message, String destination);

    /**
     * Send a basic pojo to a destination. it will be serialize to json and added to the queue.
     *
     * @param message     the pojo to be serialized to json.
     * @param destination the destination where the message should be send.
     * @param selector    a jms selector so that the message is retrieved by the correct jms listener.
     */
    void convertAndSendToQueue(Object message, Queue destination, String selector);

    void sendMessageToQueue(JmsMessage message, Queue destination);

    void sendMapMessageToQueue(JmsMessage message, String destination);

    void sendMapMessageToQueue(JmsMessage message, Queue destination);

    /**
     * Send a Jms message to a destination.
     *
     * @param message     the jms message to be sent
     * @param destination the destination where the message should be send.
     * @param jmsOperations    the jms operations to be used for sending
     */
    void sendMapMessageToQueue(JmsMessage message, String destination, JmsOperations jmsOperations);

    /**
     * Send a Jms message to a destination.
     *
     * @param message   the jms message to be sent
     * @param destination   the queue where the message should be send.
     * @param jmsOperations the jms operations to be used for sending
     */
    void sendMapMessageToQueue(JmsMessage message, Queue destination, JmsOperations jmsOperations);

    void sendMessageToTopic(JmsMessage message, Topic destination);

    /**
     * It sends a JMS message to {@code destination} topic but it marks to not be executed by origin server
     *
     * @param message       JMS message
     * @param destination   JMS topic
     * @param excludeOrigin default to false
     */
    void sendMessageToTopic(JmsMessage message, Topic destination, boolean excludeOrigin);

    /**
     * Delete a list of JMS Messages from {@code source}
     *
     * @param source JMS Queue
     * @param messageIds cannot be empty and at least one none blank value
     * @throws IllegalArgumentException if no message deleted
     */
    void deleteMessages(String source, String[] messageIds);

    /**
     * Move a list of messages from {@code source} to {@code destination}
     *
     * @param source JMS Queue
     * @param destination JMS Queue
     * @param messageIds cannot be empty and at least one none blank value
     * @throws IllegalArgumentException if no message moved
     */
    void moveMessages(String source, String destination, String[] messageIds);

    /**
     * Consumes a business message from a source.
     *
     * @param source    a JMS source
     * @param messageId Business message id
     * @return a JmsMessage
     */
    JmsMessage consumeMessage(String source, String messageId);

    /**
     * Return the number of jms messages in the destination.
     *
     * @param nameLike pattern representing the name of the destination (EG:%name%)
     * @return the number of messages contained in the destination.
     */
    long getDestinationSize(String nameLike);

    /**
     * Return the number of jms messages in the destination.
     *
     * @param jmsDestination JMS Queue
     * @return the number of messages contained in the destination.
     */
    long getDestinationSize(JMSDestination jmsDestination);


    String getDomainSelector(String selector);
}
