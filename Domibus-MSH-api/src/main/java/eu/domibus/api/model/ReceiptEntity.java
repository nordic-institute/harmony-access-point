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
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_RECEIPT")
@NamedQueries({
        @NamedQuery(name = "Receipt.deleteReceipts", query = "delete from  ReceiptEntity where entityId in :RECEIPTIDS"),
        @NamedQuery(name = "Receipt.deleteMessages", query = "delete from ReceiptEntity receipt where receipt.entityId in :IDS"),
        @NamedQuery(name = "Receipt.findBySignalRefToMessageId", query = "select re from ReceiptEntity re join fetch re.signalMessage where re.signalMessage.refToMessageId=:REF_TO_MESSAGE_ID"),
})
public class ReceiptEntity extends AbstractNoGeneratedPkEntity {
    @SuppressWarnings("JpaAttributeTypeInspection")
    @Lob
    @Column(name = "RAW_XML")
    protected byte[] rawXml; //NOSONAR

    @Column(name = "COMPRESSED")
    protected Boolean compressed;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PK", nullable = false)
    @MapsId
    private SignalMessage signalMessage;

    public SignalMessage getSignalMessage() {
        return signalMessage;
    }

    public void setSignalMessage(SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }

    public void setRawXml(String rawXml) {
        byte[] bytes = rawXml.getBytes(StandardCharsets.UTF_8);
        this.setRawXml(bytes);
    }

    public byte[] getRawXml() {
        if (rawXml == null) {
            return null;
        }

        if (!this.getCompressed()) {
            return this.rawXml;
        }

        try (GZIPInputStream unzipStream = new GZIPInputStream(new ByteArrayInputStream(rawXml))) {
            return IOUtils.toByteArray(unzipStream);
        } catch (IOException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_008, "Decompression failed", e);
        }
    }

    protected void setRawXml(byte[] rawXml) {
        if (rawXml == null) {
            this.rawXml = null;
            return;
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(rawXml.length);
        try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
            zipStream.write(rawXml);
        } catch (IOException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_008, "Compression failed", e);
        }

        this.rawXml = byteStream.toByteArray();
        this.compressed = true;
    }

    public Boolean getCompressed() {
        return BooleanUtils.isTrue(compressed);
    }

    public void setCompressed(Boolean compressed) {
        this.compressed = compressed;
    }
}
