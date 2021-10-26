package eu.domibus.ext.domain.archive;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class NotArchivedMessagesFilterDTO {
    Date messageStartDate;
    Date messageEndDate;

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
}
