package eu.domibus.core.earchive.job;

import eu.domibus.api.model.DomibusBaseEntity;
import eu.domibus.common.JPAConstants;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author François Gautier
 * @since 5.0
 */
@Entity
@Table(name = "TB_EARCHIVEBATCH_UM")
public class EArchiveBatchUserMessage implements DomibusBaseEntity {

    @XmlTransient
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JPAConstants.DOMIBUS_SCALABLE_SEQUENCE)
    @GenericGenerator(
            name = JPAConstants.DOMIBUS_SCALABLE_SEQUENCE,
            strategy = "eu.domibus.api.model.DatePrefixedGenericSequenceIdGenerator")
    @Column(name = "ID_PK")
    private long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_EARCHIVE_BATCH_ID")
    private EArchiveBatch eArchiveBatch;

    @Column(name = "FK_USER_MESSAGE_ID")
    private Long userMessageEntityId;

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public EArchiveBatch geteArchiveBatch() {
        return eArchiveBatch;
    }

    public void seteArchiveBatch(EArchiveBatch eArchiveBatch) {
        this.eArchiveBatch = eArchiveBatch;
    }

    public Long getUserMessageEntityId() {
        return userMessageEntityId;
    }

    public void setUserMessageEntityId(Long ownerId) {
        this.userMessageEntityId = ownerId;
    }
}
