package eu.domibus.ext.domain.archive;

import java.util.Date;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class NotArchivedMessagesFilterDTO {
    protected Date messageStartDate;
    protected Date messageEndDate;

    public NotArchivedMessagesFilterDTO() {
    }

    public NotArchivedMessagesFilterDTO(Date messageStartDate, Date messageEndDate) {
        this.messageStartDate = messageStartDate;
        this.messageEndDate = messageEndDate;
    }

    public Date getMessageStartDate() {
        return messageStartDate;
    }

    public void setMessageStartDate(Date messageStartDate) {
        this.messageStartDate = messageStartDate;
    }

    public Date getMessageEndDate() {
        return messageEndDate;
    }

    public void setMessageEndDate(Date messageEndDate) {
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
