package eu.domibus.ext.delegate.services.earchive;

import eu.domibus.api.earchive.*;
import eu.domibus.ext.delegate.mapper.EArchiveExtMapper;
import eu.domibus.ext.domain.archive.*;
import eu.domibus.ext.services.DomibusEArchiveExtService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX_INCREMENT_NUMBER;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class DomibusEArchiveServiceDelegate implements DomibusEArchiveExtService {
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusEArchiveServiceDelegate.class);
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
     */
    @Override
    public Long getQueuedBatchRequestsCount(QueuedBatchFilterDTO filter) {
        EArchiveBatchFilter archiveBatchFilter = convertQueuedFilter(filter, null, null);
        return domibusEArchiveService.getBatchRequestListCount(archiveBatchFilter);
    }

    /**
     * Method returns Queued batches in database for given search filter and page
     */
    @Override
    public List<BatchDTO> getQueuedBatchRequests(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        EArchiveBatchFilter archiveBatchFilter = convertQueuedFilter(filter, pageStart, pageSize);
        List<EArchiveBatchRequestDTO> result = domibusEArchiveService.getBatchRequestList(archiveBatchFilter);
        return result.stream().map(eArchiveExtMapper::archiveBatchToBatch).collect(Collectors.toList());
    }

    /**
     * Method returns count of Exported batches in database for given search filter
     */
    @Override
    public Long getExportedBatchRequestsCount(ExportedBatchFilterDTO filter) {
        EArchiveBatchFilter archiveBatchFilter = convertExportFilter(filter, null, null);
        return domibusEArchiveService.getBatchRequestListCount(archiveBatchFilter);
    }

    /**
     * Method returns Exported batches in database for given search filter and page
     */
    @Override
    public List<BatchDTO> getExportedBatchRequests(ExportedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        EArchiveBatchFilter archiveBatchFilter = convertExportFilter(filter, pageStart, pageSize);
        List<EArchiveBatchRequestDTO> result = domibusEArchiveService.getBatchRequestList(archiveBatchFilter);
        return result.stream().map(eArchiveExtMapper::archiveBatchToBatch).collect(Collectors.toList());
    }

    @Override
    public Long getExportedBatchMessageCount(String batchId) {
        return domibusEArchiveService.getExportedBatchUserMessageListCount(batchId);
    }

    @Override
    public List<String> getExportedBatchMessageIds(String batchId, Integer pageStart, Integer pageSize) {
        List<String> batchMessageList = domibusEArchiveService.getExportedBatchUserMessageList(batchId, pageStart, pageSize);
        if (CollectionUtils.isEmpty(batchMessageList)) {
            return Collections.emptyList();
        }
        return batchMessageList;
    }

    @Override
    public BatchStatusDTO reExportBatch(String batchId) {
        EArchiveBatchRequestDTO batchDTO = domibusEArchiveService.reExportBatch(batchId);
        return eArchiveExtMapper.archiveBatchToBatchStatus(batchDTO);
    }

    @Override
    public BatchDTO getBatch(String batchId) {
        EArchiveBatchRequestDTO batch = domibusEArchiveService.getBatch(batchId);
        return eArchiveExtMapper.archiveBatchToBatch(batch);
    }

    @Override
    public BatchStatusDTO setBatchClientStatus(String batchId, BatchArchiveStatusType batchStatus, String message) {
        EArchiveBatchStatus status = EArchiveBatchStatus.valueOf(batchStatus.name());
        EArchiveBatchRequestDTO batchDTO = domibusEArchiveService.setBatchClientStatus(batchId, status, message);
        return eArchiveExtMapper.archiveBatchToBatchStatus(batchDTO);
    }

    @Override
    public List<String> getNotArchivedMessages(NotArchivedMessagesFilterDTO filter, Integer pageStart, Integer pageSize) {
        return domibusEArchiveService.getNotArchivedMessages(
                dateToPKUserMessageId(filter.getMessageStartDate()),
                dateToPKUserMessageId(filter.getMessageEndDate()),
                pageStart, pageSize);
    }

    @Override
    public Long getNotArchivedMessageCount(NotArchivedMessagesFilterDTO filter) {
        return domibusEArchiveService.getNotArchivedMessagesCount(
                dateToPKUserMessageId(filter.getMessageStartDate()),
                dateToPKUserMessageId(filter.getMessageEndDate()));
    }

    /**
     * build EArchiveBatchFilter for browsing the queued batches
     *
     * @param filter    request filter data
     * @param pageStart pagination data
     * @param pageSize  pagination size
     * @return genera archive batch filter for searching the queued batches
     */
    protected EArchiveBatchFilter convertQueuedFilter(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        LOG.trace("Create batch filter from the Queued filter [{}]!", filter);
        EArchiveBatchFilter archiveBatchFilter = new EArchiveBatchFilter();
        // return  only QUEUED batches
        LOG.trace("Always return only batches in status QUEUED!");
        archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.QUEUED);
        // set pagination
        archiveBatchFilter.setPageSize(pageSize);
        archiveBatchFilter.setPageStart(pageStart);
        // set filter
        if (filter.getLastCountRequests() != null && filter.getLastCountRequests() > 0) {
            // Explained behaviour: if last count is given then ignore all other filters.
            LOG.trace("Return last count request and ignore all other filters amd set the page size to getLastCountRequests");
            archiveBatchFilter.setPageSize(filter.getLastCountRequests());
        } else {
            setBatchRequestTypes(archiveBatchFilter, filter.getRequestTypes());
            archiveBatchFilter.setStartDate(filter.getStartDate());
            archiveBatchFilter.setEndDate(filter.getEndDate());
        }

        return archiveBatchFilter;
    }

    /**
     * build EArchiveBatchFilter for browsing the queued batches
     *
     * @param filter    request filter data
     * @param pageStart pagination data
     * @param pageSize  pagination size
     * @return internal archive batch filter for searching the batches
     */
    protected EArchiveBatchFilter convertExportFilter(ExportedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        LOG.trace("Create batch filter from the export filter [{}]!", filter);
        EArchiveBatchFilter archiveBatchFilter = new EArchiveBatchFilter();
        // return  only EXPORTED batches
        if (filter.getStatuses().isEmpty()) {
            LOG.trace("Add default batch status filter: EXPORTED");
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.EXPORTED);
        } else {
            filter.getStatuses().forEach(requestedStatusType -> archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.valueOf(requestedStatusType.name())));
        }

        // set filter
        archiveBatchFilter.setMessageStartId(dateToPKUserMessageId(filter.getMessageStartDate()));
        archiveBatchFilter.setMessageEndId(dateToPKUserMessageId(filter.getMessageEndDate()));
        archiveBatchFilter.setIncludeReExportedBatches(filter.getIncludeReExportedBatches());

        // set pagination
        archiveBatchFilter.setPageSize(pageSize);
        archiveBatchFilter.setPageStart(pageStart);
        LOG.trace("Converted export filter: [{}].", archiveBatchFilter);
        return archiveBatchFilter;
    }

    private void setBatchRequestTypes(EArchiveBatchFilter archiveBatchFilter, List<BatchRequestType> requestTypes) {
        requestTypes.forEach(batchRequestType -> archiveBatchFilter.getRequestTypes().add(EArchiveRequestType.valueOf(batchRequestType.name())));
        if(archiveBatchFilter.getRequestTypes().contains(EArchiveRequestType.CONTINUOUS)) {
            archiveBatchFilter.getRequestTypes().add(EArchiveRequestType.SANITIZER);
        }
    }

    protected Long dateToPKUserMessageId(Long pkUserMessageDate) {
        return pkUserMessageDate == null ? null : pkUserMessageDate * (MAX_INCREMENT_NUMBER + 1);
    }
}
