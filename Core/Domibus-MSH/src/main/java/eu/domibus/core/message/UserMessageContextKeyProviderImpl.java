package eu.domibus.core.message;

import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.stereotype.Service;

@Service
public class UserMessageContextKeyProviderImpl implements UserMessageContextKeyProvider {

    @Override
    public void setKeyOnTheCurrentMessage(String key, String value) {
        PhaseInterceptorChain.getCurrentMessage().getExchange().put(key, value);
    }

    @Override
    public String getKeyFromTheCurrentMessage(String key) {
        return (String) PhaseInterceptorChain.getCurrentMessage().getExchange().get(key);
    }
}
