package eu.domibus.api.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_SIGNAL_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "SignalMessage.findSignalMessageIdByRefMessageId",
                query = "select signalMessage.messageInfo.messageId from SignalMessage signalMessage where signalMessage.messageInfo.refToMessageId = :ORI_MESSAGE_ID"),
        @NamedQuery(name = "SignalMessage.findSignalMessageByRefMessageId",
                query = "select signalMessage from SignalMessage signalMessage where signalMessage.messageInfo.refToMessageId = :ORI_MESSAGE_ID"),
        @NamedQuery(name = "SignalMessage.findReceiptIdsByMessageIds",
                query = "select signalMessage.receipt.entityId from SignalMessage signalMessage where signalMessage.messageInfo.messageId IN :MESSAGEIDS"),
})
public class SignalMessage extends AbstractBaseEntity {

    @OneToOne(cascade = CascadeType.ALL)
    protected MessageInfo messageInfo;

    @Embedded
    protected PullRequest pullRequest; //NOSONAR

    @OneToOne(cascade = CascadeType.ALL)
    protected ReceiptEntity receipt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "SIGNALMESSAGE_ID")
    protected Set<Error> error; //NOSONAR

    @OneToOne(mappedBy = "signalMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RawEnvelopeLog rawEnvelopeLog;

    public MessageInfo getMessageInfo() {
        return this.messageInfo;
    }

    public void setMessageInfo(final MessageInfo value) {
        this.messageInfo = value;
    }

    public PullRequest getPullRequest() {
        return this.pullRequest;
    }

    public void setPullRequest(final PullRequest value) {
        this.pullRequest = value;
    }

    public ReceiptEntity getReceipt() {
        return this.receipt;
    }

    public void setReceipt(final ReceiptEntity value) {
        this.receipt = value;
    }

    public Set<Error> getError() {
        if (this.error == null) {
            this.error = new HashSet<>();
        }
        return this.error;
    }

    public RawEnvelopeLog getRawEnvelopeLog() {
        return rawEnvelopeLog;
    }
}
