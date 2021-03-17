package eu.domibus.api.model;

import eu.domibus.api.message.MessageSubtype;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_MESSAGE_SUBTYPE")
public class MessageSubtypeEntity extends AbstractBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "SUBTYPE", unique = true)
    protected MessageSubtype messageSubtype;

    public MessageSubtype getMessageSubtype() {
        return messageSubtype;
    }

    public void setMessageSubtype(MessageSubtype messageSubtype) {
        this.messageSubtype = messageSubtype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageSubtypeEntity that = (MessageSubtypeEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageSubtype, that.messageSubtype)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageSubtype)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageSubtype", messageSubtype)
                .toString();
    }
}
