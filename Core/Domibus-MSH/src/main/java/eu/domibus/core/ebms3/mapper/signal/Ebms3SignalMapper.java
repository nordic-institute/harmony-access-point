package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3PullRequest;
import eu.domibus.api.ebms3.model.Ebms3Receipt;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.model.PullRequest;
import eu.domibus.api.model.ReceiptEntity;
import eu.domibus.api.model.SignalMessage;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
public interface Ebms3SignalMapper {

    Ebms3SignalMessage signalMessageEntityToEbms3(SignalMessage signalMessage);

    SignalMessage ebms3SignalMessageToEntity(Ebms3SignalMessage ebms3SignalMessage);

    PullRequest ebms3PullRequestToEntity(Ebms3SignalMessage ebms3SignalMessage);

    ReceiptEntity ebms3ReceiptToEntity(Ebms3SignalMessage ebms3SignalMessage);

    Ebms3PullRequest pullRequestToEbms3PullRequest(PullRequest pullReqeust);

    Ebms3Receipt receiptToEbms3Receipt(ReceiptEntity receiptEntity);
}
