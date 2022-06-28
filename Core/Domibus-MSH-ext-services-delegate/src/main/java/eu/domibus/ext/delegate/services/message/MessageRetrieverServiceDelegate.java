package eu.domibus.ext.delegate.services.message;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageStatus;
import eu.domibus.ext.services.MessageRetrieverExtService;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageRetriever;

import java.util.List;

public class MessageRetrieverServiceDelegate implements MessageRetrieverExtService {

    private final MessageRetriever messageRetriever;

    public MessageRetrieverServiceDelegate(MessageRetriever messageRetriever) {

        this.messageRetriever = messageRetriever;
    }


    @Override
    public Submission downloadMessage(String messageId) throws MessageNotFoundException {
        return messageRetriever.downloadMessage(messageId);
    }

    @Override
    public Submission downloadMessage(Long messageEntityId) throws MessageNotFoundException {
        return messageRetriever.downloadMessage(messageEntityId);
    }

    @Override
    public Submission browseMessage(String messageId) throws MessageNotFoundException {
        return messageRetriever.browseMessage(messageId);
    }

    @Override
    public Submission browseMessage(Long messageEntityId) throws MessageNotFoundException {
        return messageRetriever.browseMessage(messageEntityId);
    }

    @Override
    public MessageStatus getStatus(String messageId) {
        return messageRetriever.getStatus(messageId);
    }

    @Override
    public MessageStatus getStatus(Long messageEntityId) {
        return messageRetriever.getStatus(messageEntityId);
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(String messageId) {
        return messageRetriever.getErrorsForMessage(messageId);
    }
}
