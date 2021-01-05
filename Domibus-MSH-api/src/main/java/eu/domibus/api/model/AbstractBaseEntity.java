package eu.domibus.api.model;

import eu.domibus.api.spring.SpringContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Base type for entity
 *
 * For convenience we are using the same base entity as domibus core
 */
@XmlTransient
@MappedSuperclass
public abstract class AbstractBaseEntity implements Serializable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractBaseEntity.class);

    @XmlTransient
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PK")
    private long entityId;

    @XmlTransient
    @Column(name = "CREATION_TIME", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @XmlTransient
    @Column(name = "MODIFICATION_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @XmlTransient
    @Column(name = "CREATED_BY", nullable = false, updatable = false)
    private String createdBy;

    @XmlTransient
    @Column(name = "MODIFIED_BY")
    private String modifiedBy;

    @PrePersist
    public void updateCreationDetails() {
        String user = LOG.getMDC(DomibusLogger.MDC_USER);
        if (StringUtils.isEmpty(user)) {
            user = getDataBaseUser();
        }
        Date time = Calendar.getInstance().getTime();
        setCreatedBy(user);
        setCreationTime(time);

        setModifiedBy(user);
        setModificationTime(time);
    }

    protected String getDataBaseUser() {
        final DatabaseUtil databaseUtil = SpringContextProvider.getApplicationContext().getBean(DatabaseUtil.DATABASE_USER, DatabaseUtil.class);
        LOG.trace("DataBase UserName: [{}]", databaseUtil.getDatabaseUserName());
        return databaseUtil.getDatabaseUserName();
    }

    @PreUpdate
    public void updateModificationDetails() {
        String user = LOG.getMDC(DomibusLogger.MDC_USER);
        if (StringUtils.isEmpty(user)) {
            user = getDataBaseUser();
        }
        setModifiedBy(user);
        setModificationTime(Calendar.getInstance().getTime());
    }

    /**
     * @return the primary key of the entity
     */
    public long getEntityId() {
        return this.entityId;
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

    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(final Object other) {
        //noinspection NonFinalFieldReferenceInEquals
        return ((other != null) &&
                this.getClass().equals(other.getClass())
        );
    }
}
