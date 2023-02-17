package eu.domibus.test.common;

import eu.domibus.common.*;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import java.util.UUID;

import static eu.domibus.messaging.MessageConstants.*;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public class BackendConnectorMock extends AbstractBackendConnector {

    private MessageReceiveFailureEvent messageReceiveFailureEvent;
    private PayloadSubmittedEvent payloadSubmittedEvent;
    private PayloadProcessedEvent payloadProcessedEvent;
    private MessageDeletedBatchEvent messageDeletedBatchEvent;
    private DeliverMessageEvent deliverMessageEvent;
    private MessageSendFailedEvent messageSendFailedEvent;
    private MessageSendSuccessEvent messageSendSuccessEvent;

    private MessageResponseSentEvent messageResponseSentEvent;

    public static String MESSAGE_ID = UUID.randomUUID() + "@domibus.eu";


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
    public void messageResponseSent(final MessageResponseSentEvent messageResponseSentEvent) {
        this.messageResponseSentEvent = messageResponseSentEvent;
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
    public MessageSubmissionTransformer<?> getMessageSubmissionTransformer() {
        return null;
    }

    @Override
    public MessageRetrievalTransformer<?> getMessageRetrievalTransformer() {
        return null;
    }

    @Override
    public void deliverMessage(final DeliverMessageEvent deliverMessageEvent) {
        this.deliverMessageEvent = deliverMessageEvent;
    }

    @Override
    public void messageSendFailed(final MessageSendFailedEvent messageSendFailedEvent) {
        this.messageSendFailedEvent = messageSendFailedEvent;
    }

    @Override
    public void messageSendSuccess(final MessageSendSuccessEvent messageSendSuccessEvent) {
        this.messageSendSuccessEvent = messageSendSuccessEvent;
    }

    public void clear() {
        this.messageReceiveFailureEvent = null;
        this.payloadSubmittedEvent = null;
        this.payloadProcessedEvent = null;
        this.messageDeletedBatchEvent = null;
        this.deliverMessageEvent = null;
        this.messageSendFailedEvent = null;
        this.messageSendSuccessEvent = null;
        this.messageResponseSentEvent = null;
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

    public DeliverMessageEvent getDeliverMessageEvent() {
        DeliverMessageEvent deliverMessageEvent = new DeliverMessageEvent();
        deliverMessageEvent.setMessageId(MESSAGE_ID);
        deliverMessageEvent.getProps().put(MSH_ROLE, "RECEIVING");
        deliverMessageEvent.getProps().put(CONVERSATION_ID, "CONVERSATION_ID");
        deliverMessageEvent.getProps().put(FROM_PARTY_ID, "FROM_PARTY_ID");
        deliverMessageEvent.getProps().put(TO_PARTY_ID, "TO_PARTY_ID");
        deliverMessageEvent.getProps().put(ORIGINAL_SENDER, "ORIGINAL_SENDER");
        deliverMessageEvent.getProps().put(FINAL_RECIPIENT, "FINAL_RECIPIENT");
        deliverMessageEvent.getProps().put(SERVICE, "SERVICE");
        deliverMessageEvent.getProps().put(SERVICE_TYPE, "SERVICE_TYPE");
        deliverMessageEvent.getProps().put(ACTION, "ACTION");
        return deliverMessageEvent;
    }

    public MessageSendFailedEvent getMessageSendFailedEvent() {
        return messageSendFailedEvent;
    }

    public MessageSendSuccessEvent getMessageSendSuccessEvent() {
        return messageSendSuccessEvent;
    }

    public MessageResponseSentEvent getMessageReceiveReplySentEvent() {
        return messageResponseSentEvent;
    }

    @Override
    public boolean isEnabled(final String domainCode) {
        return true;
    }

    @Override
    public void setEnabled(final String domainCode, final boolean enabled) {
    }

}
