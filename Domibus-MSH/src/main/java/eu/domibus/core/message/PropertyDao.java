package eu.domibus.core.message;

import eu.domibus.api.model.Property;
import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class PropertyDao extends BasicDao<Property> {

    public static final String MSG_ID = "MSG_ID";

    public PropertyDao() {
        super(Property.class);
    }

    public List<Property> findMessagePropertiesForMessageId(final String messageId) {
        final Query query = this.em.createNamedQuery("Property.findPropertiesByMessageId");
        query.setParameter(MSG_ID, messageId);
        return query.getResultList();
    }
}
