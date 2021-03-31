package eu.domibus.api.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractNoGeneratedPkEntity implements DomibusBaseEntity {

    @Id
    @Column(name = "ID_PK")
    private long entityId;

    @Override
    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }
}
