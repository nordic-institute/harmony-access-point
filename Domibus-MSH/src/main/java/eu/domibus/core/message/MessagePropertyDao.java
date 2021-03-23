package eu.domibus.core.message;

import eu.domibus.api.model.MessageProperty;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.Property;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class MessagePropertyDao extends BasicDao<MessageProperty> {

    public MessagePropertyDao() {
        super(MessageProperty.class);
    }

    public List<MessageProperty> findMessageProperties(final Long userMessageEntityId) {
        final Query query = this.em.createNamedQuery("MessageProperty.findMessageProperties");
        query.setParameter("ENTITY_ID", userMessageEntityId);
        return query.getResultList();
    }

    public MessageProperty findPropertyByValue(final String value) {
        final TypedQuery<MessageProperty> query = this.em.createNamedQuery("MessageProperty.findByValue", MessageProperty.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public MessageProperty findPropertyByName(final String value) {
        final TypedQuery<MessageProperty> query = this.em.createNamedQuery("MessageProperty.findByName", MessageProperty.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public MessageProperty findOrCreateProperty(final String name, String value, String type) {
        MessageProperty property = findPropertyByName(name);
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
