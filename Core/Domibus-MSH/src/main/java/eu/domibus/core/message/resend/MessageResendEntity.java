package eu.domibus.core.message.resend;

import eu.domibus.api.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "TB_MESSAGES_TO_RESEND")
@NamedQueries({
@NamedQuery(name = "MessageResendEntity.findAllMessageIds", query = "select messageResendEntity.messageId from MessageResendEntity messageResendEntity"),
        @NamedQuery(name = "MessageResendEntity.findByMessageId", query = "select messageResendEntity.messageId from MessageResendEntity messageResendEntity where messageResendEntity.messageId=:MESSAGE_ID"),
@NamedQuery(name = "MessageResendEntity.delete",
                query = "delete from MessageResendEntity messageResendEntity where messageResendEntity.messageId=:MESSAGE_ID")
})
public class MessageResendEntity extends AbstractBaseEntity {

    @NotNull
    @Column(name = "MESSAGE_ID")
    private String messageId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageResendEntity that = (MessageResendEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageId, that.messageId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageId", messageId)
                .toString();
    }
}
