package eu.domibus.api.diagnostics;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
public class MessagesByStatusCounts {
    private Map<String, Long> userMessageCounts;

    public MessagesByStatusCounts() {
        this.userMessageCounts = new HashMap<>();
    }

    public Map<String, Long> getUserMessageCounts() {
        return userMessageCounts;
    }

    public void setUserMessageCounts(Map<String, Long> userMessageCounts) {
        this.userMessageCounts = userMessageCounts;
    }

    public void addUserMessageCount(String status, long count) {
        this.userMessageCounts.put(status, count);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Messages by Status Counts Information:\n");

        // User message counts by status
        sb.append("User Messages:\n");
        for (Map.Entry<String, Long> entry : userMessageCounts.entrySet()) {
            sb.append("Status ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }
}