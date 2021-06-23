package eu.domibus.core.message;

import eu.domibus.api.model.MessageProperty;
import eu.domibus.core.dao.BasicDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class MessagePropertyDao extends BasicDao<MessageProperty> {

    public MessagePropertyDao() {
        super(MessageProperty.class);
    }

    @Transactional
    public MessageProperty findOrCreateProperty(final String name, String value, String type) {
        MessageProperty property = findExistingProperty(name, value, type);
        if (property != null) {
            return property;
        }
        MessageProperty newProperty = new MessageProperty();
        newProperty.setName(name);
        newProperty.setValue(value);
        newProperty.setType(StringUtils.isNotBlank(type) ? type : null);
        create(newProperty);
        return newProperty;
    }

    protected MessageProperty findPropertyByNameValueAndType(final String name, String value, String type) {
        final TypedQuery<MessageProperty> query = this.em.createNamedQuery("MessageProperty.findByNameValueAndType", MessageProperty.class);
        query.setParameter("NAME", name);
        query.setParameter("VALUE", value);
        query.setParameter("TYPE", type);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    protected MessageProperty findExistingProperty(final String name, String value, String type) {
        if (StringUtils.isNotBlank(type)) {
            return findPropertyByNameValueAndType(name, value, type);
        }
        return findPropertyByNameValue(name, value);
    }

    protected MessageProperty findPropertyByNameValue(final String name, String value) {
        final TypedQuery<MessageProperty> query = this.em.createNamedQuery("MessageProperty.findByNameAndValue", MessageProperty.class);
        query.setParameter("NAME", name);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }

}
