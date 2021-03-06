package eu.domibus.jms.spi;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public class InternalJmsMessage {

	public static final String MESSAGE_PRIORITY_USED = "messagePriorityUsed";

	public enum MessageType {
		TEXT_MESSAGE,
		MAP_MESSAGE
	}

	protected String id;
	protected String type;
	protected String jmsCorrelationId;
	protected String content;
	protected Date timestamp;
	protected Integer priority;
	protected MessageType messageType = MessageType.TEXT_MESSAGE;

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

	public Map<String, String> getProperties() {
		return properties;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getJmsCorrelationId() {
		return jmsCorrelationId;
	}

	public void setJmsCorrelationId(String jmsCorrelationId) {
		this.jmsCorrelationId = jmsCorrelationId;
	}


	//TODO separate between headers and properties
	public Map<String, String> getJMSProperties() {
		Map<String, String> jmsProperties = new HashMap<>();
		for (String key : properties.keySet()) {
			if (key.startsWith("JMS")) {
				jmsProperties.put(key, properties.get(key));
			}
		}
		return jmsProperties;
	}

	//TODO separate between headers and properties
	public Map<String, String> getCustomProperties() {
		Map<String, String> customProperties = new HashMap<>();
		for (String key : properties.keySet()) {
			if (!key.startsWith("JMS")) {
				customProperties.put(key, properties.get(key));
			}
		}
		return customProperties;
	}
	
	public Object getProperty(String name) {
		if(properties == null) {
			return null;
		}
		return properties.get(name);
	}

	public void setProperty(String key, String value){
		if (properties != null) {
			properties.put(key, value);
		}
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public String toString() {
		return new org.apache.commons.lang3.builder.ToStringBuilder(this)
				.append("id", id)
				.append("type", type)
				.append("content", content)
				.append("timestamp", timestamp)
				.append("properties", properties)
				.toString();
	}
}
