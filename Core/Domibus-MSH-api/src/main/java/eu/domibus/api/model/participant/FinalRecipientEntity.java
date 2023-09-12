package eu.domibus.api.model.participant;

import eu.domibus.api.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 */
@Entity
@Table(name = "TB_FINAL_RECIPIENT_URL")
@NamedQueries({
        @NamedQuery(name = "FinalRecipientEntity.findByFinalRecipient",
                query = "select rec from FinalRecipientEntity rec where rec.finalRecipient =: FINAL_RECIPIENT"),
        @NamedQuery(name = "FinalRecipientEntity.findFinalRecipientsModifiedBefore", query = "select rec from FinalRecipientEntity rec where rec.modificationTime < :MODIFICATION_DATE and rec.creationTime < :MODIFICATION_DATE")
})
public class FinalRecipientEntity extends AbstractBaseEntity {

    @Column(name = "FINAL_RECIPIENT")
    protected String finalRecipient;

    @Column(name = "ENDPOINT_URL")
    protected String endpointURL;

    public String getFinalRecipient() {
        return finalRecipient;
    }

    public void setFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("finalRecipient", finalRecipient)
                .append("endpointURL", endpointURL)
                .toString();
    }
}
