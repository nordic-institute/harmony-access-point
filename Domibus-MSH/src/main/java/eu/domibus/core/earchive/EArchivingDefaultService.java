package eu.domibus.core.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.api.earchive.EArchiveBatchDTO;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.core.converter.EArchiveBatchMapper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.criterion.CriteriaQuery;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.dateToPKUserMessageId;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.extractDateFromPKUserMessageId;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchivingDefaultService implements DomibusEArchiveService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingDefaultService.class);

    public static final int CONTINUOUS_ID = 1;

    public static final int SANITY_ID = 2;

    private final EArchiveBatchDao eArchiveBatchDao;
    private final EArchiveBatchStartDao eArchiveBatchStartDao;
    private final EArchiveBatchMapper eArchiveBatchMapper;

    public EArchivingDefaultService(EArchiveBatchDao eArchiveBatchDao,
                                    EArchiveBatchStartDao eArchiveBatchStartDao,
                                    EArchiveBatchMapper eArchiveBatchMapper) {
        this.eArchiveBatchStartDao = eArchiveBatchStartDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.eArchiveBatchMapper = eArchiveBatchMapper;
    }

    @Override
    public void updateStartDateContinuousArchive(Long startMessageDate) {
        updateEArchiveBatchStart(CONTINUOUS_ID, startMessageDate);
    }

    @Override
    public void updateStartDateSanityArchive(Long startMessageDate) {
        updateEArchiveBatchStart(SANITY_ID, startMessageDate);
    }

    @Override
    public Long getStartDateContinuousArchive() {
        return extractDateFromPKUserMessageId(eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage());
    }

    @Override
    public Long getStartDateSanityArchive() {
        return extractDateFromPKUserMessageId(eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage());
    }

    private void updateEArchiveBatchStart(int sanityId, Long startMessageDate) {
        EArchiveBatchStart byReference = eArchiveBatchStartDao.findByReference(sanityId);
        byReference.setLastPkUserMessage(dateToPKUserMessageId(startMessageDate));
        eArchiveBatchStartDao.update(byReference);
    }

    @Override
    public Long getQueuedBatchRequestsCount(EArchiveBatchFilter filter) {
        return eArchiveBatchDao.getQueuedBatchRequestsCount(filter);
    }

    @Override
    public List<EArchiveBatchDTO> getQueuedBatchRequests(EArchiveBatchFilter filter) {
        List<EArchiveBatchEntity> list = eArchiveBatchDao.getQueuedBatchRequests(filter);
        return list.stream().map(eArchiveBatchEntity -> eArchiveBatchMapper.entityToDto(eArchiveBatchEntity)).collect(Collectors.toList());
    }

}
