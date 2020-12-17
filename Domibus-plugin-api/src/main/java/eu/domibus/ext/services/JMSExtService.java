package eu.domibus.ext.services;

import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.messaging.MessageNotFoundException;
import org.springframework.jms.core.JmsOperations;

import javax.jms.Queue;
import java.util.Collection;

/**
 * Responsible for JMS operations like sending messages to queues or topics
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface JMSExtService {

    /**
     * Sends a message to a specific queue
     *
     * @param message     The message to be sent
     * @param destination The JMS destination
     */
    void sendMessageToQueue(JmsMessageDTO message, String destination);

    /**
     * Sends a message to a specific queue
     *
     * @param message     The message to be sent
     * @param destination The JMS destination
     */
    void sendMessageToQueue(JmsMessageDTO message, Queue destination);

    /**
     * Sends a Map message to a specific queue
     *
     * @param message     The message to be sent
     * @param destination The JMS destination
     */
    void sendMapMessageToQueue(JmsMessageDTO message, String destination);

    /**
     * Sends a Map message to a specific queue
     *
     * @param message       The message to be sent
     * @param destination   The JMS destination
     * @param jmsOperations the JMS operations to be used for sending
     */
    void sendMapMessageToQueue(JmsMessageDTO message, String destination, JmsOperations jmsOperations);

    /**
     * Sends a Map message to a specific queue
     *
     * @param message     The message to be sent
     * @param destination The JMS destination
     */
    void sendMapMessageToQueue(JmsMessageDTO message, Queue destination);

    /**
     * Sends a Map message to a specific queue
     *
     * @param message       The message to be sent
     * @param destination   The JMS destination
     * @param jmsOperations the JMS operations to be used for sending
     */
    void sendMapMessageToQueue(JmsMessageDTO message, Queue destination, JmsOperations jmsOperations);


    /**
     * Lists all messages pending for download by the backend
     *
     * @return a collection of messageIds pending for download
     */
    Collection<String> listPendingMessages(String queueName);

    /**
     * Removes the message with the corresponding id from the pending received messages
     *
     * @param messageId id of the message to be removed
     * @throws MessageNotFoundException if the message is not pending
     */
    void removeFromPending(String queueName, String messageId) throws MessageNotFoundException;
}
