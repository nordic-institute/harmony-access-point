package eu.domibus.core.earchive;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.api.model.UserMessageDTO;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
@Entity
@Table(name = "TB_EARCHIVE_BATCH")
@NamedQuery(name = "EArchiveBatchEntity.findByEntityId", query = "FROM EArchiveBatchEntity batch where batch.entityId = :BATCH_ENTITY_ID")
@NamedQuery(name = "EArchiveBatchEntity.findByBatchId", query = "FROM EArchiveBatchEntity batch where batch.batchId = :BATCH_ID")
@NamedQuery(name = "EArchiveBatchEntity.findLastEntityIdArchived",
        query = "SELECT max(b.lastPkUserMessage) FROM EArchiveBatchEntity b WHERE b.requestType =  :REQUEST_TYPE")

@SqlResultSetMapping(
        name = "EArchiveBatchRequestDTOMapping",
        classes = @ConstructorResult(
                targetClass = EArchiveBatchRequestDTO.class,
                columns = {
                        @ColumnResult(name = "BATCH_ID", type = String.class),
                        @ColumnResult(name = "REQUEST_TYPE", type = String.class),
                        @ColumnResult(name = "BATCH_STATUS", type = String.class),
                        @ColumnResult(name = "ERROR_CODE", type = String.class),
                        @ColumnResult(name = "ERROR_DETAIL", type = String.class),
                        @ColumnResult(name = "DATE_REQUESTED", type = Date.class),
                        @ColumnResult(name = "MIN_MESSAGE_PK_ID", type = Long.class),
                        @ColumnResult(name = "MAX_MESSAGE_PK_ID", type = Long.class),
                }
        )
)
@SqlResultSetMapping(
        name = "EArchiveBatchRequestDTOCountMapping",
        classes = @ConstructorResult(
                targetClass = Long.class,
                columns = {
                        @ColumnResult(name = "CNT", type = Long.class),
                }
        )
)
@SqlResultSetMapping(
        name = "EArchiveBatchUserMessageMapping",
        classes = @ConstructorResult(
                targetClass = UserMessageDTO.class,
                columns = {
                        @ColumnResult(name = "FK_USER_MESSAGE_ID", type = Long.class),
                        @ColumnResult(name = "MESSAGE_ID", type = String.class),
                }
        )
)
@NamedNativeQuery(name = "EArchiveBatchRequest.getBatchList",
        resultSetMapping = "EArchiveBatchRequestDTOMapping",
        query = "select batch.*, " +
                "       bToBUM.MinId as MIN_MESSAGE_PK_ID, " +
                "       bToBUM.maxId as MAX_MESSAGE_PK_ID  " +
                "   from TB_EARCHIVE_BATCH batch " +
                // self-join to get last copy of the batch
                " LEFT JOIN (select CREATED_FROM_ID_PK, max(ID_PK) as LAST_COPY_ID_PK FROM TB_EARCHIVE_BATCH GROUP BY CREATED_FROM_ID_PK) reExported " +
                "   ON batch.ID_PK = reExported.CREATED_FROM_ID_PK " +
                // join to TB_EARCHIVEBATCH_UM to get min and max message id
                " LEFT JOIN ( SELECT FK_EARCHIVE_BATCH_ID, MIN(FK_USER_MESSAGE_ID) AS MinId, MAX(FK_USER_MESSAGE_ID) AS MaxId  FROM TB_EARCHIVEBATCH_UM GROUP BY FK_EARCHIVE_BATCH_ID ) bToBUM " +
                "   ON batch.ID_PK = bToBUM.FK_EARCHIVE_BATCH_ID " +
                " where  (:batchStartRequestDate IS NULL OR  batch.DATE_REQUESTED >= :batchStartRequestDate) " +
                "    and (:batchEndRequestDate IS NULL OR batch.DATE_REQUESTED < :batchEndRequestDate) " +
                "    and (:requestType IS NULL OR :requestType ='' OR batch.REQUEST_TYPE = :requestType) " +
                "    and (:statusList IS NULL OR :statusList ='' OR batch.BATCH_STATUS in (:statusList) ) " +
                "    and (:messageStartId IS NULL OR :messageStartId ='' OR bToBUM.maxId IS NULL or bToBUM.maxId > :messageStartId) " +
                "    and (:messageEndId IS NULL OR :messageEndId ='' OR bToBUM.MinId IS NULL OR bToBUM.MinId <= :messageEndId) " +
                "    and ((:reExport IS NULL OR :reExport!='true') AND reExported.LAST_COPY_ID_PK IS NULL OR :reExport='true')" +
                " ORDER BY batch.ID_PK DESC"
)

