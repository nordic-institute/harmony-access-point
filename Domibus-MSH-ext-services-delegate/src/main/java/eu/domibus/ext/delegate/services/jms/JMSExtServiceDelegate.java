package eu.domibus.ext.delegate.services.jms;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.messaging.MessageNotFoundException;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.util.Collection;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class JMSExtServiceDelegate implements JMSExtService {

    protected JMSManager jmsManager;

    protected DomibusExtMapper domibusExtMapper;

    public JMSExtServiceDelegate(JMSManager jmsManager, DomibusExtMapper domibusExtMapper) {
        this.jmsManager = jmsManager;
        this.domibusExtMapper = domibusExtMapper;
    }

    @Override
    public void sendMessageToQueue(JmsMessageDTO message, String destination) {
        final JmsMessage jmsMessage = domibusExtMapper.jmsMessageDTOToJmsMessage(message);
        jmsManager.sendMessageToQueue(jmsMessage, destination);
    }

    @Override
    public void sendMessageToQueue(JmsMessageDTO message, Queue destination) {
        final JmsMessage jmsMessage = domibusExtMapper.jmsMessageDTOToJmsMessage(message);
        jmsManager.sendMessageToQueue(jmsMessage, destination);
    }

    @Override
    public void sendMapMessageToQueue(JmsMessageDTO message, String destination) {
        final JmsMessage jmsMessage = domibusExtMapper.jmsMessageDTOToJmsMessage(message);
        jmsManager.sendMapMessageToQueue(jmsMessage, destination);
    }

    @Override
    public void sendMapMessageToQueue(JmsMessageDTO message, Queue destination) {
        final JmsMessage jmsMessage = domibusExtMapper.jmsMessageDTOToJmsMessage(message);
        jmsManager.sendMapMessageToQueue(jmsMessage, destination);
    }

    @Override
    public void sendMapMessageToQueue(JmsMessageDTO message, String destination, JmsOperations jmsOperations) {
        final JmsMessage jmsMessage = domibusExtMapper.jmsMessageDTOToJmsMessage(message);
        jmsManager.sendMapMessageToQueue(jmsMessage, destination, jmsOperations);
    }

    @Override
    public void sendMapMessageToQueue(JmsMessageDTO message, Queue destination, JmsOperations jmsOperations) {
        final JmsMessage jmsMessage = domibusExtMapper.jmsMessageDTOToJmsMessage(message);
        jmsManager.sendMapMessageToQueue(jmsMessage, destination, jmsOperations);
    }

    @Override
    public Collection<String> listPendingMessages(String queueName) {
        return jmsManager.listPendingMessages(queueName);
    }

    @Override
    public void removeFromPending(String queueName, String messageId) throws MessageNotFoundException {
        jmsManager.removeFromPending(queueName, messageId);
    }
}
