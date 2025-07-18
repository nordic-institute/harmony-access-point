package eu.domibus.ext.domain.diagnostics;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for alerts counts information
 *
 * @author Breaz Ionut
 * @since 5.1.9
 */
public class AlertsCountsDTO {
    private long totalCount;
    private Map<String, Long> countsByStatus;
    private Map<String, Long> countsByType;

    public AlertsCountsDTO() {
        this.countsByStatus = new HashMap<>();
        this.countsByType = new HashMap<>();
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public Map<String, Long> getCountsByStatus() {
        return countsByStatus;
    }

    public void setCountsByStatus(Map<String, Long> countsByStatus) {
        this.countsByStatus = countsByStatus;
    }

    public Map<String, Long> getCountsByType() {
        return countsByType;
    }

    public void setCountsByType(Map<String, Long> countsByType) {
        this.countsByType = countsByType;
    }

    public void addStatusCount(String status, long count) {
        this.countsByStatus.put(status, count);
    }

    public void addTypeCount(String type, long count) {
        this.countsByType.put(type, count);
    }
}