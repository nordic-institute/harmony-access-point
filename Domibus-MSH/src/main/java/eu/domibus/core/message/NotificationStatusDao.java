package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.dao.ListDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.TypedQuery;

@Service
public class NotificationStatusDao extends BasicDao<NotificationStatusEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLog.class);

    public NotificationStatusDao() {
        super(NotificationStatusEntity.class);
    }

    public NotificationStatusEntity findByStatus(final NotificationStatus notificationStatus) {
        TypedQuery<NotificationStatusEntity> query = em.createNamedQuery("NotificationStatusEntity.findByStatus", NotificationStatusEntity.class);
        query.setParameter("NOTIFICATION_STATUS", notificationStatus);
        return query.getSingleResult();
    }
}
