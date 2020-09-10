package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.core.ebms3.sender.interceptor.SetPolicyOutInterceptor;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.ebms3.common.model.MessageType;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.springframework.stereotype.Service;

/**
 * @author Thomas Dussart
 * @since 3.3
 * In case of a pulled message, the outgoing is a user message with attachements which should
 * received the same processing as the outPut of a push message.
 */
@Service
public class SetPolicyOutInterceptorServer extends SetPolicyOutInterceptor {
    public SetPolicyOutInterceptorServer() {
        super();
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        Object messageType = message.getExchange().get(MSHDispatcher.MESSAGE_TYPE_OUT);
        if (MessageType.USER_MESSAGE.equals(messageType)) {
            super.handleMessage(message);
        }
    }
}
