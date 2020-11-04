package eu.domibus.api.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_RECEIPT")
@NamedQueries({
        @NamedQuery(name = "Receipt.deleteReceipts", query = "delete from  Receipt where entityId in :RECEIPTIDS"),
})
public class Receipt extends AbstractBaseEntity {
    @SuppressWarnings("JpaAttributeTypeInspection")
    @ElementCollection
    @Lob
    @CollectionTable(name = "TB_RECEIPT_DATA", joinColumns = @JoinColumn(name = "RECEIPT_ID"))
    @Column(name = "RAW_XML")
    protected List<String> any; //NOSONAR

    public List<String> getAny() {
        if (this.any == null) {
            this.any = new ArrayList<>();
        }
        return this.any;
    }

    public void setAny(List<String> any) {
        this.any = any;
    }
}
