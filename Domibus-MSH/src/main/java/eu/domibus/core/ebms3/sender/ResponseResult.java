package eu.domibus.core.ebms3.sender;

import eu.domibus.ebms3.common.model.Messaging;

/**
 * This class holds the result of the {@link eu.domibus.core.ebms3.sender.ResponseHandler} check
 *
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public class ResponseResult {

    protected ResponseHandler.ResponseStatus responseStatus;
    protected Messaging responseMessaging;

    public ResponseResult() {
    }

    public ResponseResult(ResponseHandler.ResponseStatus responseStatus, Messaging responseMessaging) {
        this.responseStatus = responseStatus;
        this.responseMessaging = responseMessaging;
    }

    public ResponseHandler.ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseHandler.ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Messaging getResponseMessaging() {
        return responseMessaging;
    }

    public void setResponseMessaging(Messaging responseMessaging) {
        this.responseMessaging = responseMessaging;
    }
}
