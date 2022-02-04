package eu.domibus.core.audit;

import eu.domibus.AbstractIT;
import eu.domibus.core.user.ui.User;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Ignore("EDELIVERY-8927: Bamboo - Sonar Branch plan is failing due to IT test failures")
public class AuditIT extends AbstractIT {

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
