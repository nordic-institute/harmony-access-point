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
                        columnNames = {"NAME"},
                        name = "UK_NAME"
                )
        }
)
@NamedQueries({
        @NamedQuery(name = "Truststore.findByName",
                query = "select t from Truststore t where t.name=:NAME"),
        @NamedQuery(name = "Truststore.countByName",
                query = "select count(t) from Truststore t where t.name=:NAME"),
})
public class Truststore extends AbstractBaseEntity {

    @NotNull
    @Column(name = "NAME")
    private String name;

    @Lob
    @NotNull
    @Column(name = "CONTENT")
    protected byte[] content;

    public String getName() {
        return name;
    }

    public void setName(String type) {
        this.name = type;
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
                .append(name, truststore.name)
                .append(content, truststore.content)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(name)
                .append(content)
                .toHashCode();
    }
}
