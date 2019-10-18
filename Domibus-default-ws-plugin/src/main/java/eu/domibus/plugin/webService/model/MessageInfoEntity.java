package eu.domibus.plugin.webService.model;

import javax.persistence.*;

@Entity
@Table(name = "WS_PLUGIN_TB_MESSAGE")
public class MessageInfoEntity {

    @Id
    @Column(name = "ID_PK")
    private long entityId;

    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * @return the primary key of the entity
     */
    public long getEntityId() {
        return this.entityId;
    }


    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public MessageInfoEntity(final String description){
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

