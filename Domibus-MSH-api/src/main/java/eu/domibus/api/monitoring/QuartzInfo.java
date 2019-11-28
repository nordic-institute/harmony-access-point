package eu.domibus.api.monitoring;

import java.util.List;
import java.util.Objects;

public class QuartzInfo  extends ServiceInfo{

    protected List<QuartzInfoDetails> quartzInfoDetails;

    public List<QuartzInfoDetails> getQuartzInfoDetails() {
        return quartzInfoDetails;
    }

    public void setQuartzInfoDetails(List<QuartzInfoDetails> quartzInfoDetails) {
        this.quartzInfoDetails = quartzInfoDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuartzInfo that = (QuartzInfo) o;
        return Objects.equals(quartzInfoDetails, that.quartzInfoDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quartzInfoDetails);
    }

    @Override
    public String toString() {
        return "QuartzInfo{" +
                "quartzInfoDetails=" + quartzInfoDetails +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
