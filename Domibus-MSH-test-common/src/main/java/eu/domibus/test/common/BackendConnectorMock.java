package eu.domibus.test.common;

import eu.domibus.common.*;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public class BackendConnectorMock extends AbstractBackendConnector {

    private MessageReceiveFailureEvent messageReceiveFailureEvent;
    private PayloadSubmittedEvent payloadSubmittedEvent;
    private PayloadProcessedEvent payloadProcessedEvent;
    private MessageDeletedBatchEvent messageDeletedBatchEvent;

    public BackendConnectorMock(String name) {
        super(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        this.messageReceiveFailureEvent = messageReceiveFailureEvent;
    }

    @Override
    public void payloadSubmittedEvent(PayloadSubmittedEvent payloadSubmittedEvent) {
        this.payloadSubmittedEvent = payloadSubmittedEvent;
    }

    @Override
    public void payloadProcessedEvent(PayloadProcessedEvent payloadProcessedEvent) {
        this.payloadProcessedEvent = payloadProcessedEvent;
    }

    @Override
    public void messageDeletedBatchEvent(final MessageDeletedBatchEvent messageDeletedBatchEvent) {
        this.messageDeletedBatchEvent = messageDeletedBatchEvent;
    }

    @Override
    public MessageSubmissionTransformer getMessageSubmissionTransformer() {
        return null;
    }

    @Override
    public MessageRetrievalTransformer getMessageRetrievalTransformer() {
        return null;
    }

    @Override
    public void deliverMessage(final DeliverMessageEvent event) {
    }

    public void clear() {
        this.messageReceiveFailureEvent = null;
        this.payloadSubmittedEvent = null;
        this.payloadProcessedEvent = null;
        this.messageDeletedBatchEvent = null;
    }

    public MessageReceiveFailureEvent getMessageReceiveFailureEvent() {
        return messageReceiveFailureEvent;
    }

    public PayloadSubmittedEvent getPayloadSubmittedEvent() {
        return payloadSubmittedEvent;
    }

    public PayloadProcessedEvent getPayloadProcessedEvent() {
        return payloadProcessedEvent;
    }

    public MessageDeletedBatchEvent getMessageDeletedBatchEvent() {
        return messageDeletedBatchEvent;
    }
}
