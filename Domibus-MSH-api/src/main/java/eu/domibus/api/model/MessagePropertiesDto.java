package eu.domibus.api.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
public class MessagePropertiesDto {

    protected Map<String, String> properties = new HashMap<>();

    public MessagePropertiesDto(UserMessage userMessage) {
        if (userMessage != null &&
                userMessage.getMessageProperties() != null &&
                userMessage.getMessageProperties().getProperty() != null) {
            userMessage.getMessageProperties()
                    .getProperty()
                    .forEach(property -> properties.put(property.getName(), property.getValue()));
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
