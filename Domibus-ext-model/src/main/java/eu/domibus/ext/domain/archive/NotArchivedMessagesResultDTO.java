package eu.domibus.ext.domain.archive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class NotArchivedMessagesResultDTO {

    protected NotArchivedMessagesFilterDTO filter;
    protected PaginationDTO pagination;
    protected List<String> messages;

    public NotArchivedMessagesResultDTO(NotArchivedMessagesFilterDTO filter, Integer pageStart, Integer pageSize) {
        this.filter = filter;
        this.pagination = new PaginationDTO(pageStart, pageSize);
    }

    public PaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }

    public List<String> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
