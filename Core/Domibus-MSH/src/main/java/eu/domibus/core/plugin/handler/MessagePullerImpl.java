package eu.domibus.core.plugin.handler;

import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.plugin.handler.MessagePuller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service used for initiating pull requests through this interface (split from DatabaseMessageHandler)
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class MessagePullerImpl implements MessagePuller {
    @Autowired
    private MessageExchangeService messageExchangeService;

    @Override
    @Transactional
    public void initiatePull(String mpc) {
        messageExchangeService.initiatePullRequest(mpc);
    }

}
