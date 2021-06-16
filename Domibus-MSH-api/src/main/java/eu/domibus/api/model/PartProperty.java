package eu.domibus.api.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_PART_PROPERTY")
@NamedQueries({
        @NamedQuery(name = "PartProperty.findByValue", hints = {
                @QueryHint(name = "org.hibernate.cacheRegion", value = "dictionary-queries"),
                @QueryHint(name = "org.hibernate.cacheable", value = "true")}, query = "select prop from PartProperty prop where prop.value=:VALUE"),
        @NamedQuery(name = "PartProperty.findByNameValueAndType", hints = {
                @QueryHint(name = "org.hibernate.cacheRegion", value = "dictionary-queries"),
                @QueryHint(name = "org.hibernate.cacheable", value = "true")}, query = "select prop from PartProperty prop where prop.name=:NAME and prop.value=:VALUE and prop.type=:TYPE"),
        @NamedQuery(name = "PartProperty.findByNameAndValue", hints = {
                @QueryHint(name = "org.hibernate.cacheRegion", value = "dictionary-queries"),
                @QueryHint(name = "org.hibernate.cacheable", value = "true")}, query = "select prop from PartProperty prop where prop.name=:NAME and prop.value=:VALUE and prop.type is null"),
})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PartProperty extends Property {


}
