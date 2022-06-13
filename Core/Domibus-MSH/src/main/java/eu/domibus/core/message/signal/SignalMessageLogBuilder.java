package eu.domibus.core.message.signal;

import eu.domibus.api.model.*;

/**
 * @author Federico Martini
 * @since 3.2
 */
public class SignalMessageLogBuilder {

    private SignalMessageLog signalMessageLog;

    public static SignalMessageLogBuilder create() {
        return new SignalMessageLogBuilder();
    }

    private SignalMessageLogBuilder() {
        this.signalMessageLog = new SignalMessageLog();
    }

    public SignalMessageLog build() {
        return signalMessageLog;
    }

    public SignalMessageLogBuilder setSignalMessage(SignalMessage signalMessage) {
        signalMessageLog.setSignalMessage(signalMessage);
        return this;
    }

    public SignalMessageLogBuilder setMessageStatus(MessageStatusEntity messageStatus) {
        signalMessageLog.setMessageStatus(messageStatus);
        return this;
    }

    public SignalMessageLogBuilder setMshRole(MSHRoleEntity mshRole) {
        signalMessageLog.setMshRole(mshRole);
        return this;
    }
}
