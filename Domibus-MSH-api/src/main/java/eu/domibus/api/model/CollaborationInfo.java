package eu.domibus.api.model;

import eu.domibus.api.ebms3.Ebms3Constants;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class CollaborationInfo {

    @Column(name = "COLL_INFO_CONVERS_ID", nullable = false)
    @NotNull
    protected String conversationId;

    @Column(name = "COLLABORATION_INFO_ACTION")
    @NotNull
    protected String action = Ebms3Constants.TEST_ACTION;

    //Embedable
    protected AgreementRef agreementRef;

    //Embedable
    protected Service service;

    public AgreementRef getAgreementRef() {
        return this.agreementRef;
    }

    public void setAgreementRef(final AgreementRef value) {
        this.agreementRef = value;
    }

    public Service getService() {
        return this.service;
    }
    public void setService(final Service value) {
        this.service = value;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(final String value) {
        this.action = value;
    }

    public String getConversationId() {
        // this is because Oracle treats empty string as null
        // if we get space, we transform it to an empty string
        return StringUtils.SPACE.equals(this.conversationId) ? StringUtils.EMPTY : this.conversationId;
    }

    public void setConversationId(final String value) {
        // this is because Oracle treats empty strings as null
        // if we receive an empty string, we transform it to space
        this.conversationId = StringUtils.EMPTY.equals(value) ? StringUtils.SPACE : value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CollaborationInfo)) return false;

        final CollaborationInfo that = (CollaborationInfo) o;

        if (!this.action.equalsIgnoreCase(that.action)) return false;
        if (this.agreementRef != null ? !this.agreementRef.equals(that.agreementRef) : that.agreementRef != null)
            return false;
        if (!this.conversationId.equalsIgnoreCase(that.conversationId)) return false;
        return this.service.equals(that.service);

    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, action, agreementRef, service);
    }
}
