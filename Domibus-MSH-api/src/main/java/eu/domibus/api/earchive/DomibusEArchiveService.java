package eu.domibus.api.earchive;

import eu.domibus.api.model.ListUserMessageDto;
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

    Long getBatchRequestListCount(EArchiveBatchFilter filter);

    List<EArchiveBatchRequestDTO> getBatchRequestList(EArchiveBatchFilter filter);

    ListUserMessageDto getBatchUserMessageList(String batchId);

    ListUserMessageDto getNotArchivedMessages(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize);

    EArchiveBatchRequestDTO reExportBatch(String batchId);

    EArchiveBatchRequestDTO setBatchClientStatus(String batchId, EArchiveBatchStatus batchStatus);
}
