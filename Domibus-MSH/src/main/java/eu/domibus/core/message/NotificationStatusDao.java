package eu.domibus.core.message;

import eu.domibus.api.model.NotificationStatus;
import eu.domibus.api.model.NotificationStatusEntity;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Service;

import javax.persistence.TypedQuery;

@Service
public class NotificationStatusDao extends BasicDao<NotificationStatusEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLog.class);

    public NotificationStatusDao() {
        super(NotificationStatusEntity.class);
    }

    public NotificationStatusEntity findOrCreate(NotificationStatus status) {
        NotificationStatusEntity messageStatusEntity = findByStatus(status);
        if (messageStatusEntity != null) {
            return messageStatusEntity;
        }
        NotificationStatusEntity entity = new NotificationStatusEntity();
        entity.setStatus(status);
        create(entity);
        return entity;
    }

    public NotificationStatusEntity findByStatus(final NotificationStatus notificationStatus) {
        TypedQuery<NotificationStatusEntity> query = em.createNamedQuery("NotificationStatusEntity.findByStatus", NotificationStatusEntity.class);
        query.setParameter("NOTIFICATION_STATUS", notificationStatus);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
