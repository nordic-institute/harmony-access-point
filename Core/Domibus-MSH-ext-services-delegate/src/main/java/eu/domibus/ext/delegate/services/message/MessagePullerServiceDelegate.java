package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.services.MessagePullerExtService;
import eu.domibus.plugin.handler.MessagePuller;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Delegate class to enable access of plugins to the MessagePuller
 */
@Service
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
