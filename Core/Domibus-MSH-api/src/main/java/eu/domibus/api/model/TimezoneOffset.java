package eu.domibus.api.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

@Entity
@Table(name = "TB_D_TIMEZONE_OFFSET")
@NamedQueries({
        @NamedQuery(name = "TimezoneOffset.findByTimezoneIdAndOffsetSeconds",
                hints = {
                        @QueryHint(name = "org.hibernate.cacheRegion", value = "dictionary-queries"),
                        @QueryHint(name = "org.hibernate.cacheable", value = "true")},
                query = "select t from TimezoneOffset t where t.nextAttemptTimezoneId = :TIMEZONE_ID and t.nextAttemptOffsetSeconds = :OFFSET_SECONDS")
})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TimezoneOffset extends AbstractBaseEntity {

    /**
     * The timezone ID (e.g. "Europe/Brussels") on the application server when the next attempt date was saved.
     * <p>
     * This value is only intended to be read in the front-end, when converting the next attempt date - in UTC -
     * for displaying it to user as a local date time in this timezone.
     */
    @XmlTransient
    @Column(name = "NEXT_ATTEMPT_TIMEZONE_ID")
    private String nextAttemptTimezoneId;

    /**
     * The timezone offset in seconds (e.g. -3600, 0, 3600, 7200) on the application server when the next attempt date
     * was saved. A timezone can have multiple offsets, depending on whether it adjusts the hour during DST or not.
     * <p>
     * Together with the next attempt timezone ID value, it helps differentiate between corner case scenarios, when
     * multiple dates can have the same value in local date time, during DST adjustments in autumn:
     * 31-10-2021 02:00 +02:00 (offset=7200, CEST, before DST) vs 31-10-2021 02:00 +01:00 (offset=3600, CET, after DST).
     * <p>
     * This value is only intended to be read in the front-end, when converting the next attempt date - in UTC -
     * for displaying it to user as a local date time using the correct offset.
     */
    @XmlTransient
    @Column(name = "NEXT_ATTEMPT_OFFSET_SECONDS")
    private int nextAttemptOffsetSeconds;

    public String getNextAttemptTimezoneId() {
        return nextAttemptTimezoneId;
    }

    public void setNextAttemptTimezoneId(String nextAttemptTimezoneId) {
        this.nextAttemptTimezoneId = nextAttemptTimezoneId;
    }

    public int getNextAttemptOffsetSeconds() {
        return nextAttemptOffsetSeconds;
    }

    public void setNextAttemptOffsetSeconds(int nextAttemptOffsetSeconds) {
        this.nextAttemptOffsetSeconds = nextAttemptOffsetSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimezoneOffset)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TimezoneOffset that = (TimezoneOffset) o;
        return nextAttemptOffsetSeconds == that.nextAttemptOffsetSeconds
                && nextAttemptTimezoneId.equals(that.nextAttemptTimezoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nextAttemptTimezoneId, nextAttemptOffsetSeconds);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nextAttemptTimezoneId", nextAttemptTimezoneId)
                .append("nextAttemptOffsetSeconds", nextAttemptOffsetSeconds)
                .appendSuper(super.toString())
                .toString();
    }
}
