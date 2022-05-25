package eu.domibus.core.ebms3.sender;

import eu.domibus.api.ebms3.model.Ebms3Messaging;

/**
 * This class holds the result of the {@link eu.domibus.core.ebms3.sender.ResponseHandler} check
 *
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public class ResponseResult {

    protected ResponseHandler.ResponseStatus responseStatus;
    protected Ebms3Messaging responseEbms3Messaging;

    public ResponseResult() {
    }

    public ResponseResult(ResponseHandler.ResponseStatus responseStatus, Ebms3Messaging responseEbms3Messaging) {
        this.responseStatus = responseStatus;
        this.responseEbms3Messaging = responseEbms3Messaging;
    }

    public ResponseHandler.ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseHandler.ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Ebms3Messaging getResponseMessaging() {
        return responseEbms3Messaging;
    }

    public void setResponseMessaging(Ebms3Messaging responseEbms3Messaging) {
        this.responseEbms3Messaging = responseEbms3Messaging;
    }
}
