package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.model.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class Ebms3UserMessageMapperImpl implements Ebms3UserMessageMapper{

    @Override
    public Ebms3UserMessage userMessageEntityToEbms3(UserMessage userMessage) {
        return null;
    }

    @Override
    public UserMessage userMessageEbms3ToEntity(Ebms3UserMessage ebms3UserMessage) {
        return null;
    }
}
