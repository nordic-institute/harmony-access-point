package eu.domibus.core.ebms3.mapper;

import eu.domibus.api.ebms3.model.*;
import eu.domibus.api.model.*;
import eu.domibus.core.ebms3.mapper.signal.Ebms3SignalMapper;
import eu.domibus.core.ebms3.mapper.usermessage.Ebms3UserMessageMapper;
import org.apache.commons.collections4.CollectionUtils;
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
        return ebms3SignalMapper.ebms3SignalMessageToEntity(ebms3SignalMessage);
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
        SignalMessageResult result = new SignalMessageResult();
        final SignalMessage signalMessage = convertFromEbms3(ebms3Messaging.getSignalMessage());
        result.setSignalMessage(signalMessage);
        final SignalMessageError signalMessageError = convertSignalMessageError(ebms3Messaging);
        result.setSignalMessageError(signalMessageError);
        final ReceiptEntity receiptEntity = ebms3SignalMapper.ebms3ReceiptToEntity(ebms3Messaging.getSignalMessage());
        if (receiptEntity != null) {
            receiptEntity.setSignalMessage(signalMessage);
        }
        result.setReceiptEntity(receiptEntity);
        final PullRequest pullRequest = ebms3SignalMapper.ebms3PullRequestToEntity(ebms3Messaging.getSignalMessage());
        result.setPullRequest(pullRequest);

        return result;
    }

    public SignalMessageError convertSignalMessageError(final Ebms3Messaging messaging) {
        final Ebms3SignalMessage signalMessage = messaging.getSignalMessage();
        if (signalMessage == null || CollectionUtils.isEmpty(signalMessage.getError())) {
            return null;
        }

        final Ebms3Error error = signalMessage.getError().iterator().next();
        if (error == null) {
            return null;
        }

        SignalMessageError result = new SignalMessageError();
        result.setErrorCode(error.getErrorCode());
        result.setErrorDetail(error.getErrorDetail());
        result.setCategory(error.getCategory());
        result.setRefToMessageInError(error.getRefToMessageInError());
        result.setOrigin(error.getOrigin());
        result.setSeverity(error.getSeverity());
        result.setShortDescription(error.getShortDescription());
        final Ebms3Description description = error.getDescription();
        if (description != null) {
            result.setSignalDescription(description.getValue());
            result.setSignalDescriptionLang(description.getLang());
        }

        return result;
    }

/*    public Ebms3Messaging convertToEbms3(UserMessage userMessage) {
        return null;//ebms3MessagingMapper.messagingEntityToEbms3(userMessage);
    }*/
}
