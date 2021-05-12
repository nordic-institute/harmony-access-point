package eu.domibus.core.audit;

import eu.domibus.AbstractCoreIT;
import eu.domibus.core.user.ui.User;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class AuditIT extends AbstractCoreIT {

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
