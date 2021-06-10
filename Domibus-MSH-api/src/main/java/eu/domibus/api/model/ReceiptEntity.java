package eu.domibus.api.model;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;

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
        this.rawXml = bytes;
    }
    
    public byte[] getRawXml() {
        return rawXml;
    }

    public void setRawXml(byte[] rawXml) {
        this.rawXml = rawXml;
    }
}
