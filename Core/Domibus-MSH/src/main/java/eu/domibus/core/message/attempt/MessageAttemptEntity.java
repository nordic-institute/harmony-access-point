package eu.domibus.core.message.attempt;

import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.api.model.UserMessage;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Entity
@Table(name = "TB_SEND_ATTEMPT")
@NamedQueries({
        @NamedQuery(name = "MessageAttemptEntity.findAttemptsByMessageId",
                query = "select attempt from MessageAttemptEntity attempt where attempt.userMessage.messageId = :MESSAGE_ID"),
        @NamedQuery(name = "MessageAttemptEntity.findAttemptsByMessageId2",
                query = "select attempt from MessageAttemptEntity attempt where attempt.userMessage.messageId = :MESSAGE_ID and attempt.userMessage.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "MessageAttemptEntity.deleteAttemptsByMessageIds",
                query = "delete from MessageAttemptEntity attempt where attempt.userMessage.entityId IN :IDS"),
})
public class MessageAttemptEntity extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_MESSAGE_ID_FK")
    protected UserMessage userMessage;

    @Column(name = "START_DATE")
    private Timestamp startDate;

    @Column(name = "END_DATE")
    private Timestamp endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private MessageAttemptStatus status;

    @Column(name = "ERROR")
    private String error;

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public MessageAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(MessageAttemptStatus status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAttemptEntity that = (MessageAttemptEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(startDate, that.startDate)
                .append(endDate, that.endDate)
                .append(status, that.status)
                .append(error, that.error)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(startDate)
                .append(endDate)
                .append(status)
                .append(error)
                .toHashCode();
    }
}
