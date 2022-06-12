package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.PartProperty;
import eu.domibus.core.dao.BasicDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class PartPropertyDao extends BasicDao<PartProperty> {

    public PartPropertyDao() {
        super(PartProperty.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PartProperty findOrCreateProperty(final String name, String value, String type) {
        if(StringUtils.isBlank(value)) {
            return null;
        }

        PartProperty property = findExistingProperty(name, value, type);
        if (property != null) {
            return property;
        }
        PartProperty newProperty = new PartProperty();
        newProperty.setName(name);
        newProperty.setValue(value);
        newProperty.setType(StringUtils.isNotBlank(type) ? type : null);
        create(newProperty);
        return newProperty;
    }

    protected PartProperty findExistingProperty(final String name, String value, String type) {
        if(StringUtils.isNotBlank(type)) {
            return findPropertyByNameValueAndType(name, value, type);
        }
        return findPropertyByNameValue(name, value);
    }

    protected PartProperty findPropertyByNameValueAndType(final String name, String value, String type) {
        final TypedQuery<PartProperty> query = this.em.createNamedQuery("PartProperty.findByNameValueAndType", PartProperty.class);
        query.setParameter("NAME", name);
        query.setParameter("VALUE", value);
        query.setParameter("TYPE", type);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    protected PartProperty findPropertyByNameValue(final String name, String value) {
        final TypedQuery<PartProperty> query = this.em.createNamedQuery("PartProperty.findByNameAndValue", PartProperty.class);
        query.setParameter("NAME", name);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
