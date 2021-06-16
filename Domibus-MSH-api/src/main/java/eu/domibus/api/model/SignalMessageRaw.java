package eu.domibus.api.model;

import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "SignalMessageRaw.findByMessageEntityId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML) FROM SignalMessageRaw l where l.entityId=:ENTITY_ID"),
        @NamedQuery(name = "SignalMessageRaw.findByUserMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML) " +
                "FROM SignalMessageRaw l JOIN l.signalMessage sm " +
                "JOIN sm.userMessage um where um.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageRaw.deleteMessages", query = "delete from SignalMessageRaw mi where mi.entityId in :IDS"),
})
@Entity
@Table(name = "TB_SIGNAL_MESSAGE_RAW")
public class SignalMessageRaw extends AbstractNoGeneratedPkEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PK")
    @MapsId
    protected SignalMessage signalMessage;

    @Lob
    @Column(name = "RAW_XML")
    protected byte[] rawXML;

    public SignalMessage getSignalMessage() {
        return signalMessage;
    }

    public void setSignalMessage(SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }

    public byte[] getRawXML() {
        return rawXML;
    }

    public void setRawXML(byte[] rawXML) {
        this.rawXML = rawXML;
    }


}
