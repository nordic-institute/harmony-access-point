
package eu.domibus.core.message.nonrepudiation;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.persistence.*;

/**
 * @author idragusa
 * @since 3.2.5
 *
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
    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "USERMESSAGE_ID_FK")
    protected UserMessage userMessage;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "SIGNALMESSAGE_ID_FK")
    protected SignalMessage signalMessage;

    @Lob
    @Column(name = "RAW_XML")
    protected String rawXML;

    @Column(name = "MESSAGE_ID")
    protected String messageId;


    public RawEnvelopeLog() {
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public SignalMessage getSignalMessage() {
        return signalMessage;
    }

    public void setSignalMessage(SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }

    public String getRawXML() {
        return rawXML;
    }

    public void setRawXML(String rawXML) {
        this.rawXML = rawXML;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
