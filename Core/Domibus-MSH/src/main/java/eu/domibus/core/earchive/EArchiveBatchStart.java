package eu.domibus.core.earchive;

import eu.domibus.api.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * @author François Gautier
 * @since 5.0
 */
@Entity
@Table(name = "TB_EARCHIVE_START")
public class EArchiveBatchStart extends AbstractBaseEntity {

    @Column(name = "LAST_PK_USER_MESSAGE")
    private Long lastPkUserMessage;

    @Column(name = "DESCRIPTION")
    private String description;

    public Long getLastPkUserMessage() {
        return lastPkUserMessage;
    }

    public void setLastPkUserMessage(Long lastPkUserMessage) {
        this.lastPkUserMessage = lastPkUserMessage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "EArchiveBatchStart{" +
                "lastPkUserMessage=" + lastPkUserMessage +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }
}
