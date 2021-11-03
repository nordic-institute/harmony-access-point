package eu.domibus.ext.delegate.services.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.ext.delegate.mapper.EArchiveExtMapper;
import eu.domibus.ext.domain.archive.*;
import eu.domibus.ext.services.DomibusEArchiveExtService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX_INCREMENT_NUMBER;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class DomibusEArchiveServiceDelegate implements DomibusEArchiveExtService {

    private final DomibusEArchiveService domibusEArchiveService;
    private final EArchiveExtMapper eArchiveExtMapper;

    public DomibusEArchiveServiceDelegate(DomibusEArchiveService domibusEArchiveService, EArchiveExtMapper eArchiveExtMapper) {
        this.domibusEArchiveService = domibusEArchiveService;
        this.eArchiveExtMapper = eArchiveExtMapper;
    }

    @Override
    public void updateStartDateContinuousArchive(Long startDate) {
        domibusEArchiveService.updateStartDateContinuousArchive(startDate);
    }

    @Override
    public Long getStartDateContinuousArchive() {
        return domibusEArchiveService.getStartDateContinuousArchive();
    }

    @Override
    public void updateStartDateSanityArchive(Long startDate) {
        domibusEArchiveService.updateStartDateSanityArchive(startDate);
    }

    @Override
    public Long getStartDateSanityArchive() {
        return domibusEArchiveService.getStartDateSanityArchive();
    }

    /**
     * Method returns count of Queued batches in database for given search filter
     *
     * @param filter
     * @return
     */
    @Override
    public Long getQueuedBatchRequestsCount(QueuedBatchFilterDTO filter) {
        EArchiveBatchFilter archiveBatchFilter = convertQueuedFilter(filter, null, null);
        return domibusEArchiveService.getBatchRequestListCount(archiveBatchFilter);
    }

    /**
     * Method returns Queued batches in database for given search filter and page
     *
     * @param filter
     * @return
     */
    @Override
    public List<QueuedBatchDTO> getQueuedBatchRequests(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        EArchiveBatchFilter archiveBatchFilter = convertQueuedFilter(filter, pageStart, pageSize);
        List<EArchiveBatchRequestDTO> result = domibusEArchiveService.getBatchRequestList(archiveBatchFilter);
        return result.stream().map(eArchiveBatchDTO -> eArchiveExtMapper.archiveBatchToQueuedBatch(eArchiveBatchDTO)).collect(Collectors.toList());
    }

    /**
     * Method returns count of Exported batches in database for given search filter
     *
     * @param filter
     * @return
     */
    @Override
    public Long getExportedBatchRequestsCount(ExportedBatchFilterDTO filter) {
        EArchiveBatchFilter archiveBatchFilter = convertExportFilter(filter, null, null);
        return domibusEArchiveService.getBatchRequestListCount(archiveBatchFilter);
    }

    /**
     * Method returns Exported batches in database for given search filter and page
     *
     * @param filter
     * @return
     */
    @Override
    public List<ExportedBatchDTO> getExportedBatchRequests(ExportedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        EArchiveBatchFilter archiveBatchFilter = convertExportFilter(filter, pageStart, pageSize);
        List<EArchiveBatchRequestDTO> result = domibusEArchiveService.getBatchRequestList(archiveBatchFilter);
        return result.stream().map(eArchiveBatchDTO -> eArchiveExtMapper.archiveBatchToExportBatch(eArchiveBatchDTO)).collect(Collectors.toList());

    }

    @Override
    public Long getBatchMessageCount(String batchId) {
        return domibusEArchiveService.getBatchUserMessageListCount(batchId);
    }

    @Override
    public List<String> getBatchMessageIds(String batchId, Integer pageStart, Integer pageSize) {
        ListUserMessageDto batchMessageList = domibusEArchiveService.getBatchUserMessageList(batchId, pageStart, pageSize);
        if (batchMessageList == null
                || batchMessageList.getUserMessageDtos() == null
                || batchMessageList.getUserMessageDtos().isEmpty()) {
            return Collections.emptyList();
        }
        return batchMessageList.getUserMessageDtos().stream()
                .map(userMessageDTO -> userMessageDTO.getMessageId()).collect(Collectors.toList());
    }

    @Override
    public BatchStatusDTO reExportBatch(String batchId) {
        EArchiveBatchRequestDTO batchDTO = domibusEArchiveService.reExportBatch(batchId);
        return eArchiveExtMapper.archiveBatchToBatchStatus(batchDTO);
    }

    @Override
    public BatchStatusDTO setBatchClientStatus(String batchId, BatchArchiveStatusType batchStatus) {
        EArchiveBatchStatus status = EArchiveBatchStatus.valueOf(batchStatus.name());
        EArchiveBatchRequestDTO batchDTO = domibusEArchiveService.setBatchClientStatus(batchId, status);
        return eArchiveExtMapper.archiveBatchToBatchStatus(batchDTO);
    }

    @Override
    public List<String> getNotArchivedMessages(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize) {
        ListUserMessageDto list = domibusEArchiveService.getNotArchivedMessages(messageStartDate, messageEndDate, pageStart, pageSize);
        return list.getUserMessageDtos().stream().map(um -> um.getMessageId()).collect(Collectors.toList());
    }

    /**
     * build EArchiveBatchFilter for browsing the queued batches
     *
     * @param filter    request filter data
     * @param pageStart pagination data
     * @param pageSize  pagination size
     * @return genera archive batch filter for searching the queued bathes
     */
    protected EArchiveBatchFilter convertQueuedFilter(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        EArchiveBatchFilter archiveBatchFilter = new EArchiveBatchFilter();
        // return  only QUEUED batches
        archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.QUEUED);
        // set filter
        if (filter.getLastCountRequests() != null || filter.getLastCountRequests() > 0) {
            archiveBatchFilter.setPageSize(filter.getLastCountRequests());
        } else {
            // ignore all other filters and return last count
            archiveBatchFilter.setRequestType(Objects.equals(filter.getRequestType(), BatchRequestTypeParameter.ALL) ? null : filter.getRequestType().name());
            archiveBatchFilter.setStartDate(filter.getStartDate());
            archiveBatchFilter.setEndDate(filter.getEndDate());
            archiveBatchFilter.setRequestType(filter.getRequestType().name());
        }
        // set pagination
        archiveBatchFilter.setPageSize(pageSize);
        archiveBatchFilter.setPageStart(pageStart);
        return archiveBatchFilter;
    }

    /**
     * build EArchiveBatchFilter for browsing the queued batches
     *
     * @param filter    request filter data
     * @param pageStart pagination data
     * @param pageSize  pagination size
     * @return internal archive batch filter for searching the bathes
     */
    protected EArchiveBatchFilter convertExportFilter(ExportedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        EArchiveBatchFilter archiveBatchFilter = new EArchiveBatchFilter();
        // return  only EXPORTED batches
        if (filter.getStatus() == null) {
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.EXPORTED);
        } else if (filter.getStatus().equals(ExportedBatchStatusTypeParameter.ALL)) {
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.EXPORTED);
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.ARCHIVED);
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.ARCHIVE_FAILED);
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.EXPIRED);
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.DELETED);
        } else {
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.valueOf(filter.getStatus().name()));
        }

        if (filter.getReExport()) {
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.REEXPORTED);
        }

        // set filter
        archiveBatchFilter.setMessageStartId(dateToPKUserMessageId(filter.getMessageStartDate()));
        archiveBatchFilter.setMessageEndId(dateToPKUserMessageId(filter.getMessageEndDate()));

        // set pagination
        archiveBatchFilter.setPageSize(pageSize);
        archiveBatchFilter.setPageStart(pageStart);
        return archiveBatchFilter;
    }

    protected Long dateToPKUserMessageId(Long pkUserMessageDate) {
        return pkUserMessageDate == null ? null : pkUserMessageDate * (MAX_INCREMENT_NUMBER + 1);
    }
}
