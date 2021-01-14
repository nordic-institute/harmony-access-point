package eu.domibus.core.message;

import eu.domibus.api.message.MessageSubtype;

/**
 * @author idragusa
 * @since 4.2
 */
public class MessageDto {

    protected String userMessageId;
    protected String signalMessageId;
    protected Long receiptId;
    protected MessageSubtype messageSubtype;
    protected String backend;

    public MessageDto(String userMessageId, String signalMessageId, Long receiptId, MessageSubtype messageSubtype, String backend) {
        this.userMessageId = userMessageId;
        this.signalMessageId = signalMessageId;
        this.receiptId = receiptId;
        this.messageSubtype = messageSubtype;
        this.backend = backend;
    }

    public String getUserMessageId() {
        return userMessageId;
    }

    public void setUserMessageId(String userMessageId) {
        this.userMessageId = userMessageId;
    }

    public String getSignalMessageId() {
        return signalMessageId;
    }

    public void setSignalMessageId(String signalMessageId) {
        this.signalMessageId = signalMessageId;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
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