@NamedNativeQuery(name = "EArchiveBatchRequest.getBatchListCount",
        resultSetMapping = "EArchiveBatchRequestDTOCountMapping",
        query = "select COUNT(batch.ID_PK) as CNT" +
                "   from TB_EARCHIVE_BATCH batch " +
                // self-join to get last copy of the batch
                " LEFT JOIN (select CREATED_FROM_ID_PK, max(ID_PK) as LAST_COPY_ID_PK FROM TB_EARCHIVE_BATCH GROUP BY CREATED_FROM_ID_PK) reExported " +
                "   ON batch.ID_PK = reExported.CREATED_FROM_ID_PK " +
                // join to TB_EARCHIVEBATCH_UM to get min and max message id
                " LEFT JOIN ( SELECT FK_EARCHIVE_BATCH_ID, MIN(FK_USER_MESSAGE_ID) AS MinId, MAX(FK_USER_MESSAGE_ID) AS MaxId  FROM TB_EARCHIVEBATCH_UM GROUP BY FK_EARCHIVE_BATCH_ID ) bToBUM " +
                "   ON batch.ID_PK = bToBUM.FK_EARCHIVE_BATCH_ID " +
                " where  (:batchStartRequestDate IS NULL OR  batch.DATE_REQUESTED >= :batchStartRequestDate) " +
                "    and (:batchEndRequestDate IS NULL OR batch.DATE_REQUESTED < :batchEndRequestDate) " +
                "    and (:requestType IS NULL OR :requestType ='' OR batch.REQUEST_TYPE = :requestType) " +
                "    and (:statusList IS NULL OR :statusList ='' OR batch.BATCH_STATUS in (:statusList) ) " +
                "    and (:messageStartId IS NULL OR :messageStartId ='' OR bToBUM.maxId IS NULL or bToBUM.maxId > :messageStartId) " +
                "    and (:messageEndId IS NULL OR :messageEndId ='' OR bToBUM.MinId IS NULL OR bToBUM.MinId <= :messageEndId) " +
                "    and ((:reExport IS NULL OR :reExport!='true') AND reExported.LAST_COPY_ID_PK IS NULL OR :reExport='true')"
)
// UserMessageDTO
@NamedNativeQuery(name = "EArchiveBatchRequest.getMessagesForBatchId",
        resultSetMapping = "EArchiveBatchUserMessageMapping",
        query = "select msgMap.FK_USER_MESSAGE_ID, userMessage.MESSAGE_ID " +
                " FROM TB_EARCHIVE_BATCH batch" +
                " INNER JOIN TB_EARCHIVEBATCH_UM msgMap " +
                "   ON batch.ID_PK = msgMap.FK_EARCHIVE_BATCH_ID " +
                " INNER JOIN TB_USER_MESSAGE userMessage " +
                "   ON msgMap.FK_USER_MESSAGE_ID = userMessage.ID_PK " +
                " WHERE batch.BATCH_ID = :batchId " +
                " ORDER BY msgMap.FK_USER_MESSAGE_ID ASC"
)

