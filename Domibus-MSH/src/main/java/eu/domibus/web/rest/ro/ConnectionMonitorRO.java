package eu.domibus.web.rest.ro;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class ConnectionMonitorRO {

    boolean testable;

    boolean monitored;

    public enum ConnectionStatus {
        UNKNOWN,
        OK,
        BROKEN,
        PENDING,
    }

    ConnectionStatus status = ConnectionStatus.UNKNOWN;

    TestServiceMessageInfoRO lastSent;

    TestServiceMessageInfoRO lastReceived;

    public boolean isMonitored() {
        return monitored;
    }

    public void setMonitored(boolean monitored) {
        this.monitored = monitored;
    }

    public boolean isTestable() {
        return testable;
    }

    public void setTestable(boolean testable) {
        this.testable = testable;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    public TestServiceMessageInfoRO getLastSent() {
        return lastSent;
    }

    public void setLastSent(TestServiceMessageInfoRO lastSent) {
        this.lastSent = lastSent;
    }

    public TestServiceMessageInfoRO getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(TestServiceMessageInfoRO lastReceived) {
        this.lastReceived = lastReceived;
    }
}
