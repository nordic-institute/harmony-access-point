package eu.domibus.core.message.retention;

import java.util.concurrent.Future;

/**
 * This service class is responsible for the retention and clean up of Domibus messages, including signal messages.
 *
 * @author idragusa
 * @since 4.2.1
 */
public class DeleteUserMessagesDetails {

    public Future<?> deleteExpiredFuture;
    public String queryName;
    public String mpc;
    public long startTime;

    public DeleteUserMessagesDetails(Future<?> deleteExpiredFuture, String queryName, String mpc, long startTime) {
        this.deleteExpiredFuture = deleteExpiredFuture;
        this.queryName = queryName;
        this.mpc = mpc;
        this.startTime = startTime;
    }

    public Future<?> getDeleteExpiredFuture() {
        return deleteExpiredFuture;
    }

    public void setDeleteExpiredFuture(Future<?> deleteExpiredFuture) {
        this.deleteExpiredFuture = deleteExpiredFuture;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getMpc() {
        return mpc;
    }

    public void setMpc(String mpc) {
        this.mpc = mpc;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
