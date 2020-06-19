package eu.domibus.common;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public class MessageReceiveFailureEvent {

    protected String messageId;
    protected String endpoint;
    protected String service;
    protected String serviceType;
    protected String action;
    protected ErrorResult errorResult;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public ErrorResult getErrorResult() {
        return errorResult;
    }

    public void setErrorResult(ErrorResult errorResult) {
        this.errorResult = errorResult;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
