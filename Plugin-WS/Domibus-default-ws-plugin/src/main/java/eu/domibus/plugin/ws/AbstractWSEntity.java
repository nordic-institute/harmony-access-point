package eu.domibus.plugin.ws;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

import static eu.domibus.common.JPAConstants.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@MappedSuperclass
public class AbstractWSEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator =  DOMIBUS_SCALABLE_SEQUENCE)
    @GenericGenerator(
            name = DOMIBUS_SCALABLE_SEQUENCE,
            strategy = DATE_PREFIXED_SEQUENCE_ID_GENERATOR)
    @Column(name = "ID_PK")
    private long entityId;

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }
}
