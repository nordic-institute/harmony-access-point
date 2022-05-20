package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;


/**
 * @author Federico Martini
 * @since 3.2
 */
@Entity
@Table(name = "TB_SIGNAL_MESSAGE_LOG")
@NamedQueries({
        @NamedQuery(name = "SignalMessageLog.findByMessageId", query = "select signalMessageLog from SignalMessageLog signalMessageLog where signalMessageLog.signalMessage.signalMessageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.findByMessageIdAndRole", query = "select signalMessageLog from SignalMessageLog signalMessageLog where signalMessageLog.signalMessage.signalMessageId=:MESSAGE_ID and signalMessageLog.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "SignalMessageLog.deleteMessageLogs", query = "delete from SignalMessageLog sml where sml.signalMessage.entityId in :IDS"),
})
public class SignalMessageLog extends AbstractNoGeneratedPkEntity {

    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    @Column(name = "DELETED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MSH_ROLE_ID_FK")
    private MSHRoleEntity mshRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGE_STATUS_ID_FK")
    private MessageStatusEntity messageStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PK")
    @MapsId
    private SignalMessage signalMessage;

    public SignalMessageLog() {
        setReceived(new Date());
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public MSHRoleEntity getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRoleEntity mshRole) {
        this.mshRole = mshRole;
    }

    public MessageStatusEntity getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatusEntity messageStatus) {
        this.messageStatus = messageStatus;
    }

    public SignalMessage getSignalMessage() {
        return signalMessage;
    }

    public void setSignalMessage(SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SignalMessageLog that = (SignalMessageLog) o;

        return new EqualsBuilder()
                .append(received, that.received)
                .append(deleted, that.deleted)
                .append(mshRole, that.mshRole)
                .append(messageStatus, that.messageStatus)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(received)
                .append(deleted)
                .append(mshRole)
                .append(messageStatus)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("received", received)
                .append("deleted", deleted)
                .append("mshRole", mshRole)
                .append("messageStatus", messageStatus)
                .toString();
    }
}


