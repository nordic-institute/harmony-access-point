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
                query = "select t from TruststoreEntity t where t.name=:NAME"),
        @NamedQuery(name = "Truststore.countByName",
                query = "select count(t) from TruststoreEntity t where t.name=:NAME"),
})
public class TruststoreEntity extends AbstractBaseEntity {

    @NotNull
    @Column(name = "NAME")
    private String name;

    @NotNull
    @Column(name = "TYPE")
    private String type;

    @NotNull
    @Column(name = "PASSWORD")
    private String password;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

        TruststoreEntity truststore = (TruststoreEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(name, truststore.name)
                .append(type, truststore.type)
                .append(password, truststore.password)
                .append(content, truststore.content)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(name)
                .append(type)
                .append(password)
                .append(content)
                .toHashCode();
    }
}
