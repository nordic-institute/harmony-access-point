package eu.domibus.api.model;

/**
 * Holds the result of the transformation of the Signal from ebms3 model to the internal model
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
public class SignalMessageResult {

    protected SignalMessage signalMessage;
    protected SignalMessageError signalMessageError;
    protected ReceiptEntity receiptEntity;
    protected PullRequest pullRequest;

    public SignalMessage getSignalMessage() {
        return signalMessage;
    }

    public void setSignalMessage(SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }

    public SignalMessageError getSignalMessageError() {
        return signalMessageError;
    }

    public void setSignalMessageError(SignalMessageError signalMessageError) {
        this.signalMessageError = signalMessageError;
    }

    public ReceiptEntity getReceiptEntity() {
        return receiptEntity;
    }

    public void setReceiptEntity(ReceiptEntity receiptEntity) {
        this.receiptEntity = receiptEntity;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public void setPullRequest(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }
}