@NamedNativeQuery(name = "EArchiveBatchRequest.getNotArchivedMessagesForPeriod",
        resultSetMapping = "EArchiveBatchUserMessageMapping",
        query = "select uml.ID_PK as FK_USER_MESSAGE_ID, um.MESSAGE_ID as MESSAGE_ID " +
                " FROM TB_USER_MESSAGE_LOG uml " +
                " INNER JOIN TB_USER_MESSAGE um " +
                "   ON uml.ID_PK = um.ID_PK " +
                " LEFT JOIN TB_EARCHIVEBATCH_UM msgMap " +
                "   ON uml.ID_PK = msgMap.FK_USER_MESSAGE_ID "+
                " WHERE msgMap.ID_PK IS NULL " +
                "  AND uml.ID_PK > :LAST_ENTITY_ID " +
                "  AND uml.ID_PK < :MAX_ENTITY_ID " +
                "  AND uml.DELETED IS NULL " +
                "  AND uml.ARCHIVED IS NULL " +
                " ORDER BY uml.ID_PK asc"
)

public class EArchiveBatchEntity extends AbstractBaseEntity {

    @Column(name = "BATCH_ID")
    private String batchId;

    @Column(name = "REQUEST_TYPE")
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(name = "BATCH_STATUS")
    @Enumerated(EnumType.STRING)
    private EArchiveBatchStatus eArchiveBatchStatus;

    @Column(name = "DATE_REQUESTED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRequested;

    @Column(name = "LAST_PK_USER_MESSAGE")
    private Long lastPkUserMessage;

    @Column(name = "BATCH_SIZE")
    private Integer batchSize;

    @Column(name = "ERROR_CODE")
    private String errorCode;

    @Column(name = "ERROR_DETAIL")
    private String errorMessage;

    @Column(name = "CREATED_FROM_ID_PK")
    private Long createdFromBatchIdPk;

    @Column(name = "STORAGE_LOCATION")
    private String storageLocation;

    /*
        @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<EArchiveBatchUserMessage> eArchiveBatchUserMessages;
    */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "MESSAGEIDS_JSON")
    protected byte[] messageIdsJson;

    public void setMessageIdsJson(String rawJson) {
        byte[] bytes = rawJson.getBytes(StandardCharsets.UTF_8);
        this.messageIdsJson = bytes;
    }

    public void setMessageIdsJson(byte[] bytes) {
        this.messageIdsJson = bytes;
    }

    public byte[] getMessageIdsJson() {
        return messageIdsJson;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Date getDateRequested() {
        return dateRequested;
    }

    public void setDateRequested(Date dateRequested) {
        this.dateRequested = dateRequested;
    }

    public Long getLastPkUserMessage() {
        return lastPkUserMessage;
    }

    public void setLastPkUserMessage(Long lastPkUserMessage) {
        this.lastPkUserMessage = lastPkUserMessage;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer size) {
        this.batchSize = size;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public EArchiveBatchStatus geteArchiveBatchStatus() {
        return eArchiveBatchStatus;
    }

    public void seteArchiveBatchStatus(EArchiveBatchStatus eArchiveBatchStatus) {
        this.eArchiveBatchStatus = eArchiveBatchStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getCreatedFromBatchIdPk() {
        return createdFromBatchIdPk;
    }

    public void setCreatedFromBatchIdPk(Long createdFromBatchIdPk) {
        this.createdFromBatchIdPk = createdFromBatchIdPk;
    }

    @Override
    public String toString() {
        return "EArchiveBatchEntity{" +
                "batchId='" + batchId + '\'' +
                ", requestType=" + requestType +
                ", eArchiveBatchStatus=" + eArchiveBatchStatus +
                ", dateRequested=" + dateRequested +
                ", lastPkUserMessage=" + lastPkUserMessage +
                ", batchSize=" + batchSize +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", createdFromBatchIdPk=" + createdFromBatchIdPk +
                ", storageLocation='" + storageLocation + '\'' +
                ", messageIdsJson=" + Arrays.toString(messageIdsJson) +
                '}';
    }
}
