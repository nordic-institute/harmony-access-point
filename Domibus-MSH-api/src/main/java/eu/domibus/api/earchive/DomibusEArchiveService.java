package eu.domibus.api.earchive;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusEArchiveService {

    void updateStartDateContinuousArchive(Long startDate);

    void updateStartDateSanityArchive(Long startDate);

    Long getStartDateContinuousArchive();

    Long getStartDateSanityArchive();

    Long getQueuedBatchRequestsCount(EArchiveBatchFilter filter);

    List<EArchiveBatchDTO> getQueuedBatchRequests(EArchiveBatchFilter filter);


}
