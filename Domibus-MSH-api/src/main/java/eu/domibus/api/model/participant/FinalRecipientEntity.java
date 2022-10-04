package eu.domibus.api.model.participant;

import eu.domibus.api.model.AbstractBaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "TB_FINAL_RECIPIENT_URL")
@NamedQueries({
        @NamedQuery(name = "FinalRecipientEntity.findByFinalRecipient",
                query = "select rec from FinalRecipientEntity rec where rec.finalRecipient =: FINAL_RECIPIENT")
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
}