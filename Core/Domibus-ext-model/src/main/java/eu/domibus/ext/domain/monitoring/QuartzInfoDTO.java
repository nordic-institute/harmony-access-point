package eu.domibus.ext.domain.monitoring;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * DTO class that stores Quartz Monitoring Service details
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class QuartzInfoDTO extends ServiceInfoDTO {

    private static final long serialVersionUID = 1L;

    /**
     * Quartz Trigger details {@link QuartzTriggerDetailsDTO}
     */
    protected List<QuartzTriggerDetailsDTO> quartzTriggerInfos;

    /**
     * Get List of Quartz Trigger details
     *
     * @return List of Quartz Trigger details {@link QuartzTriggerDetailsDTO}
     */
    public List<QuartzTriggerDetailsDTO> getQuartzTriggerInfos() {
        return quartzTriggerInfos;
    }

    /**
     * Set List of Quartz Trigger details
     *
     * @param quartzTriggerInfos List of Quartz Trigger details {@link QuartzTriggerDetailsDTO}
     */
    public void setQuartzTriggerInfos(List<QuartzTriggerDetailsDTO> quartzTriggerInfos) {
        this.quartzTriggerInfos = quartzTriggerInfos;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("quartzTriggerInfo", quartzTriggerInfos)
                .append("name", name)
                .append("status", status)
                .toString();
    }
}
