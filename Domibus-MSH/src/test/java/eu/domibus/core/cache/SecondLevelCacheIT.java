package eu.domibus.core.cache;

import eu.domibus.api.model.UserMessage;
import eu.domibus.core.dao.InMemoryDatabaseMshConfig;
import eu.domibus.core.user.ui.User;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDatabaseMshConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class SecondLevelCacheIT {
    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecondLevelCacheIT.class);

    @PersistenceContext(unitName = "domibusEM")
    protected EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void secondLevelCache_SimpleEntityCached() {
        //////////////////////////////Init Session Begins//////////////////////////////
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        initRow(sessionFactory, "insert into TB_USER (ID_PK,USER_EMAIL,USER_ENABLED,USER_PASSWORD,USER_NAME,OPTLOCK,ATTEMPT_COUNT,SUSPENSION_DATE,USER_DELETED,PASSWORD_CHANGE_DATE,DEFAULT_PASSWORD,CREATION_TIME,CREATED_BY,MODIFICATION_TIME,MODIFIED_BY) " +
                "select 1, null,'1','$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36','user1',null,'0',null,'0',to_timestamp('16/03/21 10:19:28,000000000','DD/MM/RR HH24:MI:SSXFF'),'1',to_timestamp('16/03/21 10:19:28,000000000','DD/MM/RR HH24:MI:SSXFF'),'GREEN_500',null,null;");
        sessionFactory.getStatistics().logSummary();
        LOG.info(sessionFactory.getStatistics().getSecondLevelCacheStatistics("eu.domibus.core.user.ui.User").toString());
        //////////////////////////////Init Session Ends//////////////////////////////

        //////////////////////////////First Session Begins//////////////////////////////
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        Query query = session.createNamedQuery("User.findAll");
        @SuppressWarnings("unchecked")
        List<User> users = query.getResultList();
        session.getTransaction().commit();
        sessionFactory.getStatistics().logSummary();
        LOG.info(sessionFactory.getStatistics().getSecondLevelCacheStatistics("eu.domibus.core.user.ui.User").toString());
        //////////////////////////////First Session Ends//////////////////////////////


        //////////////////////////////Second Session Begins//////////////////////////////
        Session sessionNew = sessionFactory.getCurrentSession();
        sessionNew.beginTransaction();
        Query anotherQuery = sessionNew.createNamedQuery("User.findAll");
        @SuppressWarnings("unchecked")
        List<User> usersFromCache = anotherQuery.getResultList();
        sessionNew.getTransaction().commit();
        sessionFactory.getStatistics().logSummary();
        LOG.info(sessionFactory.getStatistics().getSecondLevelCacheStatistics("eu.domibus.core.user.ui.User").toString());
        //////////////////////////////Second Session Ends//////////////////////////////

        log2ndLevelCache(sessionFactory);

    }

    private void log2ndLevelCache(SessionFactory sessionFactory) {
        LOG.info(
                "\n2LC put [{}] \n" +
                        "2LC hit [{}] \n" +
                        "2LC mis [{}] \n"
                , sessionFactory.getStatistics().getSecondLevelCachePutCount()
                , sessionFactory.getStatistics().getSecondLevelCacheHitCount()
                , sessionFactory.getStatistics().getSecondLevelCacheMissCount());
        sessionFactory.getStatistics().logSummary();
        sessionFactory.getStatistics().clear();
    }

    /**
     * UserMessage is not cached
     * MessageInfo is cached
     *
     * The query gets findAll without 2nd level caching and when lazy loading MessageInfo is querying the database producing 1 miss in 2nd level caching and 1 put.
     * Second call of findAll query the database but the lazy loading of message info is retrieving the MessageInfo in the 2nd level caching.
     */
    @Test
    public void secondLevelCacheUserMessage_nestedEntityCached() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        //////////////////////////////Init Session Begins//////////////////////////////
        initRow(sessionFactory, "Insert into TB_MESSAGE_INFO (ID_PK,MESSAGE_ID,REF_TO_MESSAGE_ID,TIME_STAMP,CREATION_TIME,CREATED_BY,MODIFICATION_TIME,MODIFIED_BY) values ('93','12H35m54031_BlueToRed',null,to_timestamp('16/03/21 12:35:57,955000000','DD/MM/RR HH24:MI:SSXFF'),to_timestamp('16/03/21 12:35:58,282000000','DD/MM/RR HH24:MI:SSXFF'),'GREEN_500',to_timestamp('16/03/21 12:35:58,282000000','DD/MM/RR HH24:MI:SSXFF'),'GREEN_500');\n");
        initRow(sessionFactory, "Insert into TB_MESSAGE_INFO (ID_PK,MESSAGE_ID,REF_TO_MESSAGE_ID,TIME_STAMP,CREATION_TIME,CREATED_BY,MODIFICATION_TIME,MODIFIED_BY)" +
                "values ('428','ab8d8e4b-8d6b-11eb-b109-9c5c8ec0f1ad@domibus.eu','14H11m49857_BlueToRed_1',to_timestamp('25/03/21 14:11:54,000000000','DD/MM/RR HH24:MI:SSXFF'),to_timestamp('25/03/21 14:11:55,385000000','DD/MM/RR HH24:MI:SSXFF'),'GREEN_500',to_timestamp('25/03/21 14:11:55,385000000','DD/MM/RR HH24:MI:SSXFF'),'GREEN_500');");
        initRow(sessionFactory, "Insert into TB_USER_MESSAGE (ID_PK,COLLABORATION_INFO_ACTION,AGREEMENT_REF_PMODE,AGREEMENT_REF_TYPE,AGREEMENT_REF_VALUE,COLL_INFO_CONVERS_ID,SERVICE_TYPE,SERVICE_VALUE,MPC,FROM_ROLE,TO_ROLE,MESSAGEINFO_ID_PK,FK_MESSAGE_FRAGMENT_ID,SPLIT_AND_JOIN,CREATION_TIME,CREATED_BY,MODIFICATION_TIME,MODIFIED_BY) " +
                "values ('92','TC1Leg1',null,null,null,'c6bb134a-864b-11eb-900e-9c5c8ec0f1ad@domibus.eu','tc1','bdx:noprocess','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder','93',null,'1',to_timestamp('16/03/21 12:35:58,279000000','DD/MM/RR HH24:MI:SSXFF'),'GREEN_500',to_timestamp('16/03/21 12:35:58,279000000','DD/MM/RR HH24:MI:SSXFF'),'GREEN_500');");
        //////////////////////////////Init Session Ends//////////////////////////////

        //////////////////////////////First Session Begins//////////////////////////////
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        Query query = session.createNamedQuery("UserMessage.findAll");//.setCacheable(true);
        @SuppressWarnings("unchecked")
        List<UserMessage> userMessages = query.getResultList();
        session.getTransaction().commit();
        sessionFactory.getStatistics().logSummary();
        LOG.info(sessionFactory.getStatistics().getSecondLevelCacheStatistics("eu.domibus.api.model.UserMessage").toString());
        //////////////////////////////First Session Ends//////////////////////////////
        sessionFactory.getStatistics().logSummary();

        //////////////////////////////Second Session Begins//////////////////////////////
        Session sessionNew = sessionFactory.getCurrentSession();
        sessionNew.beginTransaction();
        Query anotherQuery = sessionNew.createNamedQuery("UserMessage.findAll");//.setCacheable(true);
        @SuppressWarnings("unchecked")
        List<UserMessage> userMessagesFromCache = anotherQuery.getResultList();
        sessionNew.getTransaction().commit();
        sessionFactory.getStatistics().logSummary();
        LOG.info(sessionFactory.getStatistics().getSecondLevelCacheStatistics("eu.domibus.api.model.UserMessage").toString());
        //////////////////////////////Second Session Ends//////////////////////////////
        log2ndLevelCache(sessionFactory);
    }

    private void initRow(SessionFactory sessionFactory, String s) {
        Session init = sessionFactory.getCurrentSession();
        init.beginTransaction();
        Query insert = init.createNativeQuery(s);
        insert.executeUpdate();
        init.getTransaction().commit();
    }

}
