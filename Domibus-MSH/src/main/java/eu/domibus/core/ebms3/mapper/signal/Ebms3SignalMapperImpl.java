package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3MessageInfo;
import eu.domibus.api.ebms3.model.Ebms3PullRequest;
import eu.domibus.api.ebms3.model.Ebms3Receipt;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.model.PullRequest;
import eu.domibus.api.model.ReceiptEntity;
import eu.domibus.api.model.SignalMessage;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class Ebms3SignalMapperImpl implements Ebms3SignalMapper {

    @Override
    public Ebms3SignalMessage signalMessageEntityToEbms3(SignalMessage signalMessage) {
        Ebms3SignalMessage result = new Ebms3SignalMessage();

        final Ebms3MessageInfo ebms3MessageInfo = signalMessageToEbms3MessageInfo(signalMessage);
        result.setMessageInfo(ebms3MessageInfo);

        return result;
    }

    @Override
    public SignalMessage ebms3SignalMessageToEntity(Ebms3SignalMessage ebms3SignalMessage) {
        final SignalMessage signalMessage = new SignalMessage();
        final Ebms3MessageInfo messageInfo = ebms3SignalMessage.getMessageInfo();
        signalMessage.setSignalMessageId(messageInfo.getMessageId());
        signalMessage.setRefToMessageId(messageInfo.getRefToMessageId());
        signalMessage.setTimestamp(messageInfo.getTimestamp());
        return signalMessage;
    }

    @Override
    public PullRequest ebms3PullRequestToEntity(Ebms3SignalMessage ebms3SignalMessage) {
        final Ebms3PullRequest ebms3PullRequest = ebms3SignalMessage.getPullRequest();
        if(ebms3PullRequest == null) {
            return null;
        }

        PullRequest result = new PullRequest();
        result.setMpc(ebms3PullRequest.getMpc());
        return result;
    }

    @Override
    public ReceiptEntity ebms3ReceiptToEntity(Ebms3SignalMessage ebms3SignalMessage) {
        final Ebms3Receipt ebms3Receipt = ebms3SignalMessage.getReceipt();
        if(ebms3Receipt == null) {
            return null;
        }

        ReceiptEntity result = new ReceiptEntity();
        result.setRawXml(ebms3Receipt.getAny().get(0));
        return result;
    }

    protected Ebms3MessageInfo signalMessageToEbms3MessageInfo(SignalMessage signalMessage) {
        Ebms3MessageInfo result = new Ebms3MessageInfo();
        result.setMessageId(signalMessage.getSignalMessageId());
        result.setRefToMessageId(signalMessage.getRefToMessageId());
        result.setTimestamp(signalMessage.getTimestamp());
        return result;
    }

    @Override
    public Ebms3PullRequest pullRequestToEbms3PullRequest(PullRequest pullRequest) {
        if(pullRequest == null) {
            return null;
        }

        Ebms3PullRequest result = new Ebms3PullRequest();
        result.setMpc(pullRequest.getMpc());
        return result;
    }

    @Override
    public Ebms3Receipt receiptToEbms3Receipt(ReceiptEntity receiptEntity) {
        Ebms3Receipt result = new Ebms3Receipt();
        List<String> receiptList = new ArrayList<>();
        receiptList.add(new String(receiptEntity.getRawXml(), StandardCharsets.UTF_8));
        result.setAny(receiptList);
        return result;
    }
}
