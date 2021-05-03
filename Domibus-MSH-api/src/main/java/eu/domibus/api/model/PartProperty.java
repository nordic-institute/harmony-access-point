package eu.domibus.api.model;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_PART_PROPERTY")
@NamedQueries({
        @NamedQuery(name = "PartProperty.findPartProperties", query = "select props from UserMessage um left join um.messageProperties props where um.entityId= :ENTITY_ID"),
        @NamedQuery(name = "PartProperty.findByValue", query = "select prop from PartProperty prop where prop.value=:VALUE"),
        @NamedQuery(name = "PartProperty.findByNameValueAndType", query = "select prop from PartProperty prop where prop.name=:NAME and prop.value=:VALUE and (prop.type=:TYPE or prop.type is null)"),
})
public class PartProperty extends Property {


}
