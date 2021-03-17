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
        @NamedQuery(name = "RawDto.findByMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML) FROM UserMessageRaw l where l.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "Raw.deleteByMessageID", query = "DELETE FROM UserMessageRaw r where r.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "RawDto.findByUserMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML) " +
                "FROM UserMessageRaw l where l.userMessage.entityId=:USER_MESSAGE_ID"),
})
public class UserMessageRaw extends AbstractNoGeneratedPkEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    protected UserMessage userMessage;

    @Lob
    @Column(name = "RAW_XML")
    protected byte[] rawXML;

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public byte[] getRawXML() {
        return rawXML;
    }

    public void setRawXML(byte[] rawXML) {
        this.rawXML = rawXML;
    }


}
