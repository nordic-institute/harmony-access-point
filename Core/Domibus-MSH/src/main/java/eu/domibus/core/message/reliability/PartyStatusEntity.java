package eu.domibus.core.message.reliability;

import eu.domibus.api.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Razvan Cretu
 * @since 5.1
 */
@Entity
@Table(name = "TB_PARTY_STATUS",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"PARTY_NAME"},
                        name = "UK_NAME"
                )
        }
)
@NamedQueries({
        @NamedQuery(name = "PartyStatus.findByName",
                query = "select p from PartyStatusEntity p where p.partyName=:PARTY_NAME"),
        @NamedQuery(name = "PartyStatus.countByName",
                query = "select count(p) from PartyStatusEntity p where p.partyName=:PARTY_NAME")
})
public class PartyStatusEntity extends AbstractBaseEntity {

    @NotNull
    @Column(name = "PARTY_NAME")
    private String partyName;

    @NotNull
    @Column(name = "CONNECTIVITY_STATUS")
    private String connectivityStatus;

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getConnectivityStatus() {
        return connectivityStatus;
    }

    public void setConnectivityStatus(String status) {
        this.connectivityStatus = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartyStatusEntity partyStatus = (PartyStatusEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(partyName, partyStatus.partyName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(partyName)
                .toHashCode();
    }
}
