package eu.domibus.plugin.ws.message;

import javax.persistence.*;
import java.util.Date;

/**
 * @author idragusa
 * @since 4.2
 */
@Entity
@Table(name = "WS_PLUGIN_TB_MESSAGE_LOG")
@NamedQuery(name = "WSMessageLogEntity.findByMessageId",
        query = "select wsMessageLogEntity from WSMessageLogEntity wsMessageLogEntity where wsMessageLogEntity.messageId=:MESSAGE_ID")
@NamedQuery(name = "WSMessageLogEntity.findAll",
        query = "select wsMessageLogEntity from WSMessageLogEntity wsMessageLogEntity order by wsMessageLogEntity.received asc")
@NamedQuery(name = "WSMessageLogEntity.findAllByFinalRecipient",
        query = "select wsMessageLogEntity from WSMessageLogEntity wsMessageLogEntity where wsMessageLogEntity.finalRecipient=:FINAL_RECIPIENT order by wsMessageLogEntity.received asc")
@NamedQuery(name = "WSMessageLogEntity.deleteByMessageId",
        query = "DELETE FROM WSMessageLogEntity wsMessageLogEntity where wsMessageLogEntity.messageId=:MESSAGE_ID")
@NamedQuery(name = "WSMessageLogEntity.deleteByMessageIds",
        query = "DELETE FROM WSMessageLogEntity wsMessageLogEntity where wsMessageLogEntity.messageId in :MESSAGE_IDS")
public class WSMessageLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PK")
    private long entityId;

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "CONVERSATION_ID")
    private String conversationId;

    @Column(name = "REF_TO_MESSAGE_ID")
    private String refToMessageId;

    @Column(name = "FROM_PARTY_ID")
    private String fromPartyId;

    @Column(name = "FINAL_RECIPIENT")
    private String finalRecipient;

    @Column(name = "ORIGINAL_SENDER")
    private String originalSender;

    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    public WSMessageLogEntity() {
    }

    public WSMessageLogEntity(String messageId, String conversationId, String refToMessageId, String fromPartyId,
                              String finalRecipient, String originalSender, Date received) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.refToMessageId = refToMessageId;
        this.fromPartyId = fromPartyId;
        this.finalRecipient = finalRecipient;
        this.originalSender = originalSender;
        this.received = received;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFromPartyId() {
        return fromPartyId;
    }

    public void setFromPartyId(String fromPartyId) {
        this.fromPartyId = fromPartyId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public String getFinalRecipient() {
        return finalRecipient;
    }

    public void setFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
    }

    public String getOriginalSender() {
        return originalSender;
    }

    public void setOriginalSender(String originalSender) {
        this.originalSender = originalSender;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
