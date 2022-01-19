package eu.domibus.test.common;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.PayloadProcessedEvent;
import eu.domibus.common.PayloadSubmittedEvent;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public class BackendConnectorMock extends AbstractBackendConnector {

    private MessageReceiveFailureEvent messageReceiveFailureEvent;
    private PayloadSubmittedEvent payloadSubmittedEvent;
    private PayloadProcessedEvent payloadProcessedEvent;

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
}
