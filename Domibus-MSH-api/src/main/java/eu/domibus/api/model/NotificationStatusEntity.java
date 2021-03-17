package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_NOTIFICATION_STATUS")
public class NotificationStatusEntity extends AbstractBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", unique = true)
    protected NotificationStatus status;

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NotificationStatusEntity that = (NotificationStatusEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(status, that.status)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(status)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("status", status)
                .toString();
    }
}
