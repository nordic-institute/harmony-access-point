package eu.domibus;

import eu.domibus.core.message.UserMessageContextKeyProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@Primary
public class UserMessageContextKeyProviderMock implements UserMessageContextKeyProvider {

    final ThreadLocal<Properties> propertiesThreadLocal = new ThreadLocal<>();

    @Override
    public void setKeyOnTheCurrentMessage(String key, String value) {
        getProperties().setProperty(key, value);
    }

    private Properties getProperties() {
        Properties properties = propertiesThreadLocal.get();
        if(properties == null) {
            properties = new Properties();
            propertiesThreadLocal.set(properties);
        }
        return properties;
    }

    @Override
    public String getKeyFromTheCurrentMessage(String key) {
        return (String) getProperties().get(key);
    }
}
