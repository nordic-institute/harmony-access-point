package eu.domibus.ext.domain.archive;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class NotArchivedMessagesFilterDTO {
    ZonedDateTime messageStartDate;
    ZonedDateTime messageEndDate;

    public NotArchivedMessagesFilterDTO(ZonedDateTime messageStartDate, ZonedDateTime messageEndDate) {
        this.messageStartDate = messageStartDate;
        this.messageEndDate = messageEndDate;
    }

    public ZonedDateTime getMessageStartDate() {
        return messageStartDate;
    }

    public void setMessageStartDate(ZonedDateTime messageStartDate) {
        this.messageStartDate = messageStartDate;
    }

    public ZonedDateTime getMessageEndDate() {
        return messageEndDate;
    }

    public void setMessageEndDate(ZonedDateTime messageEndDate) {
        this.messageEndDate = messageEndDate;
    }
}
