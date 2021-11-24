package eu.domibus.api.model;

import org.apache.commons.io.IOUtils;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
        if (rawXML == null) {
            return null;
        }

        try (GZIPInputStream unzipStream = new GZIPInputStream(new ByteArrayInputStream(rawXML))) {
            return IOUtils.toByteArray(unzipStream);
        } catch (IOException e) {
            // TODO
            return rawXML; // not compressed? return the raw data
        }
    }

    public void setRawXML(byte[] rawXML) {
        if (rawXML == null) {
            this.rawXML = null;
            return;
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(rawXML.length);
        try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
            zipStream.write(rawXML);
        } catch (IOException e) {
            // TODO
        }

        this.rawXML = byteStream.toByteArray();
    }


}
