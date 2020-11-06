package eu.domibus.common.model.logging;

import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.UserMessage;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Entity
@Table(name = "TB_USER_MESSAGE_LOG")
//@DiscriminatorValue("USER_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "UserMessageLog.findRetryMessages",
                query = "select userMessageLog.messageId " +
                        "from UserMessageLog userMessageLog " +
                        "where userMessageLog.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY " +
                        "and userMessageLog.nextAttempt < :CURRENT_TIMESTAMP " +
                        "and 1 <= userMessageLog.sendAttempts " +
                        "and userMessageLog.sendAttempts <= userMessageLog.sendAttemptsMax " +
                        "and (userMessageLog.sourceMessage is null or userMessageLog.sourceMessage=false)" +
                        "and (userMessageLog.scheduled is null or userMessageLog.scheduled=false)"),
        @NamedQuery(name = "UserMessageLog.findReadyToPullMessages", query = "SELECT mi.messageInfo.messageId, mi.messageInfo.timestamp FROM UserMessageLog as um, UserMessage mi where um.messageStatus=eu.domibus.common.MessageStatus.READY_TO_PULL and um.messageId=mi.messageInfo.messageId order by mi.messageInfo.timestamp desc"),//TODO Fix me
        @NamedQuery(name = "UserMessageLog.getMessageStatus", query = "select userMessageLog.messageStatus from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.setMessageStatus", query = "update UserMessageLog message set message.messageStatus = :STATUS where message.entityId = :ID"),
        @NamedQuery(name = "UserMessageLog.setAsScheduled", query = "update UserMessageLog message set message.scheduled = true where message.entityId = :ID"),
        @NamedQuery(name = "UserMessageLog.setAsNotified", query = "update UserMessageLog message set message.notificationStatus = :STATUS where message.entityId = :ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageId", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageIdAndRole", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID and userMessageLog.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.messageExist", query = "select count(userMessageLog) from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID and userMessageLog.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.findBackendForMessage", query = "select userMessageLog.backend from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findEntries", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findUndownloadedUserMessagesOlderThan", query = "select userMessageLog.messageId from UserMessageLog userMessageLog where (userMessageLog.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or userMessageLog.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and userMessageLog.deleted is null and userMessageLog.mpc = :MPC and userMessageLog.received < :DATE"),
        @NamedQuery(name = "UserMessageLog.findDownloadedUserMessagesOlderThan", query = "select userMessageLog.messageId from UserMessageLog userMessageLog where (userMessageLog.messageStatus = eu.domibus.common.MessageStatus.DOWNLOADED) and userMessageLog.mpc = :MPC and userMessageLog.downloaded is not null and userMessageLog.downloaded < :DATE"),
        @NamedQuery(name = "UserMessageLog.setNotificationStatus", query = "update UserMessageLog userMessageLog set userMessageLog.notificationStatus=:NOTIFICATION_STATUS where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.countEntries", query = "select count(userMessageLog.messageId) from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.setMessageStatusAndNotificationStatus",
                query = "update UserMessageLog userMessageLog set userMessageLog.deleted=:TIMESTAMP, userMessageLog.messageStatus=:MESSAGE_STATUS, userMessageLog.notificationStatus=:NOTIFICATION_STATUS where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findAllInfo", query = "select userMessageLog from UserMessageLog userMessageLog")
})
public class UserMessageLog extends MessageLog {


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "ID_PK")
    protected UserMessage userMessage;

    @Column(name = "SOURCE_MESSAGE")
    protected Boolean sourceMessage;

    @Column(name = "MESSAGE_FRAGMENT")
    protected Boolean messageFragment;

    @Column(name = "SCHEDULED")
    protected Boolean scheduled;

    public UserMessageLog() {
        setMessageType(MessageType.USER_MESSAGE);
        setReceived(new Date());
        setSendAttempts(0);
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public Boolean getSourceMessage() {
        return BooleanUtils.toBoolean(sourceMessage);
    }

    public void setSourceMessage(Boolean sourceMessage) {
        this.sourceMessage = sourceMessage;
    }

    public Boolean getMessageFragment() {
        return BooleanUtils.toBoolean(messageFragment);
    }

    public void setMessageFragment(Boolean messageFragment) {
        this.messageFragment = messageFragment;
    }

    public Boolean getScheduled() {
        return scheduled;
    }

    public void setScheduled(Boolean scheduled) {
        this.scheduled = scheduled;
    }

    public Boolean isSplitAndJoin() {
        return getSourceMessage() || getMessageFragment();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserMessageLog that = (UserMessageLog) o;

        return new EqualsBuilder()
                .append(messageId, that.messageId)
                .append(mshRole, that.mshRole)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageId)
                .append(mshRole)
                .toHashCode();
    }
}