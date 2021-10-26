package eu.domibus.core.earchive;

import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX;
import static eu.domibus.core.earchive.RequestType.CONTINUOUS;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class EArchiveBatchDao extends BasicDao<EArchiveBatchEntity> {

    public EArchiveBatchDao() {
        super(EArchiveBatchEntity.class);
    }

    public EArchiveBatchEntity findEArchiveBatchByBatchId(long entityId) {
        TypedQuery<EArchiveBatchEntity> query = this.em.createNamedQuery("EArchiveBatchEntity.findByEntityId", EArchiveBatchEntity.class);
        query.setParameter("BATCH_ENTITY_ID", entityId);

        return getFirstResult(query);
    }

    public EArchiveBatchEntity findEArchiveBatchByBatchId(String batchId) {
        TypedQuery<EArchiveBatchEntity> query = this.em.createNamedQuery("EArchiveBatchEntity.findByBatchId", EArchiveBatchEntity.class);
        query.setParameter("BATCH_ID", batchId);

        return getFirstResult(query);
    }

    protected <T> T getFirstResult(TypedQuery<T> query) {
        List<T> resultList = query.getResultList();
        if (isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }

    public Long findLastEntityIdArchived() {
        TypedQuery<Long> query = this.em.createNamedQuery("EArchiveBatchEntity.findLastEntityIdArchived", Long.class);
        query.setParameter("REQUEST_TYPE", CONTINUOUS);
        List<Long> resultList = query.getResultList();
        if (isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }

    @Transactional
    public EArchiveBatchEntity setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status) {
        eArchiveBatchByBatchId.seteArchiveBatchStatus(status);
        return merge(eArchiveBatchByBatchId);
    }

    public List<EArchiveBatchRequestDTO> getBatchRequestList(EArchiveBatchFilter filter) {
        TypedQuery<EArchiveBatchRequestDTO> batchQuery = em.createNamedQuery("EArchiveBatchRequest.getBatchList", EArchiveBatchRequestDTO.class);
        // set parameters
        setBatchQueryParametersFromFilter(batchQuery, filter);
        // set pagination
        setPaginationParametersToQuery(batchQuery, filter.getPageSize(), filter.getPageSize());

        return batchQuery.getResultList();
    }

    public List<UserMessageDTO> getNotArchivedMessages(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize) {
        TypedQuery<UserMessageDTO> query = em.createNamedQuery("EArchiveBatchRequest.getNotArchivedMessagesForPeriod", UserMessageDTO.class);
        query.setParameter("LAST_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageStartDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("MAX_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageEndDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));

        setPaginationParametersToQuery(query, pageSize, pageStart);

        return query.getResultList();
    }

    public List<UserMessageDTO> getBatchMessageList(String batchId) {
        TypedQuery<UserMessageDTO> query = em.createNamedQuery("EArchiveBatchRequest.getMessagesForBatchId", UserMessageDTO.class);
        query.setParameter("batchId",batchId );
        return query.getResultList();
    }

    public Long getBatchRequestListCount(EArchiveBatchFilter filter) {
        TypedQuery<Long> countQuery = em.createNamedQuery("EArchiveBatchRequest.getBatchListCount", Long.class);
        setBatchQueryParametersFromFilter(countQuery, filter);
        return countQuery.getSingleResult();
    }

    public <T> void setBatchQueryParametersFromFilter(TypedQuery query, EArchiveBatchFilter filter) {
        putQueryParameter(query, "batchStartRequestDate", filter.getStartDate());
        putQueryParameter(query, "batchEndRequestDate", filter.getEndDate());
        putQueryParameter(query, "requestType", filter.getRequestType());
        putQueryParameter(query, "statusList", filter.getStatusList());
        putQueryParameter(query, "messageStartId", filter.getMessageStarId());
        putQueryParameter(query, "messageEndId", filter.getMessageEndDate());
        putQueryParameter(query, "reExport", filter.getShowReExported());
    }

    public <T> void putQueryParameter(TypedQuery query, String paramName, T value) {
        if (value == null) {
            query.setParameter(paramName, null);
        } else if (value instanceof List) {
            query.setParameter(paramName, ((List) value).isEmpty() ? "" : value);
        } else {
            query.setParameter(paramName, value);
        }
    }

    public <T> void setPaginationParametersToQuery(TypedQuery<T> query, Integer pageSize, Integer pageStart){
        // set pagination
        if (pageSize == null || pageSize <0){
            // page size is not given - can not set pagination. Return all results
            return;
        }

        // if page is not set start with the fist page
        int startingAt = (pageStart == null || pageStart < 0 ?  0: pageStart) * pageSize;
        query.setMaxResults(pageSize);
        query.setFirstResult(startingAt);

    }


}
