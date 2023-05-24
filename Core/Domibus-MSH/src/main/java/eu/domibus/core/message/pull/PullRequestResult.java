package eu.domibus.core.message.pull;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;

import java.util.Date;

public class PullRequestResult {

    private final int sendAttempts;

    private final Date nextAttempts;

    private final MessageStatus messageStatus;

    private final String messageId;

    public PullRequestResult(String messageId, final UserMessageLog userMessageLog) {
        this.sendAttempts = userMessageLog.getSendAttempts();
        this.nextAttempts = userMessageLog.getNextAttempt();
        this.messageStatus = userMessageLog.getMessageStatus();
        this.messageId = messageId;
    }


    public int getSendAttempts() {
        return sendAttempts;
    }

    public Date getNextAttempts() {
        return nextAttempts;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public String getMessageId() {
        return messageId;
    }
}
