package eu.domibus.common.dao.security;

import eu.domibus.AbstractIT;
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

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    public void listRoles() {
        createUserRole("USER_ROLE_1");

        List<UserRole> roles = userRoleDao.listRoles();

        assertEquals(1, roles.size());
        assertEquals(1, roles.size());
        assertEquals("USER_ROLE_1", roles.get(0).getName());
        assertNotNull(roles.get(0).getCreationTime());
        assertNotNull(roles.get(0).getModificationTime());
        assertNotNull(roles.get(0).getCreatedBy());
        assertNotNull(roles.get(0).getModifiedBy());
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