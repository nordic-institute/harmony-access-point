package eu.domibus.core.earchive;

import eu.domibus.api.model.DomibusBaseEntity;
import eu.domibus.common.JPAConstants;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@Entity
@Table(name = "TB_EARCHIVEBATCH_UM")
@NamedQuery(name = "EArchiveBatchUserMessage.findByArchiveBatchId", query = "FROM EArchiveBatchUserMessage batchUms where batchUms.eArchiveBatch.batchId = :batchId ORDER BY batchUms.userMessageEntityId asc")
public class EArchiveBatchUserMessage implements DomibusBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JPAConstants.DOMIBUS_SCALABLE_SEQUENCE)
    @GenericGenerator(
            name = JPAConstants.DOMIBUS_SCALABLE_SEQUENCE,
            strategy = JPAConstants.DATE_PREFIXED_SEQUENCE_ID_GENERATOR)
    @Column(name = "ID_PK")
    private long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_EARCHIVE_BATCH_ID")
    private EArchiveBatchEntity eArchiveBatch;

    @Column(name = "FK_USER_MESSAGE_ID")
    private Long userMessageEntityId;

    @Column(name = "MESSAGE_ID")
    private String messageId;

    public EArchiveBatchUserMessage() {
    }

    public EArchiveBatchUserMessage(Long userMessageEntityId, String messageId) {
        this.userMessageEntityId = userMessageEntityId;
        this.messageId = messageId;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public EArchiveBatchEntity geteArchiveBatch() {
        return eArchiveBatch;
    }

    public void seteArchiveBatch(EArchiveBatchEntity eArchiveBatch) {
        this.eArchiveBatch = eArchiveBatch;
    }

    public Long getUserMessageEntityId() {
        return userMessageEntityId;
    }

    public void setUserMessageEntityId(Long userMessageEntityId) {
        this.userMessageEntityId = userMessageEntityId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String userMessageId) {
        this.messageId = userMessageId;
    }

    @Override
    public String toString() {
        return "EArchiveBatchUserMessage{" +
                "entityId=" + entityId +
                ", userMessageEntityId=" + userMessageEntityId +
                ", messageId='" + messageId + '\'' +
                '}';
    }
}
