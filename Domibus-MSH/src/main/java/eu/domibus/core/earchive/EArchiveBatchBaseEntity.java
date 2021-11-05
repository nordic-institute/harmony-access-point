package eu.domibus.core.earchive;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.AbstractBaseEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * To enable loading batch without Clobs:
 * https://vladmihalcea.com/the-best-way-to-lazy-load-entity-attributes-using-jpa-and-hibernate/
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@MappedSuperclass
public class EArchiveBatchBaseEntity extends AbstractBaseEntity {
    @Column(name = "BATCH_ID")
    protected String batchId;

    @Column(name = "REQUEST_TYPE")
    @Enumerated(EnumType.STRING)
    protected EArchiveRequestType requestType;

    @Column(name = "BATCH_STATUS")
    @Enumerated(EnumType.STRING)
    protected EArchiveBatchStatus eArchiveBatchStatus;

    @Column(name = "DATE_REQUESTED")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date dateRequested;

    @Column(name = "LAST_PK_USER_MESSAGE")
    protected Long lastPkUserMessage;

    @Column(name = "BATCH_SIZE")
    protected Integer batchSize;

    @Column(name = "ERROR_CODE")
    protected String errorCode;

    @Column(name = "ERROR_DETAIL")
    protected String errorMessage;

    @Column(name = "STORAGE_LOCATION")
    protected String storageLocation;

    @Column(name = "FIRST_PK_USER_MESSAGE")
    private Long firstPkUserMessage;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public EArchiveRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(EArchiveRequestType requestType) {
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

    public Long getFirstPkUserMessage() {
        return firstPkUserMessage;
    }

    public void setFirstPkUserMessage(Long firstPkUserMessage) {
        this.firstPkUserMessage = firstPkUserMessage;
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

    public EArchiveBatchStatus getEArchiveBatchStatus() {
        return eArchiveBatchStatus;
    }

    public void setEArchiveBatchStatus(EArchiveBatchStatus eArchiveBatchStatus) {
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

    @Override
    public String toString() {
        return "EArchiveBatchBaseEntity{" +
                "batchId='" + batchId + '\'' +
                ", requestType=" + requestType +
                ", eArchiveBatchStatus=" + eArchiveBatchStatus +
                ", dateRequested=" + dateRequested +
                ", lastPkUserMessage=" + lastPkUserMessage +
                ", batchSize=" + batchSize +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", storageLocation='" + storageLocation + '\'' +
                ", firstPkUserMessage=" + firstPkUserMessage +
                '}';
    }
}
