package eu.domibus.api.diagnostics;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
public class AlertsCounts {
    private long totalCount;
    private Map<String, Long> countsByStatus;
    private Map<String, Long> countsByType;

    public AlertsCounts() {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Alerts Counts Information:\n");

        // Total count
        sb.append("Total Alerts: ").append(totalCount).append("\n");

        // Counts by status
        for (Map.Entry<String, Long> entry : countsByStatus.entrySet()) {
            sb.append("Status ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        // Counts by type
        for (Map.Entry<String, Long> entry : countsByType.entrySet()) {
            sb.append("Type ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }
}