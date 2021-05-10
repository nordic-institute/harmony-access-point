package eu.domibus.common.dao.security;

import eu.domibus.AbstractIT;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.user.ui.UserRole;
import eu.domibus.core.user.ui.UserRoleDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class UserRoleDaoImplTestIT extends AbstractIT {
    @Autowired
    private UserRoleDao userRoleDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    public void listRoles() {
        createUserRole("USER_ROLE_1");

        List<UserRole> roles = userRoleDao.listRoles();

        assertEquals(1, roles.size());
        assertEquals(1, roles.size());
        UserRole userRole = roles.get(0);
        assertEquals("USER_ROLE_1", userRole.getName());
        assertNotNull(userRole.getCreationTime());
        assertNotNull(userRole.getModificationTime());
        assertEquals(userRole.getCreationTime(), userRole.getModificationTime());
        assertNotNull(userRole.getCreatedBy());
        assertNotNull(userRole.getModifiedBy());
    }

    @Test
    @Transactional
    @Rollback
    public void findByName() {
        createUserRole("USER_ROLE_2");

        UserRole found = userRoleDao.findByName("USER_ROLE_2");

        assertNotNull(found);
        assertEquals("USER_ROLE_2", found.getName());
    }

    private void createUserRole(String name) {
        UserRole userRole = new UserRole(name);
        entityManager.persist(userRole);
    }

}