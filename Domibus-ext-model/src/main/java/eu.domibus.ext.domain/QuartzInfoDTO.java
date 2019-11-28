package eu.domibus.ext.domain;

import java.util.List;
import java.util.Objects;
/**
 * @author Soumya Chandran
 * @since 4.2
 */
public class QuartzInfoDTO extends ServiceInfoDTO {

    protected List<QuartzInfoDetailsDTO> quartzTriggerDetails;

    public List<QuartzInfoDetailsDTO> getQuartzTriggerDetails() {
        return quartzTriggerDetails;
    }

    public void setQuartzTriggerDetails(List<QuartzInfoDetailsDTO> quartzTriggerDetails) {
        this.quartzTriggerDetails = quartzTriggerDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        QuartzInfoDTO that = (QuartzInfoDTO) o;
        return Objects.equals(quartzTriggerDetails, that.quartzTriggerDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quartzTriggerDetails);
    }

    @Override
    public String toString() {
        return "QuartzInfoDTO{" +
                "quartzTriggerDetails=" + quartzTriggerDetails +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

}
