package eu.domibus.api.model;

import java.util.Objects;

public class Partition {
    protected String partitionName;
    protected Long highValue;

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Long getHighValue() {
        return highValue;
    }

    public void setHighValue(Long highValue) {
        this.highValue = highValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Partition partition = (Partition) o;
        return partitionName.equals(partition.partitionName) && highValue.equals(partition.highValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionName, highValue);
    }
}

