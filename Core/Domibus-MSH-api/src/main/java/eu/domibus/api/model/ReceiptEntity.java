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
        @NamedQuery(name = "Receipt.deleteMessages", query = "delete from ReceiptEntity receipt where receipt.entityId in :IDS"),
        @NamedQuery(name = "Receipt.findBySignalRefToMessageId", query = "select re from ReceiptEntity re join fetch re.signalMessage where re.signalMessage.refToMessageId=:REF_TO_MESSAGE_ID"),
        @NamedQuery(name = "Receipt.findBySignalRefToMessageId2", query = "select re from ReceiptEntity re join fetch re.signalMessage where re.signalMessage.refToMessageId=:REF_TO_MESSAGE_ID and re.signalMessage.mshRole.role=:MSH_ROLE"),
})
public class ReceiptEntity extends RawXmlEntity {
    @SuppressWarnings("JpaAttributeTypeInspection")

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
}
