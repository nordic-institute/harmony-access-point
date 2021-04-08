package eu.domibus.api.model;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_MESSAGE_PROPERTY")
@NamedQueries({
        @NamedQuery(name = "MessageProperty.findByValue", query = "select prop from MessageProperty prop where prop.value=:VALUE"),
        @NamedQuery(name = "MessageProperty.findByName", query = "select prop from PartProperty prop where prop.name=:NAME"),
})
public class MessageProperty extends Property {

}
