package eu.domibus.api.monitoring;

import java.util.Objects;

public class DataBaseInfo  extends ServiceInfo{

    @Override
    public String toString() {
        return "DataBaseInfo{" +
                "name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataBaseInfo that = (DataBaseInfo) o;
        return Objects.equals(name, that.name) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, status);
    }
}
