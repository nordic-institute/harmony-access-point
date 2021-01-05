package eu.domibus.core.message;

import eu.domibus.api.message.MessageSubtype;

/**
 * @author idragusa
 * @since 4.2
 */
public class UserMessageLogDto {

    protected String messageId;

    protected MessageSubtype messageSubtype;

    protected String backend;

    public UserMessageLogDto(String messageId, MessageSubtype messageSubtype, String backend) {
        this.messageId = messageId;
        this.messageSubtype = messageSubtype;
        this.backend = backend;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageid(String messageId) {
        this.messageId = messageId;
    }

    public MessageSubtype getMessageSubtype() {
        return messageSubtype;
    }

    public void setMessageSubtype(MessageSubtype messageSubtype) {
        this.messageSubtype = messageSubtype;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public boolean isTestMessage() {
        if(MessageSubtype.TEST == messageSubtype) {
            return true;
        }
        return false;
    }
}
