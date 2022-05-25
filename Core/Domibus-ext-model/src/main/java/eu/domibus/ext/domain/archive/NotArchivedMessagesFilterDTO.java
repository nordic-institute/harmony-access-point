package eu.domibus.ext.domain.archive;

import java.util.Date;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class NotArchivedMessagesFilterDTO {
    protected Long messageStartDate;
    protected Long messageEndDate;

    public NotArchivedMessagesFilterDTO() {
    }

    public NotArchivedMessagesFilterDTO(Long messageStartDate, Long messageEndDate) {
        this.messageStartDate = messageStartDate;
        this.messageEndDate = messageEndDate;
    }

    public Long getMessageStartDate() {
        return messageStartDate;
    }

    public void setMessageStartDate(Long messageStartDate) {
        this.messageStartDate = messageStartDate;
    }

    public Long getMessageEndDate() {
        return messageEndDate;
    }

    public void setMessageEndDate(Long messageEndDate) {
        this.messageEndDate = messageEndDate;
    }

    @Override
    public String toString() {
        return "NotArchivedMessagesFilterDTO{" +
                "messageStartDate=" + messageStartDate +
                ", messageEndDate=" + messageEndDate +
                '}';
    }
}
