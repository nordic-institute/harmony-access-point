package eu.domibus.ext.services;

import eu.domibus.ext.domain.archive.BatchRequestTypeParameter;
import eu.domibus.ext.domain.archive.QueuedBatchDTO;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusEArchiveExtService {

    void updateStartDateContinuousArchive(Long startDate);

    Long getStartDateContinuousArchive();

    void updateStartDateSanityArchive(Long startDate);

    Long getStartDateSanityArchive();


    Integer getQueuedBatchRequestsCount(BatchRequestTypeParameter requestType,
                                   ZonedDateTime startDate,
                                   ZonedDateTime endDate
    );

    List<QueuedBatchDTO> getQueuedBatchRequests(BatchRequestTypeParameter requestType,
                                                ZonedDateTime startDate,
                                                ZonedDateTime endDate,
                                                Integer pageStart,
                                                Integer pageSize
    );
}
