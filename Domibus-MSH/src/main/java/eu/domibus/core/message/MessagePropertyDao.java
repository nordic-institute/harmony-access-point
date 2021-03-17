package eu.domibus.core.message;

import eu.domibus.api.model.MessageProperty;
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
public class MessagePropertyDao extends BasicDao<MessageProperty> {

    public MessagePropertyDao() {
        super(MessageProperty.class);
    }

    public List<MessageProperty> findMessageProperties(final Long messageId) {
        final Query query = this.em.createNamedQuery("MessageProperty.findMessageProperties");
        query.setParameter("ENTITY_ID", messageId);
        return query.getResultList();
    }
}
