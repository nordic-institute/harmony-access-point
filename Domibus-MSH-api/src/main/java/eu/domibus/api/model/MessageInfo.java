package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_MESSAGE_INFO")
@NamedQueries({
        @NamedQuery(name = "MessageInfo.findMessageIdsWithRefToMessageIds", query = "select mi.messageId from MessageInfo mi, SignalMessageLog sml where (sml.messageId=mi.messageId and sml.messageType=:MESSAGE_TYPE) and mi.refToMessageId in :MESSAGEIDS"),
        @NamedQuery(name = "MessageInfo.deleteMessages", query = "delete from MessageInfo mi where mi.messageId in :MESSAGEIDS"),
})
public class MessageInfo extends AbstractBaseEntity {

    public static final String MESSAGE_ID_CONTEXT_PROPERTY = "ebms.messageid";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TIME_STAMP")
    protected Date timestamp;

    @Column(name = "MESSAGE_ID", nullable = false, unique = true, updatable = false)
    @NotNull
    protected String messageId;

    @Column(name = "REF_TO_MESSAGE_ID")
    protected String refToMessageId;

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final Date value) {
        this.timestamp = value;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(final String value) {
        this.messageId = value;
    }

    public String getRefToMessageId() {
        return this.refToMessageId;
    }

    public void setRefToMessageId(final String value) {
        this.refToMessageId = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageInfo that = (MessageInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(timestamp, that.timestamp)
                .append(messageId, that.messageId)
                .append(refToMessageId, that.refToMessageId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(timestamp)
                .append(messageId)
                .append(refToMessageId)
                .toHashCode();
    }
}
