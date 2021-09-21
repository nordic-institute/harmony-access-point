package eu.domibus.core.earchive;

import eu.domibus.api.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
@Entity
@Table(name = "TB_EARCHIVE_BATCH")
@NamedQuery(name = "EArchiveBatch.findByBatchId", query = "FROM EArchiveBatch batch where batch.entityId = :BATCH_ENTITY_ID")
@NamedQuery(name = "EArchiveBatch.findLastEntityIdArchived", query = "SELECT batch.lastPkUserMessage FROM EArchiveBatch batch where batch.entityId = (SELECT max(b.entityId) FROM EArchiveBatch b)")
public class EArchiveBatch  extends AbstractBaseEntity {

    @Column(name = "BATCH_ID")
    private String batchId;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    @NotNull
    private EArchiveBatchStatus eArchiveBatchStatus;

    @Column(name = "REQUEST_TYPE")
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(name = "DATE_REQUESTED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRequested;

    @Column(name = "LAST_PK_USER_MESSAGE")
    private Long lastPkUserMessage;

    @Column(name = "SIZE")
    private Integer size;

    @Column(name = "STORAGE_LOCATION")
    private String storageLocation;

    @Lob
    @Column(name = "MESSAGEIDS_JSON")
    protected byte[] messageIdsJson;

    public void setMessageIdsJson(String rawJson) {
        byte[] bytes = rawJson.getBytes(StandardCharsets.UTF_8);
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

    public EArchiveBatchStatus getEArchiveBatchStatus() {
        return eArchiveBatchStatus;
    }

    public void setEArchiveBatchStatus(EArchiveBatchStatus status) {
        this.eArchiveBatchStatus = status;
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

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
}
