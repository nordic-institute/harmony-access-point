package eu.domibus.core.message.retention;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;


/**
 * This class is responsible for the state of a deletion job
 *
 * @author idragusa
 * @since 4.2.1
 */
@Entity
@Table(name = "TB_DELETION_JOB")
@NamedQueries({
        @NamedQuery(name = "DeletionJobDao.findCurrentDeletionJobs",
                query = "select deletionJob from DeletionJob deletionJob"),
        })

public class DeletionJob extends AbstractBaseEntity {

    @Column(name = "MPC")
    @NotNull
    private String mpc;
    @Column(name = "RETENTION")
    @NotNull
    private int retention;
    @Column(name = "LIMIT")
    @NotNull
    private int limit;
    @Column(name = "PROCEDURE_NAME")
    @NotNull
    public String procedureName;
    @Column(name = "STATE")
    @NotNull
    private String state;
    @Column(name = "ACTUAL_START_DATE")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualStartDate;

    public DeletionJob(String mpc, int retention, int limit, String procedureName) {
        this.mpc = mpc;
        this.retention = retention;
        this.limit = limit;
        this.procedureName = procedureName;
        this.state = DeletionJobState.NEW.name();
        this.actualStartDate = new Date(System.currentTimeMillis());
    }

    public String getMpc() {
        return mpc;
    }

    public void setMpc(String mpc) {
        this.mpc = mpc;
    }

    public long getRetention() {
        return retention;
    }

    public void setRetention(int retention) {
        this.retention = retention;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletionJob that = (DeletionJob) o;
        return retention == that.retention &&
                limit == that.limit &&
                Objects.equals(mpc, that.mpc) &&
                Objects.equals(procedureName, that.procedureName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mpc, retention, limit, procedureName);
    }

    public boolean isActive() {
        if(DeletionJobState.RUNNING == DeletionJobState.valueOf(state)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "DeletionJob{" +
                "mpc='" + mpc + '\'' +
                ", retention=" + retention +
                ", limit=" + limit +
                ", procedureName='" + procedureName + '\'' +
                ", state='" + state + '\'' +
                ", actualStartDate=" + actualStartDate +
                '}';
    }
}
