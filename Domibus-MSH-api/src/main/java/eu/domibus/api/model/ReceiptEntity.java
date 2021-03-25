package eu.domibus.api.model;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_RECEIPT")
@NamedQueries({
        @NamedQuery(name = "Receipt.deleteReceipts", query = "delete from  ReceiptEntity where entityId in :RECEIPTIDS"),
        @NamedQuery(name = "Receipt.findBySignalRefToMessageId", query = "select re from ReceiptEntity re join fetch re.signalMessage where re.signalMessage.refToMessageId=:REF_TO_MESSAGE_ID"),
})
public class ReceiptEntity extends AbstractNoGeneratedPkEntity {
    @SuppressWarnings("JpaAttributeTypeInspection")
    @Lob
    @Column(name = "RAW_XML")
    protected String rawXml; //NOSONAR

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private SignalMessage signalMessage;

    public String getRawXml() {
        return rawXml;
    }

    public void setRawXml(String rawXml) {
        this.rawXml = rawXml;
    }

    public SignalMessage getSignalMessage() {
        return signalMessage;
    }

    public void setSignalMessage(SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }
}
