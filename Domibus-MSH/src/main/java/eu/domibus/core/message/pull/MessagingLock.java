package eu.domibus.core.message.pull;

import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.api.model.MessageState;
import eu.domibus.api.model.TimezoneOffset;
import eu.domibus.api.scheduler.Reprogrammable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static eu.domibus.api.model.MessageState.READY;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Entity
@Table(name = "TB_MESSAGING_LOCK")
@NamedQueries({
        @NamedQuery(name = "MessagingLock.findForMessageId",
                query = "select m from MessagingLock m where m.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessagingLock.delete",
                query = "delete from MessagingLock m where m.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessagingLock.findStalledMessages",
                query = "SELECT m from MessagingLock m where m.staled<:CURRENT_TIMESTAMP and messageState != 'DEL'"),
        @NamedQuery(name = "MessagingLock.findDeletedMessages",
                query = "SELECT m from MessagingLock m where messageState = 'DEL'"),
        @NamedQuery(name = "MessagingLock.findReadyToPull", query = "from MessagingLock where messageState = 'READY' and mpc=:MPC and lower(initiator)=lower(:INITIATOR) AND messageType='PULL' and nextAttempt<:CURRENT_TIMESTAMP and staled>:CURRENT_TIMESTAMP order by entityId"),
        @NamedQuery(name = "MessagingLock.findWaitingForReceipt", query = "from MessagingLock where messageState = 'WAITING' AND nextAttempt<:CURRENT_TIMESTAMP order by entityId")
})
@NamedNativeQuery(name = "MessagingLock.lockQuerySkipBlocked_Oracle",
        query = "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,FK_TIMEZONE_OFFSET,MESSAGE_STALED,CREATED_BY,CREATION_TIME,MODIFIED_BY,MODIFICATION_TIME " +
                "FROM TB_MESSAGING_LOCK ml " +
                "WHERE ml.MESSAGE_STATE='READY' " +
                "AND ml.MPC=:MPC " +
                "AND LOWER(ml.INITIATOR)=LOWER(:INITIATOR) " +
                "AND ml.MESSAGE_TYPE='PULL' " +
                "AND ml.NEXT_ATTEMPT<:CURRENT_TIMESTAMP " +
                "AND ml.MESSAGE_STALED>:CURRENT_TIMESTAMP " +
                "AND ml.ROWNUM <= 1 " +
                "FOR UPDATE SKIP LOCKED",
        resultClass = MessagingLock.class)
@NamedNativeQuery(name = "MessagingLock.lockQuerySkipBlocked_MySQL",
        query = "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,FK_TIMEZONE_OFFSET,MESSAGE_STALED,CREATED_BY,CREATION_TIME,MODIFIED_BY,MODIFICATION_TIME " +
                "FROM TB_MESSAGING_LOCK ml " +
                "WHERE ml.MESSAGE_STATE='READY' " +
                "AND ml.MPC=:MPC " +
                "AND LOWER(ml.INITIATOR)=LOWER(:INITIATOR) " +
                "AND ml.MESSAGE_TYPE='PULL' " +
                "AND ml.NEXT_ATTEMPT<:CURRENT_TIMESTAMP " +
                "AND ml.MESSAGE_STALED>:CURRENT_TIMESTAMP " +
                "LIMIT 1 " +
                "FOR UPDATE SKIP LOCKED ",
        resultClass = MessagingLock.class)
@NamedNativeQuery(name = "MessagingLock.lockByMessageId",
        query = "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,FK_TIMEZONE_OFFSET,MESSAGE_STALED,CREATED_BY,CREATION_TIME,MODIFIED_BY,MODIFICATION_TIME FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_ID=?1 ",
        resultClass = MessagingLock.class)
public class MessagingLock extends AbstractBaseEntity implements Reprogrammable {

    public final static String PULL = "PULL";

    @Column(name = "MESSAGE_TYPE")
    @NotNull
    private String messageType; //NOSONAR

    @Column(name = "MESSAGE_RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date received; //NOSONAR

    @Column(name = "MESSAGE_STATE")
    @Enumerated(EnumType.STRING)
    private MessageState messageState;

    @Column(name = "MESSAGE_ID")
    @NotNull
    private String messageId; //NOSONAR

    @Column(name = "INITIATOR")
    @NotNull
    private String initiator; //NOSONAR

    @Column(name = "MPC")
    @NotNull
    private String mpc; //NOSONAR

    @Column(name = "MESSAGE_STALED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date staled;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "FK_TIMEZONE_OFFSET")
    private TimezoneOffset timezoneOffset;

    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;

    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;

    public MessagingLock(             //NOSONAR
                                      final String messageId,
                                      final String initiator,
                                      final String mpc,
                                      final Date received,
                                      final Date staled,
                                      final Date nextAttempt,
                                      final TimezoneOffset timezoneOffset,
                                      final int sendAttempts,
                                      final int sendAttemptsMax) {
        this.received = received;
        this.staled = staled;
        this.messageId = messageId;
        this.initiator = initiator;
        this.mpc = mpc;
        this.messageType = PULL;
        this.messageState = READY;
        this.nextAttempt = nextAttempt;
        this.timezoneOffset = timezoneOffset;
        this.sendAttempts = sendAttempts;
        this.sendAttemptsMax = sendAttemptsMax;
    }

    public MessagingLock() {//NOSONAR
    }

    public String getMessageType() {
        return messageType;
    }

    public Date getReceived() {
        return received;
    }

    public String getMessageId() {
        return messageId;
    }

    public MessageState getMessageState() {
        return messageState;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getMpc() {
        return mpc;
    }

    public Date getStaled() {
        return staled;
    }

    @Override
    public Date getNextAttempt() {
        return nextAttempt;
    }

    @Override
    public TimezoneOffset getTimezoneOffset() {
        return timezoneOffset;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public int getSendAttemptsMax() {
        return sendAttemptsMax;
    }

    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }

    @Override
    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    @Override
    public void setTimezoneOffset(TimezoneOffset timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public void setSendAttempts(int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessagingLock that = (MessagingLock) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageType, that.messageType)
                .append(received, that.received)
                .append(messageState, that.messageState)
                .append(messageId, that.messageId)
                .append(initiator, that.initiator)
                .append(mpc, that.mpc)
                .append(staled, that.staled)
                .append(nextAttempt, that.nextAttempt)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageType)
                .append(received)
                .append(messageState)
                .append(messageId)
                .append(initiator)
                .append(mpc)
                .append(staled)
                .append(nextAttempt)
                .toHashCode();
    }

}
