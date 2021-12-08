package eu.domibus.api.model;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author idragusa
 * @since 3.2.5
 * <p>
 * Entity class containing the raw xml of the a message.
 */
@Entity
@Table(name = "TB_USER_MESSAGE_RAW")
@NamedQueries({
        @NamedQuery(name = "RawDto.findByMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML,l.compressed) FROM UserMessageRaw l where l.userMessage.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "RawDto.findByEntityId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML,l.compressed) FROM UserMessageRaw l where l.entityId=:ENTITY_ID"),
        @NamedQuery(name = "Raw.deleteByMessageID", query = "DELETE FROM UserMessageRaw r where r.entityId=:MESSAGE_ENTITY_ID"),
        @NamedQuery(name = "RawDto.findByUserMessageId", query = "SELECT new eu.domibus.api.model.RawEnvelopeDto(l.entityId,l.rawXML,l.compressed) " +
                "FROM UserMessageRaw l where l.userMessage.entityId=:USER_MESSAGE_ID"),
        @NamedQuery(name = "UserMessageRaw.deleteMessages", query = "delete from UserMessageRaw r where r.entityId in :IDS"),
})
public class UserMessageRaw extends AbstractNoGeneratedPkEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PK")
    @MapsId
    protected UserMessage userMessage;

    @Lob
    @Column(name = "RAW_XML")
    protected byte[] rawXML;

    @Column(name = "COMPRESSED")
    protected Boolean compressed;

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public void setRawXML(String rawXML) {
        byte[] bytes = rawXML.getBytes(StandardCharsets.UTF_8);
        this.setRawXML(bytes);
    }

    public byte[] getRawXML() {
        if (rawXML == null) {
            return null;
        }

        if (!this.getCompressed()) {
            return this.rawXML;
        }

        try (GZIPInputStream unzipStream = new GZIPInputStream(new ByteArrayInputStream(rawXML))) {
            return IOUtils.toByteArray(unzipStream);
        } catch (IOException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_008, "Decompression failed", e);
        }
    }

    protected void setRawXML(byte[] rawXML) {
        if (rawXML == null) {
            this.rawXML = null;
            return;
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(rawXML.length);
        try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
            zipStream.write(rawXML);
        } catch (IOException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_008, "Compression failed", e);
        }

        this.rawXML = byteStream.toByteArray();
        this.compressed = true;
    }

    public Boolean getCompressed() {
        return BooleanUtils.toBoolean(compressed);
    }

    public void setCompressed(Boolean compressed) {
        this.compressed = compressed;
    }

}
