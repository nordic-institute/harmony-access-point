package eu.domibus.api.model;

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
