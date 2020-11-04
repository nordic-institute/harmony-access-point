package eu.domibus.core.ebms3;

import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class Ebms3Converter {

    public eu.domibus.api.ebms3.model.SignalMessage convertToEbms3(SignalMessage signalMessage) {
        //TODO cosmin
        return null;
    }

    public eu.domibus.api.ebms3.model.UserMessage convertToEbms3(UserMessage userMessage) {
        //TODO cosmin
        return null;
    }

    public Messaging convertFromEbms3(eu.domibus.api.ebms3.model.Messaging messaging) {
        //TODO cosmin
        return null;
    }

    public SignalMessage convertFromEbms3(eu.domibus.api.ebms3.model.SignalMessage messaging) {
        //TODO cosmin
        return null;
    }
}
