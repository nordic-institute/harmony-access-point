
package eu.domibus.core.message.nonrepudiation;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;

/**
 * @author idragusa
 * @since 3.2.5
 * <p>
 * Entity class containing the raw xml of the a message.
 */
@Entity
@Table(name = "TB_RAWENVELOPE_LOG")
@NamedQueries({
        @NamedQuery(name = "RawDto.findByMessageId", query = "SELECT new eu.domibus.core.message.nonrepudiation.RawEnvelopeDto(l.entityId,l.rawXML) FROM RawEnvelopeLog l where l.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "Raw.findByMessageId", query = "SELECT l FROM RawEnvelopeLog l where l.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "Raw.deleteByMessageID",
                query = "DELETE FROM RawEnvelopeLog r where r.messageId=:MESSAGE_ID")
})
public class RawEnvelopeLog extends AbstractBaseEntity {
    @Lob
    @Column(name = "RAW_XML")
    protected byte[] rawXML;

    @Column(name = "MESSAGE_ID")
    protected String messageId;

    @Column(name = "USERMESSAGE_ID_FK")
    protected Long userMessageId;

    @Column(name = "SIGNALMESSAGE_ID_FK")
    protected Long signalMessageId;

    public RawEnvelopeLog() {
    }

    public void setRawXML(String rawXML) {
        this.rawXML = rawXML.getBytes(StandardCharsets.UTF_8);
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public Long getUserMessageId() {
        return userMessageId;
    }

    public void setUserMessageId(Long userMessageId) {
        this.userMessageId = userMessageId;
    }

    public Long getSignalMessageId() {
        return signalMessageId;
    }

    public void setSignalMessageId(Long signalMessageId) {
        this.signalMessageId = signalMessageId;
    }
}
