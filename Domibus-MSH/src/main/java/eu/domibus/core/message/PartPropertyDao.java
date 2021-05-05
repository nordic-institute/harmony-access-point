package eu.domibus.core.message;

import eu.domibus.api.model.PartProperty;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class PartPropertyDao extends BasicDao<PartProperty> {

    public PartPropertyDao() {
        super(PartProperty.class);
    }

    public PartProperty findPropertyByValue(final String value) {
        final TypedQuery<PartProperty> query = this.em.createNamedQuery("PartProperty.findByValue", PartProperty.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public PartProperty findPropertyByNameValueAndType(final String name, String value, String type) {
        final TypedQuery<PartProperty> query = this.em.createNamedQuery("PartProperty.findByNameValueAndType", PartProperty.class);
        query.setParameter("NAME", name);
        query.setParameter("VALUE", value);
        query.setParameter("TYPE", type);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    @Transactional
    public PartProperty findOrCreateProperty(final String name, String value, String type) {
        PartProperty property = findPropertyByNameValueAndType(name, value, type);
        if (property != null) {
            return property;
        }
        PartProperty newProperty = new PartProperty();
        newProperty.setName(name);
        newProperty.setValue(value);
        newProperty.setType(type);
        create(newProperty);
        return newProperty;
    }
}
