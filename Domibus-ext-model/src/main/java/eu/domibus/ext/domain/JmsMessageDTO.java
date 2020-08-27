package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class JmsMessageDTO {

    protected String id;
    protected String jmsCorrelationId;
    protected String type;
    protected Date timestamp;
    protected String content;
    protected Map<String, Object> properties = new HashMap<>();

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

    public void setProperty(String name, Object value) {

        if (!isValidObjectType(value)){
            throw new IllegalArgumentException("Unsupported value type: ["+ value.getClass()+"] for JMS property name: ["+name+"]. Only objectified primitive objects and String types are allowed!");
        }
        // convert objectified primitive objects to string
        properties.put(name, String.valueOf(value));
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {

        // get invalid properties
        Map<String, Object> invalidProperties = properties.entrySet().stream()
                .filter(e -> !isValidObjectType(e.getValue())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue ));

        if (!invalidProperties.isEmpty()) {
            StringWriter sw = new StringWriter();
            invalidProperties.forEach((name, value) ->{
                    sw.append("Unsupported value type: ["+ value.getClass()+"] for JMS property name: ["+name+"].\n");
            });
            sw.append("Only objectified primitive objects and String types are allowed!");
            throw new IllegalArgumentException(sw.toString());
        }
        //Convert objects to string.
        Map<String,Object> newProperties = properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
        this.properties = newProperties;
    }

    public String getStringProperty(String key) {
        return (String) properties.get(key);
    }

    public String getJmsCorrelationId() {
        return jmsCorrelationId;
    }

    public void setJmsCorrelationId(String jmsCorrelationId) {
        this.jmsCorrelationId = jmsCorrelationId;
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

    /**
     * To ensure backward compatibility allow all objectified primitive objects and String types.
     *
     * @param value: check class type for the object
     * @return true if valid object else return false.
     */
    protected boolean isValidObjectType(Object value) {
        boolean valid = value instanceof Boolean || value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long;
        valid = valid || value instanceof Float || value instanceof Double || value instanceof Character || value instanceof String || value == null;
        return valid;
    }
}
