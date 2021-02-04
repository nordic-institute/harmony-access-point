package eu.domibus.web.rest.ro;

import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.NotificationStatus;
import eu.domibus.api.validators.CustomWhiteListed;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class MessageLogFilterRequestRO implements Serializable {
    private int page = 0;

    private int pageSize = 10;

    private Boolean asc = true;

    private String orderBy;

    private String messageId;

    private String conversationId;

    private MSHRole mshRole;

    private MessageType messageType = MessageType.USER_MESSAGE;

    private MessageStatus messageStatus;

    private NotificationStatus notificationStatus;

    private String fromPartyId;

    private String toPartyId;

    private String refToMessageId;

    private String originalSender;

    private String finalRecipient;

    private String receivedFrom;

    private String receivedTo;

    private MessageSubtype messageSubtype;

    @CustomWhiteListed(permitted = ":/-.")
    private String action;

    private String serviceType;

    @CustomWhiteListed(permitted = ":/-.")
    private String serviceValue;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getAsc() {
        return asc;
    }

    public void setAsc(Boolean asc) {
        this.asc = asc;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public String getFromPartyId() {
        return fromPartyId;
    }

    public void setFromPartyId(String fromPartyId) {
        this.fromPartyId = fromPartyId;
    }

    public String getToPartyId() {
        return toPartyId;
    }

    public void setToPartyId(String toPartyId) {
        this.toPartyId = toPartyId;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public String getOriginalSender() {
        return originalSender;
    }

    public void setOriginalSender(String originalSender) {
        this.originalSender = originalSender;
    }

    public String getFinalRecipient() {
        return finalRecipient;
    }

    public void setFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
    }

    public MessageSubtype getMessageSubtype() {
        return messageSubtype;
    }

    public void setMessageSubtype(MessageSubtype messageSubtype) {
        this.messageSubtype = messageSubtype;
    }

    public String getReceivedFrom() {
        return receivedFrom;
    }

    public void setReceivedFrom(String receivedFrom) {
        this.receivedFrom = receivedFrom;
    }

    public String getReceivedTo() {
        return receivedTo;
    }

    public void setReceivedTo(String receivedTo) {
        this.receivedTo = receivedTo;
    }

    public String getAction() {
        return action;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getServiceValue() {
        return serviceValue;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setServiceValue(String serviceValue) {
        this.serviceValue = serviceValue;
    }
}
