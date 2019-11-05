package eu.domibus.core.replication;

import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.FlushMode;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

public class UIReplicationUpdateEventListener implements PostUpdateEventListener {

    public static final UIReplicationUpdateEventListener INSTANCE =
            new UIReplicationUpdateEventListener();

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationUpdateEventListener.class);


    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        final Object entity = postUpdateEvent.getEntity();

        if (entity instanceof UserMessageLog) {
            UserMessageLog userMessageLog = (UserMessageLog) entity;
            final String messageId = userMessageLog.getMessageInfo().getMessageId();
            LOG.debug("update intercepted for userMessageLog=[{}]", messageId);

            postUpdateEvent.getSession().createNativeQuery(
                    "UPDATE TB_MESSAGE_UI " +
                            "SET message_status = :message_status " +
                            "WHERE message_id = :message_id")
                    .setParameter("message_id", messageId)
                    .setParameter("message_status", userMessageLog.getMessageStatus().name())
                    .setFlushMode(FlushMode.MANUAL)
                    .executeUpdate();
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return false;
    }
}
