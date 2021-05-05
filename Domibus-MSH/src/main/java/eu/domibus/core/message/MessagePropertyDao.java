package eu.domibus.core.message;

import eu.domibus.api.model.MessageProperty;
import eu.domibus.core.dao.BasicDao;
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

    public MessageProperty findPropertyByNameValueAndType(final String name, String value, String type) {
        final TypedQuery<MessageProperty> query = this.em.createNamedQuery("MessageProperty.findByNameValueAndType", MessageProperty.class);
        query.setParameter("NAME", name);
        query.setParameter("VALUE", value);
        query.setParameter("TYPE", type);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    @Transactional
    public MessageProperty findOrCreateProperty(final String name, String value, String type) {
        MessageProperty property = findPropertyByNameValueAndType(name, value, type);
        if (property != null) {
            return property;
        }
        MessageProperty newProperty = new MessageProperty();
        newProperty.setName(name);
        newProperty.setValue(value);
        newProperty.setType(type);
        create(newProperty);
        return newProperty;
    }
}
