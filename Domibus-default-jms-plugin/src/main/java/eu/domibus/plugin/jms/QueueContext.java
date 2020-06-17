package eu.domibus.plugin.jms;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @since 4.2
 * @author Cosmin Baciu
 */
public class QueueContext {

    protected String messageId;

    protected String service;

    protected String action;

    public QueueContext(String messageId, String service, String action) {
        this.messageId = messageId;
        this.service = service;
        this.action = action;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("service", service)
                .append("action", action)
                .toString();
    }
}
