package eu.domibus.core.message;

import eu.domibus.api.message.MessageSubtype;

/**
 * @author idragusa
 * @since 4.2
 */
public class MessageDto {

    protected String userMessageId;
    protected String signalMessageId;
    protected Long umlEntityId;
    protected Long smiEntityId;
    protected Long umiEntityId;
    protected Long receiptEntityId;
    protected MessageSubtype messageSubtype;
    protected String backend;

    public MessageDto(String userMessageId, String signalMessageId, Long receiptId, MessageSubtype messageSubtype, String backend) {
        this.userMessageId = userMessageId;
        this.signalMessageId = signalMessageId;

        this.receiptEntityId = receiptId;
        this.messageSubtype = messageSubtype;
        this.backend = backend;
    }

    public MessageDto(String userMessageId, String signalMessageId, Long umlEntityId, Long smiEntityId, Long umiEntityId, Long receiptEntityId, MessageSubtype messageSubtype, String backend) {
        this.userMessageId = userMessageId;
        this.signalMessageId = signalMessageId;
        this.umlEntityId = umlEntityId;
        this.smiEntityId = smiEntityId;
        this.umiEntityId = umiEntityId;
        this.receiptEntityId = receiptEntityId;
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

    public Long getUmlEntityId() {
        return umlEntityId;
    }

    public void setUmlEntityId(Long umlEntityId) {
        this.umlEntityId = umlEntityId;
    }

    public Long getSmiEntityId() {
        return smiEntityId;
    }

    public void setSmiEntityId(Long smiEntityId) {
        this.smiEntityId = smiEntityId;
    }

    public Long getUmiEntityId() {
        return umiEntityId;
    }

    public void setUmiEntityId(Long umiEntityId) {
        this.umiEntityId = umiEntityId;
    }

    public Long getReceiptEntityId() {
        return receiptEntityId;
    }

    public void setReceiptEntityId(Long receiptEntityId) {
        this.receiptEntityId = receiptEntityId;
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
