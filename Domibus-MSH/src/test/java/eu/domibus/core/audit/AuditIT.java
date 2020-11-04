package eu.domibus.core.audit;

import eu.domibus.core.user.ui.User;
import eu.domibus.core.dao.InMemoryDatabaseMshConfig;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDatabaseMshConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class AuditIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuditIT.class);

    @PersistenceContext
    private javax.persistence.EntityManager em;

    @Before
    public void setup() {
        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }

    //just inserting audited entity to verify that envers does not cause any problems.
    @Test
    @Transactional
    public void testSaveEntity() {
        User user = new User();
        user.setUserName("Test33");
        user.setEmail("dussart.thomas@gmail.com");
        user.setPassword("test");
        user.setActive(true);
        em.persist(user);
        //TODO add the other entities here.(Configuration/Message filter)
    }

}
