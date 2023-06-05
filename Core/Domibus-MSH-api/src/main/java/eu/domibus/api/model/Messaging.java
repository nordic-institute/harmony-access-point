package eu.domibus.api.model;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
public class Messaging  {

    protected String id;
    protected SignalMessage signalMessage;
    protected UserMessage userMessage;

    public SignalMessage getSignalMessage() {
        return this.signalMessage;
    }

    public void setSignalMessage(final SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }

    public UserMessage getUserMessage() {
        return this.userMessage;
    }

    public void setUserMessage(final UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
