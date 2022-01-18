package eu.domibus.test.common;

import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public class MessageReceivePluginMock extends AbstractBackendConnector {

    private MessageReceiveFailureEvent event;

    public MessageReceivePluginMock(String name) {
        super(name);
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        this.event = messageReceiveFailureEvent;
    }

    @Override
    public MessageSubmissionTransformer getMessageSubmissionTransformer() {
        return null;
    }

    @Override
    public MessageRetrievalTransformer getMessageRetrievalTransformer() {
        return null;
    }

    public void clear() {
        this.event = null;
    }

    public MessageReceiveFailureEvent getEvent() {
        return event;
    }
}
