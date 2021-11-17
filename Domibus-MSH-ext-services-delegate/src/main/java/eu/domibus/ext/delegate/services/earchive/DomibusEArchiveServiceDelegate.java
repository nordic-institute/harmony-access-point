package eu.domibus.ext.delegate.services.earchive;

import eu.domibus.api.earchive.*;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.ext.delegate.mapper.EArchiveExtMapper;
import eu.domibus.ext.domain.archive.*;
import eu.domibus.ext.services.DomibusEArchiveExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

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
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusEArchiveServiceDelegate.class);
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
    public BatchStatusDTO setBatchClientStatus(String batchId, BatchArchiveStatusType batchStatus, String message) {
        EArchiveBatchStatus status = EArchiveBatchStatus.valueOf(batchStatus.name());
        EArchiveBatchRequestDTO batchDTO = domibusEArchiveService.setBatchClientStatus(batchId, status, message);
        return eArchiveExtMapper.archiveBatchToBatchStatus(batchDTO);
    }

    @Override
    public List<String> getNotArchivedMessages(NotArchivedMessagesFilterDTO filter, Integer pageStart, Integer pageSize) {
        ListUserMessageDto list = domibusEArchiveService.getNotArchivedMessages(filter.getMessageStartDate(), filter.getMessageEndDate(), pageStart, pageSize);
        return list.getUserMessageDtos().stream().map(um -> um.getMessageId()).collect(Collectors.toList());
    }

    @Override
    public Long getNotArchivedMessageCount(NotArchivedMessagesFilterDTO filter) {
        return domibusEArchiveService.getNotArchivedMessagesCount(filter.getMessageStartDate(), filter.getMessageEndDate());
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
        LOG.trace("Create batch filter from the Queued filter [{}]!", filter);
        EArchiveBatchFilter archiveBatchFilter = new EArchiveBatchFilter();
        // return  only QUEUED batches
        LOG.trace("Always return only batches in status QUEUED!");
        archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.QUEUED);
        // set filter
        if (filter.getLastCountRequests() != null && filter.getLastCountRequests() > 0) {
            // Explained behaviour: if last count is given then ignore all other filters.
            LOG.trace("Return last count request and ignore all other filters");
            archiveBatchFilter.setPageSize(filter.getLastCountRequests());
        } else {

            setBatchStatus(archiveBatchFilter, filter.getRequestTypes());
            archiveBatchFilter.setStartDate(filter.getStartDate());
            archiveBatchFilter.setEndDate(filter.getEndDate());

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
        LOG.trace("Create batch filter from the export filter [{}]!", filter);
        EArchiveBatchFilter archiveBatchFilter = new EArchiveBatchFilter();
        // return  only EXPORTED batches
        if (filter.getStatuses().isEmpty()) {
            LOG.trace("Add default batch status filter: EXPORTED");
            archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.EXPORTED);
        } else {
            filter.getStatuses().forEach(requestedStatusType -> archiveBatchFilter.getStatusList().add(EArchiveBatchStatus.valueOf(requestedStatusType.name())));
        }
        // set filter for all
        setBatchStatus(archiveBatchFilter, filter.getRequestTypes());

        // set filter
        archiveBatchFilter.setMessageStartId(dateToPKUserMessageId(filter.getMessageStartDate()));
        archiveBatchFilter.setMessageEndId(dateToPKUserMessageId(filter.getMessageEndDate()));

        // set pagination
        archiveBatchFilter.setPageSize(pageSize);
        archiveBatchFilter.setPageStart(pageStart);
        LOG.trace("Converted export filter: [{}].", archiveBatchFilter);
        return archiveBatchFilter;
    }

    private void setBatchStatus(EArchiveBatchFilter archiveBatchFilter, List<BatchRequestType> requestTypes) {
        requestTypes.forEach(batchRequestType -> archiveBatchFilter.getRequestTypes().add(EArchiveRequestType.valueOf(batchRequestType.name())));
        if(archiveBatchFilter.getRequestTypes().contains(EArchiveRequestType.CONTINUOUS)) {
            archiveBatchFilter.getRequestTypes().add(EArchiveRequestType.SANITIZER);
        }
    }

    protected Long dateToPKUserMessageId(Long pkUserMessageDate) {
        return pkUserMessageDate == null ? null : pkUserMessageDate * (MAX_INCREMENT_NUMBER + 1);
    }
}
