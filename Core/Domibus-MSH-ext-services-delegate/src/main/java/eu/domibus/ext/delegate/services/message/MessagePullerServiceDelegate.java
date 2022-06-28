package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.services.MessagePullerExtService;
import eu.domibus.plugin.handler.MessagePuller;

public class MessagePullerServiceDelegate implements MessagePullerExtService {
    private final MessagePuller messagePuller;

    public MessagePullerServiceDelegate(MessagePuller messagePuller) {
        this.messagePuller = messagePuller;
    }

    @Override
    public void initiatePull(String mpc) {
        messagePuller.initiatePull(mpc);
    }
}
