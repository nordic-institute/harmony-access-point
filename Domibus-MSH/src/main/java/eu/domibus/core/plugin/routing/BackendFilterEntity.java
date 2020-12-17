package eu.domibus.core.plugin.routing;

import eu.domibus.core.audit.envers.RevisionLogicalName;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.AuditJoinTable;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Walczac
 */
@Entity
@Table(name = "TB_BACKEND_FILTER")
@NamedQueries({
        @NamedQuery(name = "BackendFilter.findEntriesOrderedByPriority", query = "select bf from BackendFilterEntity bf order by bf.index")
})
@Audited(withModifiedFlag = true)
@RevisionLogicalName("Message filter")
public class BackendFilterEntity extends AbstractBaseEntity {

    @Column(name = "PRIORITY")
    private int index;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "FK_BACKEND_FILTER")
    @OrderColumn(name = "PRIORITY")
    @AuditJoinTable(name = "TB_BACK_RCRITERIA_AUD")
    private List<RoutingCriteriaEntity> routingCriterias = new ArrayList<>();

    @Column(name = "BACKEND_NAME")
    private String backendName;

    public void setIndex(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public List<RoutingCriteriaEntity> getRoutingCriterias() {
        return routingCriterias;
    }

    public void setRoutingCriterias(List<RoutingCriteriaEntity> routingCriterias) {
        this.routingCriterias = routingCriterias;
    }

    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(final String backendName) {
        this.backendName = backendName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BackendFilterEntity that = (BackendFilterEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(index, that.index)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(index)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("index", index)
                .append("routingCriterias", routingCriterias)
                .append("backendName", backendName)
                .toString();
    }
}
