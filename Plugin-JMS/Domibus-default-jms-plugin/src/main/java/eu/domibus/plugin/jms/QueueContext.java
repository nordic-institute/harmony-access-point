package eu.domibus.plugin.jms;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Class responsible for holding the queue context(message id, service, action, etc)
 *
 * @since 4.2
 * @author Cosmin Baciu
 */
public class QueueContext {

    protected String messageId;

    protected String service;

    protected String action;

    protected String jmsCorrelationId;

    public QueueContext(String messageId, String service, String action, String jmsCorrelationId) {
        this.messageId = messageId;
        this.service = service;
        this.action = action;
        this.jmsCorrelationId = jmsCorrelationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getJmsCorrelationId() {
        return jmsCorrelationId;
    }

    public void setJmsCorrelationId(String jmsCorrelationId) {
        this.jmsCorrelationId= jmsCorrelationId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("service", service)
                .append("action", action)
                .append("jmsCorrelationId", jmsCorrelationId)
                .toString();
    }
}
