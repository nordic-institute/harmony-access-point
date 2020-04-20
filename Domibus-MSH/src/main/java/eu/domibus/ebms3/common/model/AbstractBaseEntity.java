package eu.domibus.ebms3.common.model;

import eu.domibus.core.spring.SpringContextProvider;
import eu.domibus.core.util.DatabaseUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.common.util.StringUtils;

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

    @Id
    @XmlTransient
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PK")
    private long entityId;

    @Column(name = "CREATION_TIME", updatable = false, nullable = false)
    @XmlTransient
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "MODIFICATION_TIME")
    @XmlTransient
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @Column(name = "CREATED_BY", nullable = false, updatable = false)
    @XmlTransient
    private String createdBy;

    @Column(name = "MODIFIED_BY")
    @XmlTransient
    private String modifiedBy;

    @PrePersist
    public void updateCreationDetails() {
        String user = LOG.getMDC(DomibusLogger.MDC_USER);
        if (StringUtils.isEmpty(user)) {
            user = getDataBaseUser();
        }
        setCreatedBy(user);
        setCreationTime(Calendar.getInstance().getTime());
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
