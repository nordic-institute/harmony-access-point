package eu.domibus.core.earchive;

import eu.domibus.api.model.AbstractBaseEntity;

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
@NamedQuery(name = "EArchiveBatchEntity.findByBatchId", query = "FROM EArchiveBatchEntity batch where batch.entityId = :BATCH_ENTITY_ID")
@NamedQuery(name = "EArchiveBatchEntity.findLastEntityIdArchived",
        query = "SELECT max(b.lastPkUserMessage) FROM EArchiveBatchEntity b WHERE b.requestType =  :REQUEST_TYPE")
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

    @Column(name = "STORAGE_LOCATION")
    private String storageLocation;

    @Lob
    @Basic(fetch = FetchType.LAZY)
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

    @Override
    public String toString() {
        return "EArchiveBatchEntity{" +
                "batchId='" + batchId + '\'' +
                ", requestType=" + requestType +
                ", eArchiveBatchStatus=" + eArchiveBatchStatus +
                ", dateRequested=" + dateRequested +
                ", lastPkUserMessage=" + lastPkUserMessage +
                ", batchSize=" + batchSize +
                ", storageLocation='" + storageLocation + '\'' +
                ", messageIdsJson=" + Arrays.toString(messageIdsJson) +
                "} " + super.toString();
    }
}
