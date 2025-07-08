package eu.domibus.ext.domain.diagnostics;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for messages by status counts information
 *
 * @author Breaz Ionut
 * @since 5.1.9
 */
public class MessagesByStatusCountsDTO {
    private Map<String, Long> userMessageCounts;

    public MessagesByStatusCountsDTO() {
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
}