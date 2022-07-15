package eu.domibus.api.model;

import javax.persistence.*;

/**
 * @author idragusa
 * @since 3.2.5
 * <p>
 * Entity class containing the raw xml of the a message.
 */
@Entity
@Table(name = "TB_USER_MESSAGE_RAW")
@NamedQueries({
        @NamedQuery(name = "RawDto.findByMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML,l.compressed, l.userMessage.entityId) FROM UserMessageRaw l where l.userMessage.messageId=:MESSAGE_ID and l.userMessage.mshRole.role=:MSH_ROLE"),
        @NamedQuery(name = "RawDto.findByEntityId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML,l.compressed) FROM UserMessageRaw l where l.entityId=:ENTITY_ID"),
        @NamedQuery(name = "Raw.deleteByMessageID", query = "DELETE FROM UserMessageRaw r where r.entityId=:MESSAGE_ENTITY_ID"),
        @NamedQuery(name = "RawDto.findByUserMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML,l.compressed) " +
                "FROM UserMessageRaw l where l.userMessage.entityId=:USER_MESSAGE_ID"),
        @NamedQuery(name = "UserMessageRaw.deleteMessages", query = "delete from UserMessageRaw r where r.entityId in :IDS"),
})
public class UserMessageRaw extends RawXmlEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PK")
    @MapsId
    protected UserMessage userMessage;

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

}
