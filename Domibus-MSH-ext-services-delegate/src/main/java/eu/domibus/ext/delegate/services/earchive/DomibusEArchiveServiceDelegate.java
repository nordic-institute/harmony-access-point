package eu.domibus.ext.delegate.services.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.ext.domain.archive.BatchRequestTypeParameter;
import eu.domibus.ext.domain.archive.QueuedBatchDTO;
import eu.domibus.ext.services.DomibusEArchiveExtService;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class DomibusEArchiveServiceDelegate implements DomibusEArchiveExtService {

    private final DomibusEArchiveService domibusEArchiveService;

    public DomibusEArchiveServiceDelegate(DomibusEArchiveService domibusEArchiveService) {
        this.domibusEArchiveService = domibusEArchiveService;
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

    @Override
    public Integer getQueuedBatchRequestsCount(BatchRequestTypeParameter requestType, ZonedDateTime startDate, ZonedDateTime endDate) {
        return null;
    }

    @Override
    public List<QueuedBatchDTO> getQueuedBatchRequests(BatchRequestTypeParameter requestType, ZonedDateTime startDate, ZonedDateTime endDate, Integer pageStart, Integer pageSize) {
        return null;
    }
}
