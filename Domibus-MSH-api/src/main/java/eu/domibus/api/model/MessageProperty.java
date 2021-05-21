package eu.domibus.api.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_MESSAGE_PROPERTY")
@NamedQuery(name = "MessageProperty.findByNameValueAndType", hints = {
        @QueryHint(name = "org.hibernate.cacheRegion", value = "dictionary-queries"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")}, query = "select prop from MessageProperty prop where prop.name=:NAME and prop.value=:VALUE and (prop.type=:TYPE or prop.type is null)")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MessageProperty extends Property {

}
