package eu.domibus.ext.services;

import eu.domibus.ext.domain.archive.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZonedDateTime;
import java.util.Date;
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

    Long getQueuedBatchRequestsCount(QueuedBatchFilterDTO filter);

    List<QueuedBatchDTO> getQueuedBatchRequests(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize);

    Long getExportedBatchRequestsCount(ExportedBatchFilterDTO filter);

    List<ExportedBatchDTO> getExportedBatchRequests(ExportedBatchFilterDTO filter, Integer pageStart, Integer pageSize);

    Long getBatchMessageCount(String batchId);

    List<String> getBatchMessageIds(String batchId, Integer pageStart, Integer pageSize);

    BatchStatusDTO reExportBatch(String batchId);

    BatchStatusDTO setBatchClientStatus(String batchId, BatchArchiveStatusType batchStatus);

    List<String>  getNotArchivedMessages(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize);
}
