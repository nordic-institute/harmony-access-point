package eu.domibus.ext.domain.monitoring;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * DTO class that stores DataBase Monitoring Info
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class DataBaseInfoDTO extends ServiceInfoDTO {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("status", status)
                .toString();
    }
}
