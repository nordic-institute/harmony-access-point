package eu.domibus.ext.domain.archive;

import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class MessagesDTO {

    Integer limit;
    Integer start;
    Integer total;
    List<String> messages;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
