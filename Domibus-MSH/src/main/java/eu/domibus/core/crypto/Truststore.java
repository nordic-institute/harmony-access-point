package eu.domibus.core.crypto;

import eu.domibus.api.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Entity
@Table(name = "TB_TRUSTSTORE",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"TYPE"},
                        name = "UK_TYPE"
                )
        }
)
@NamedQueries({
        @NamedQuery(name = "Truststore.findByType",
                query = "select t from Truststore t where t.type=:TYPE"),
        @NamedQuery(name = "Truststore.countByType",
                query = "select count(t) from Truststore t where t.type=:TYPE"),
})
public class Truststore extends AbstractBaseEntity {

    @NotNull
    @Column(name = "TYPE")
    private String type;

    @Lob
    @NotNull
    @Column(name = "CONTENT")
    protected byte[] content;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Truststore truststore = (Truststore) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(type, truststore.type)
                .append(content, truststore.content)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(type)
                .append(content)
                .toHashCode();
    }
}
