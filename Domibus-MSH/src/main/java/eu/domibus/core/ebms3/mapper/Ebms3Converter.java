package eu.domibus.core.ebms3.mapper;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.model.*;
import eu.domibus.core.ebms3.mapper.signal.Ebms3SignalMapper;
import eu.domibus.core.ebms3.mapper.usermessage.Ebms3UserMessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Service
public class Ebms3Converter {

    protected Ebms3SignalMapper ebms3SignalMapper;
    protected Ebms3UserMessageMapper ebms3UserMessageMapper;

    public Ebms3Converter(Ebms3SignalMapper ebms3SignalMapper,
                          Ebms3UserMessageMapper ebms3UserMessageMapper) {
        this.ebms3SignalMapper = ebms3SignalMapper;
        this.ebms3UserMessageMapper = ebms3UserMessageMapper;
    }

    public Ebms3SignalMessage convertToEbms3(SignalMessage signalMessage) {
        return ebms3SignalMapper.signalMessageEntityToEbms3(signalMessage);
    }

    public SignalMessage convertFromEbms3(Ebms3SignalMessage ebms3SignalMessage) {
        return ebms3SignalMapper.signalMessageEbms3ToEntity(ebms3SignalMessage);
    }

    public Ebms3UserMessage convertToEbms3(UserMessage userMessage, List<PartInfo> partInfoList) {
        return ebms3UserMessageMapper.userMessageEntityToEbms3(userMessage, partInfoList);
    }

    public UserMessage convertFromEbms3(Ebms3UserMessage ebms3UserMessage) {
        return ebms3UserMessageMapper.userMessageEbms3ToEntity(ebms3UserMessage);
    }
    public List<PartInfo> convertPartInfoFromEbms3(Ebms3UserMessage ebms3UserMessage) {
        return ebms3UserMessageMapper.partInfoEbms3ToEntity(ebms3UserMessage);
    }

    public SignalMessageResult convertFromEbms3(Ebms3Messaging ebms3Messaging) {
        //TODO
        return null;//ebms3MessagingMapper.messagingEbms3ToEntity(ebms3Messaging);
    }

/*    public Ebms3Messaging convertToEbms3(UserMessage userMessage) {
        return null;//ebms3MessagingMapper.messagingEntityToEbms3(userMessage);
    }*/
}
