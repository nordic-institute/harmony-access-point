package eu.domibus.plugin.ws;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

import static eu.domibus.orm.HibernateConstants.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@MappedSuperclass
public class AbstractWSEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =  DOMIBUS_SCALABLE_SEQUENCE)
    @GenericGenerator(
            name = DOMIBUS_SCALABLE_SEQUENCE,
            strategy = DATE_PREFIXED_SEQUENCE_ID_GENERATOR,
            parameters = {@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = INCREMENT_SIZE)})
    @Column(name = "ID_PK")
    private long entityId;

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }
}
