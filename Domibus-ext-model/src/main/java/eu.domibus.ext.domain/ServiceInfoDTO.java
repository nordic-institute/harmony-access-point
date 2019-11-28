package eu.domibus.ext.domain;

import java.io.Serializable;
import java.util.Objects;

public abstract class ServiceInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String  name;
    protected MonitoringStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MonitoringStatus getStatus() {
        return status;
    }

    public void setStatus(MonitoringStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInfoDTO that = (ServiceInfoDTO) o;
        return Objects.equals(name, that.name) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, status);
    }

    @Override
    public String toString() {
        return "ServiceInfoDTO{" +
                "name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
