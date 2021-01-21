package eu.domibus.core.message;

import eu.domibus.api.model.MessagePropertiesDto;
import eu.domibus.api.model.Property;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

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

    public MessagePropertiesDto findMessagePropertiesForMessageId(final String messageId) {
        final TypedQuery<MessagePropertiesDto> query = this.em.createNamedQuery("Property.findPropertiesByMessageId", MessagePropertiesDto.class);
        query.setParameter(MSG_ID, messageId);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
