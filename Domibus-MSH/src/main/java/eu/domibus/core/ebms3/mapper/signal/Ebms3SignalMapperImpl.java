package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.model.SignalMessage;
import org.springframework.stereotype.Service;

@Service
public class Ebms3SignalMapperImpl implements Ebms3SignalMapper {

    @Override
    public Ebms3SignalMessage signalMessageEntityToEbms3(SignalMessage signalMessage) {
        return null;
    }

    @Override
    public SignalMessage signalMessageEbms3ToEntity(Ebms3SignalMessage ebms3SignalMessage) {
        return null;
    }
}
