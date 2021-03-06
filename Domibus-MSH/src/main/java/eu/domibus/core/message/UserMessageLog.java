package eu.domibus.core.message;

import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.MessageType;
import org.apache.commons.lang3.BooleanUtils;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Entity
@Table(name = "TB_MESSAGE_LOG")
@DiscriminatorValue("USER_MESSAGE")
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
        @NamedQuery(name = "UserMessageLog.findReadyToPullMessages", query = "SELECT mi.messageId,mi.timestamp FROM UserMessageLog as um ,MessageInfo mi where um.messageStatus=eu.domibus.common.MessageStatus.READY_TO_PULL and um.messageId=mi.messageId order by mi.timestamp desc"),
        @NamedQuery(name = "UserMessageLog.getMessageStatus", query = "select userMessageLog.messageStatus from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageId", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageIdAndRole", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID and userMessageLog.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.findBackendForMessage", query = "select userMessageLog.backend from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findEntries", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findDeletedUserMessagesOlderThan", query = "select new eu.domibus.core.message.UserMessageLogDto(uml.messageInfo.messageId, uml.messageSubtype, uml.backend) from UserMessageLog uml where uml.messageStatus = eu.domibus.common.MessageStatus.DELETED and uml.deleted is not null and uml.mpc = :MPC and uml.deleted < :DATE"),
        @NamedQuery(name = "UserMessageLog.findUndownloadedUserMessagesOlderThan", query = "select new eu.domibus.core.message.UserMessageLogDto(uml.messageInfo.messageId, uml.messageSubtype, uml.backend) from UserMessageLog uml where (uml.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or uml.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and uml.deleted is null and uml.mpc = :MPC and uml.received < :DATE"),
        @NamedQuery(name = "UserMessageLog.findDownloadedUserMessagesOlderThan", query = "select new eu.domibus.core.message.UserMessageLogDto(uml.messageInfo.messageId, uml.messageSubtype, uml.backend) from UserMessageLog uml where (uml.messageStatus = eu.domibus.common.MessageStatus.DOWNLOADED) and uml.mpc = :MPC and uml.downloaded is not null and uml.downloaded < :DATE"),
        @NamedQuery(name = "UserMessageLog.findSentUserMessagesWithPayloadNotClearedOlderThan", query = "select new eu.domibus.core.message.UserMessageLogDto(uml.messageInfo.messageId, uml.messageSubtype, uml.backend) from UserMessageLog uml where (uml.messageStatus = eu.domibus.common.MessageStatus.ACKNOWLEDGED or uml.messageStatus = eu.domibus.common.MessageStatus.SEND_FAILURE) and uml.deleted is null and uml.mpc = :MPC and uml.modificationTime is not null and uml.modificationTime < :DATE"),
        @NamedQuery(name = "UserMessageLog.findSentUserMessagesOlderThan", query = "select new eu.domibus.core.message.UserMessageLogDto(uml.messageInfo.messageId, uml.messageSubtype, uml.backend) from UserMessageLog uml where (uml.messageStatus = eu.domibus.common.MessageStatus.ACKNOWLEDGED or uml.messageStatus = eu.domibus.common.MessageStatus.SEND_FAILURE) and uml.mpc = :MPC and uml.modificationTime is not null and uml.modificationTime < :DATE"),
        @NamedQuery(name = "UserMessageLog.setNotificationStatus", query = "update UserMessageLog userMessageLog set userMessageLog.notificationStatus=:NOTIFICATION_STATUS where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.countEntries", query = "select count(userMessageLog.messageId) from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.setMessageStatusAndNotificationStatus",
                query = "update UserMessageLog userMessageLog set userMessageLog.deleted=:TIMESTAMP, userMessageLog.messageStatus=:MESSAGE_STATUS, userMessageLog.notificationStatus=:NOTIFICATION_STATUS where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findAllInfo", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.deleteMessageLogs", query = "delete from UserMessageLog uml where uml.messageId in :MESSAGEIDS"),
        @NamedQuery(name = "UserMessageLog.findSendEnqueuedMessages",
                query = "select distinct m.messageInfo.messageId from UserMessage m " +
                        "left join m.messageProperties.property p, UserMessageLog ml " +
                        "where ml.messageId = m.messageInfo.messageId  and ml.messageStatus =  eu.domibus.common.MessageStatus.SEND_ENQUEUED " +
                        "and ml.messageType = eu.domibus.ebms3.common.model.MessageType.USER_MESSAGE and ml.deleted is null " +
                        "and (:FINAL_RECIPIENT is null or (p.name = 'finalRecipient' and p.value = :FINAL_RECIPIENT)) " +
                        "and (:START_DATE is null or ml.received >= :START_DATE) " +
                        "and (:END_DATE is null or ml.received <= :END_DATE)"),

})
public class UserMessageLog extends MessageLog {

    @ManyToOne
    @JoinColumn(name = "MESSAGE_ID", referencedColumnName = "MESSAGE_ID", updatable = false, insertable = false)
    protected MessageInfo messageInfo;

    @Column(name = "SOURCE_MESSAGE")
    protected Boolean sourceMessage;

    @Column(name = "MESSAGE_FRAGMENT")
    protected Boolean messageFragment;

    @Column(name = "SCHEDULED")
    protected Boolean scheduled;

    @Version
    @Column(name = "VERSION")
    protected int version;

    public MessageInfo getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(MessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

    public UserMessageLog() {
        setMessageType(MessageType.USER_MESSAGE);
        setReceived(new Date());
        setSendAttempts(0);
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
}
