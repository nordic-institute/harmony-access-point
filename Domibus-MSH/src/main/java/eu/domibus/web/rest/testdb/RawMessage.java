package eu.domibus.web.rest.testdb;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "TB_RAWMESSAGE")
public class RawMessage extends AbstractBaseEntity {
    @Column(name = "RAWENVELOPE")
    protected String rawEnvelope;

    @Lob
    @Column(name = "RAWPAYLOAD")
    protected byte[] rawPayload;

    public RawMessage(String rawEnvelope) {
        this.rawEnvelope = rawEnvelope;
    }

    public String getRawEnvelope() {
        return rawEnvelope;
    }

    public void setRawEnvelope(String rawEnvelope) {
        this.rawEnvelope = rawEnvelope;
    }

    public byte[] getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(byte[] rawPayload) {
        this.rawPayload = rawPayload;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RawMessage rm = (RawMessage) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(rawEnvelope, rm.rawEnvelope)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(rawEnvelope)
                .toHashCode();
    }
}
