package eu.domibus.ext.domain.archive;

import java.time.OffsetDateTime;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class CurrentBatchStartDateDTO {

    OffsetDateTime startDate;

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }
}
