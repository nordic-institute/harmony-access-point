package eu.domibus.core.replication;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class UIReplicationEventListenerIntegrator implements Integrator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationEventListenerIntegrator.class);

    public static final UIReplicationEventListenerIntegrator INSTANCE =
            new UIReplicationEventListenerIntegrator();

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        final EventListenerRegistry eventListenerRegistry =
                serviceRegistry.getService(EventListenerRegistry.class);

//        eventListenerRegistry.appendListeners(
//                EventType.POST_INSERT,
//                ReplicationInsertEventListener.INSTANCE
//        );

        eventListenerRegistry.appendListeners(
                EventType.POST_UPDATE,
                UIReplicationUpdateEventListener.INSTANCE
        );
        LOG.debug("UIReplicationUpdateEventListener registered");

//        eventListenerRegistry.appendListeners(
//                EventType.PRE_DELETE,
//                ReplicationDeleteEventListener.INSTANCE
//        );
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

    }
}
