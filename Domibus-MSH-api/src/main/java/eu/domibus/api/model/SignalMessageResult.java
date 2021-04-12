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
}
