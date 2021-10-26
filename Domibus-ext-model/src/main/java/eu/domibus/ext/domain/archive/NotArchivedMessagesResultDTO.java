package eu.domibus.ext.domain.archive;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class NotArchivedMessagesResultDTO {

    NotArchivedMessagesFilterDTO filter;
    PaginationDTO pagination;
    List<String> messages;

    public NotArchivedMessagesResultDTO(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize) {
        this.filter = new NotArchivedMessagesFilterDTO(messageStartDate, messageEndDate);
        this.pagination = new PaginationDTO(pageStart, pageSize);
    }

    public PaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
