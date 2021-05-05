package eu.domibus.api.model;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;

/**
 * @author idragusa
 * @since 3.2.5
 * <p>
 * Entity class containing the raw xml of the a message.
 */
@Entity
@Table(name = "TB_USER_MESSAGE_RAW")
@NamedQueries({
        @NamedQuery(name = "RawDto.findByMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML) FROM UserMessageRaw l where l.userMessage.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "Raw.deleteByMessageID", query = "DELETE FROM UserMessageRaw r where r.entityId=:MESSAGE_ENTITY_ID"),
        @NamedQuery(name = "RawDto.findByUserMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML) " +
                "FROM UserMessageRaw l where l.userMessage.entityId=:USER_MESSAGE_ID"),
})
public class UserMessageRaw extends AbstractNoGeneratedPkEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PK")
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

    public void setRawXML(String rawXML) {
        byte[] bytes = rawXML.getBytes(StandardCharsets.UTF_8);
        this.rawXML = bytes;
    }

    public byte[] getRawXML() {
        return rawXML;
    }

    public void setRawXML(byte[] rawXML) {
        this.rawXML = rawXML;
    }


}
