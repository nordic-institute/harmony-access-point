package eu.domibus.api.jms;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public class JmsMessage {

    public static final String PROPERTY_ORIGINAL_QUEUE = "originalQueue";
    // order of the fields is important for CSV generation
    protected String id;
    protected String type;
    protected String jmsCorrelationId;
    protected Date timestamp;
    protected String content;
    protected Integer priority;
    protected Map<String, String> customProperties;
    protected Map<String, String> properties = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public <T> T getProperty(String name) {
        return (T) properties.get(name);
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String getJmsCorrelationId() {
        return jmsCorrelationId;
    }

    public void setJmsCorrelationId(String jmsCorrelationId) {
        this.jmsCorrelationId = jmsCorrelationId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Map<String, String> getJMSProperties() {
        Map<String, String> jmsProperties = new HashMap<>();
        for (String key : properties.keySet()) {
            if (key.startsWith("JMS")) {
                jmsProperties.put(key, properties.get(key));
            }
        }
        return jmsProperties;
    }

    public Map<String, String> getCustomProperties() {
        if (customProperties == null) {
            customProperties = new HashMap<>();
            for (String key : properties.keySet()) {
                if (!key.startsWith("JMS")) {
                    customProperties.put(key, properties.get(key));
                }
            }
        }
        return customProperties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getStringProperty(String key) {
        return (String) properties.get(key);
    }

    public String getCustomStringProperty(String key) {
        return (String) getCustomProperties().get(key);
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("content", content)
                .append("timestamp", timestamp)
                .append("properties", properties)
                .toString();
    }
}
