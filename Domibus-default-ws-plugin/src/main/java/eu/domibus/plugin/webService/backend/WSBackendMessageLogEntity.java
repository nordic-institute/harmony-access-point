package eu.domibus.plugin.webService.backend;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
@Entity
@Table(name = "WS_PLUGIN_TB_BACKEND_MESSAGE_LOG")
@NamedQuery(name = "WSBackendMessageLogEntity.findByMessageId",
        query = "select wsBackendMessageLogEntity " +
                "from WSBackendMessageLogEntity wsBackendMessageLogEntity " +
                "where wsBackendMessageLogEntity.messageId=:MESSAGE_ID")
public class WSBackendMessageLogEntity {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSBackendMessageLogEntity.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PK")
    private long entityId;

    @Column(name = "CREATION_TIME", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "MODIFICATION_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @Column(name = "CREATED_BY", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "MODIFIED_BY")
    private String modifiedBy;

    @Column(name = "MESSAGE_ID", nullable = false)
    private String messageId;

    @Column(name = "FINAL_RECIPIENT")
    private String finalRecipient;

    @Column(name = "BACKEND_MESSAGE_STATUS")
    @Enumerated(EnumType.STRING)
    private WSBackendMessageStatus messageStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "BACKEND_MESSAGE_TYPE")
    private WSBackendMessageType type;

    @Column(name = "ENDPOINT")
    private String endpoint;

    @Column(name = "SENT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sent;

    @Column(name = "FAILED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date failed;

    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;

    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    @Column(name = "SCHEDULED")
    protected Boolean scheduled;

    public WSBackendMessageLogEntity() {
        String user = LOG.getMDC(DomibusLogger.MDC_USER);
        if (StringUtils.isBlank(user)) {
            user = "wsplugin_default";
        }
        setCreatedBy(user);
        setSent(new Date());
        setCreationTime(new Date());
        setModificationTime(new Date());
        setSendAttempts(0);
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFinalRecipient() {
        return finalRecipient;
    }

    public void setFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
    }

    public WSBackendMessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(WSBackendMessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public WSBackendMessageType getType() {
        return type;
    }

    public void setType(WSBackendMessageType type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public Date getFailed() {
        return failed;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public void setSendAttempts(int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public int getSendAttemptsMax() {
        return sendAttemptsMax;
    }

    public void setSendAttemptsMax(int sendAttemptsMax) {
        this.sendAttemptsMax = sendAttemptsMax;
    }

    public Date getNextAttempt() {
        return nextAttempt;
    }

    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public Boolean getScheduled() {
        return scheduled;
    }

    public void setScheduled(Boolean scheduled) {
        this.scheduled = scheduled;
    }

}
