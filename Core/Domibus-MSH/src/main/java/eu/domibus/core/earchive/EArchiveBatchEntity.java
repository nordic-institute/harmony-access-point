package eu.domibus.core.earchive;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Entity
@Table(name = "TB_EARCHIVE_BATCH")
@NamedQuery(name = "EArchiveBatchEntity.findByEntityId", query = "FROM EArchiveBatchEntity batch where batch.entityId = :BATCH_ENTITY_ID")
@NamedQuery(name = "EArchiveBatchEntity.findByBatchId", query = "FROM EArchiveBatchEntity batch where batch.batchId = :BATCH_ID")
@NamedQuery(name = "EArchiveBatchEntity.findByStatus", query = "FROM EArchiveBatchEntity b where b.eArchiveBatchStatus in :STATUSES order by b.entityId asc")
@NamedQuery(name = "EArchiveBatchEntity.updateStatusByDate", query = "UPDATE EArchiveBatchEntity b set b.eArchiveBatchStatus=:NEW_STATUS where b.eArchiveBatchStatus in :STATUSES and b.dateRequested < :LIMIT_DATE")
public class EArchiveBatchEntity extends AbstractBaseEntity {

    @Column(name = "BATCH_ID")
    protected String batchId;

    @Column(name = "ORIGINAL_BATCH_ID")
    protected String originalBatchId;

    @Column(name = "REEXPORTED")
    protected Boolean reExported = Boolean.FALSE;

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

    @Column(name = "DOMIBUS_CODE")
    protected String domibusCode;

    @Column(name = "MESSAGE")
    protected String message;

    @Column(name = "STORAGE_LOCATION")
    protected String storageLocation;

    @Column(name = "FIRST_PK_USER_MESSAGE")
    private Long firstPkUserMessage;

    @Column(name = "MANIFEST_CHECK_SUM")
    protected String manifestChecksum;

    @Transient
    private List<EArchiveBatchUserMessage> eArchiveBatchUserMessages;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getOriginalBatchId() {
        return originalBatchId;
    }

    public void setOriginalBatchId(String originalBatchId) {
        this.originalBatchId = originalBatchId;
    }

    public Boolean getReExported() {
        return reExported;
    }

    public void setReExported(Boolean reExported) {
        this.reExported = reExported;
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

    public String getDomibusCode() {
        return domibusCode;
    }

    public void setDomibusCode(String domibusCode) {
        this.domibusCode = domibusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<EArchiveBatchUserMessage> geteArchiveBatchUserMessages() {
        return eArchiveBatchUserMessages;
    }

    public void seteArchiveBatchUserMessages(List<EArchiveBatchUserMessage> eArchiveBatchUserMessages) {
        this.eArchiveBatchUserMessages = eArchiveBatchUserMessages;
    }

    public String getManifestChecksum() {
        return manifestChecksum;
    }

    public void setManifestChecksum(String manifestChecksum) {
        this.manifestChecksum = manifestChecksum;
    }

    @Override
    public String toString() {
        return "EArchiveBatchEntity{" +
                "batchId='" + batchId + '\'' +
                ", originalBatchId='" + originalBatchId + '\'' +
                ", reExported=" + reExported +
                ", requestType=" + requestType +
                ", eArchiveBatchStatus=" + eArchiveBatchStatus +
                ", dateRequested=" + dateRequested +
                ", firstPkUserMessage=" + firstPkUserMessage +
                ", lastPkUserMessage=" + lastPkUserMessage +
                ", batchSize=" + batchSize +
                ", code='" + domibusCode + '\'' +
                ", description='" + message + '\'' +
                ", storageLocation='" + storageLocation + '\'' +
                ", manifestChecksum='" + manifestChecksum + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EArchiveBatchEntity that = (EArchiveBatchEntity) o;

        return new EqualsBuilder().appendSuper(super.equals(o))
                .append(batchId, that.batchId)
                .append(requestType, that.requestType)
                .append(eArchiveBatchStatus, that.eArchiveBatchStatus)
                .append(dateRequested, that.dateRequested)
                .append(lastPkUserMessage, that.lastPkUserMessage)
                .append(batchSize, that.batchSize)
                .append(domibusCode, that.domibusCode)
                .append(message, that.message)
                .append(storageLocation, that.storageLocation)
                .append(firstPkUserMessage, that.firstPkUserMessage)
                .append(manifestChecksum, that.manifestChecksum)
                .append(reExported, that.reExported)
                .append(originalBatchId, that.originalBatchId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(batchId)
                .append(requestType)
                .append(eArchiveBatchStatus)
                .append(dateRequested)
                .append(lastPkUserMessage)
                .append(batchSize)
                .append(domibusCode)
                .append(message)
                .append(storageLocation)
                .append(firstPkUserMessage)
                .append(manifestChecksum)
                .append(reExported)
                .append(originalBatchId)
                .toHashCode();
    }
}

