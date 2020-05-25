package eu.domibus.common.dao.security;

import eu.domibus.AbstractIT;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserRole;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 3.3
 */
public class UserDaoImplTestIT extends AbstractIT {
    @Autowired
    private UserDao userDao;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    public void getSuspendedUsers() throws Exception {
        UserEntityBase user = createUser("userOne");
        Date date = asDate(LocalDate.now().minusDays(2));
        user.setSuspensionDate(date);

        List<UserEntityBase> users = userDao.getSuspendedUsers(asDate(LocalDate.now().minusDays(1)));

        assertEquals(1, users.size());
        user = users.get(0);
//        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("test", user.getPassword());
        assertEquals(true, user.isActive());
    }

    @Test
    @Transactional
    @Rollback
    public void loadActiveUserByUsername() {
        createUser("userTwo");

        final User userOne = userDao.loadActiveUserByUsername("userTwo");

        assertNotNull(userOne);

        userDao.delete(Arrays.asList(userOne));
    }

    @Test
    @Transactional
    @Rollback
    public void loadUserByUsername() {
        createUser("user3");

        final User user = userDao.loadUserByUsername("user3");
        assertNotNull(user);

        final User user2 = userDao.loadUserByUsername("user33");
        assertNull(user2);
    }

    @Test
    @Transactional
    @Rollback
    public void delete() {
        createUser("user4");
        final List<User> users = Arrays.asList(createUser("user5"));
        userDao.delete(users);

        User user = userDao.loadUserByUsername("user4");
        assertNotNull(user);

        User user2 = userDao.loadUserByUsername("user5");
        assertNull(user2);
    }

    @Test
    @Transactional
    @Rollback
    public void findWithPasswordChangedBetween() throws Exception {
        User user1 = createUser("userPassChanged1");
        user1.setPasswordChangeDate(LocalDateTime.now().minusDays(5));

        User user2 = createUser("userPassChanged2");
        user2.setPasswordChangeDate(LocalDateTime.now().minusDays(2));

        List<UserEntityBase> users = userDao.findWithPasswordChangedBetween(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1), false);

        assertEquals(1, users.size());
        assertEquals("userPassChanged2", users.get(0).getUserName());
    }

    @Test
    @Transactional
    @Rollback
    public void update() throws Exception {
        User user = createUser("updateUser");
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("test", user.getPassword());
        assertEquals(true, user.isActive());

        user.setEmail("changed@gmail.com");
        user.setActive(false);

        final List<User> users = Arrays.asList(user);
        userDao.update(users);

        User sameUser = (User) userDao.findByUserName("updateUser");
        assertEquals("changed@gmail.com", sameUser.getEmail());
        assertEquals(false, sameUser.isActive());
    }

    @Test
    @Transactional
    @Rollback
    public void existsWithName() {
        String userName = "user1ForExistsTest";
        User user = createUser(userName);
        user.setDeleted(true);
        userDao.update(user);

        boolean res = userDao.existsWithId(userName);
        assertTrue(res);

        res = userDao.existsWithId(userName + "sss");
        assertFalse(res);

        userName = "user2ForExistsTest";
        user = createUser(userName);

        res = userDao.existsWithId(userName);
        assertTrue(res);
    }

    private User createUser(String name) {
        User user = new User();

        user.setUserName(name);
        user.setPassword("test");
        UserRole userRole = new UserRole("ROLE_USER_" + name);
        entityManager.persist(userRole);
        user.addRole(userRole);
        user.setEmail("test@gmail.com");
        user.setActive(true);
        userDao.create(user);
        return user;
    }

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}