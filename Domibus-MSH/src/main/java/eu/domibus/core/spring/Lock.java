package eu.domibus.core.spring;

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
@Table(name = "TB_LOCK",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"LOCK_KEY"},
                        name = "UK_LOCK_KEY"
                )
        }
)
@NamedQueries({
        @NamedQuery(name = "Lock.findByLockName",
                query = "select l from Lock l where l.lockKey=:LOCK_KEY",
                lockMode = LockModeType.PESSIMISTIC_WRITE,
                hints = @QueryHint(name = "javax.persistence.lock.timeout", value = "10000")
        ),
})
public class Lock extends AbstractBaseEntity {

    @NotNull
    @Column(name = "LOCK_KEY")
    private String lockKey;

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Lock lock = (Lock) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(lockKey, lock.lockKey)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(lockKey)
                .toHashCode();
    }
}
