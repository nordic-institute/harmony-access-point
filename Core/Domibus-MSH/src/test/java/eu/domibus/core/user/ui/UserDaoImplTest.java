package eu.domibus.core.user.ui;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class UserDaoImplTest {
    @Tested
    UserDaoImpl userDao;

    @Injectable
    EntityManager entityManager;

    @Injectable
    TypedQuery<User> namedQuery;

    @Injectable
    User user;

    @Test
    public void listUsers() {
        new Expectations() {{
            entityManager.createNamedQuery("User.findAll", User.class);
            result = namedQuery;
        }};

        userDao.listUsers();

        new Verifications() {{
            namedQuery.getResultList();
            times = 1;
        }};
    }

    @Test
    public void getSuspendedUsers() {
        final Date currentTimeMinusSuspensionInterval = new Date(2323223232L);

        new Expectations() {{
            entityManager.createNamedQuery("User.findSuspendedUsers", User.class);
            result = namedQuery;
        }};

        userDao.getSuspendedUsers(currentTimeMinusSuspensionInterval);

        new Verifications() {{
            namedQuery.setParameter("SUSPENSION_INTERVAL", currentTimeMinusSuspensionInterval);
            times = 1;

            namedQuery.getResultList().stream().collect(Collectors.toList());
            times = 1;
        }};
    }

    @Test
    public void loadUserByUsername() {
        final String userName = "admin";

        new Expectations() {{
            entityManager.createNamedQuery("User.findByUserName", User.class);
            result = namedQuery;
        }};

        userDao.loadUserByUsername(userName);

        new Verifications() {{
            namedQuery.setParameter(User.USER_NAME, userName);

            namedQuery.getSingleResult();
            times = 1;
        }};
    }

    @Test
    public void loadActiveUserByUsername() {
        final String userName = "admin";

        new Expectations() {{
            entityManager.createNamedQuery("User.findActiveByUserName", User.class);
            result = namedQuery;
        }};

        userDao.loadActiveUserByUsername(userName);

        new Verifications() {{
            namedQuery.setParameter(User.USER_NAME, userName);

            namedQuery.getSingleResult();
            times = 1;
        }};
    }

    @Test
    public void update() {
        List<User> users = new ArrayList<>();
        users.add(user);

        userDao.update(users);

        new Verifications() {{
            userDao.update(user);
            times = 1;
        }};
    }

    @Test
    public void delete() {
        List<User> users = new ArrayList<>();
        users.add(user);

        userDao.delete(users);

        new Verifications() {{
            userDao.update(user);
            times = 1;
        }};
    }


    @Test
    public void findByUserName() {
        final String userName = "admin";

        userDao.findByUserName(userName);

        new Verifications() {{
            userDao.loadUserByUsername(userName);
            times = 1;
        }};
    }

    @Test
    public void findWithPasswordChangedBetween(@Injectable LocalDate start, @Injectable LocalDate end, @Injectable TypedQuery<String> query) {
        final boolean withDefaultPassword = true;

        new Expectations() {{
            entityManager.createNamedQuery("User.findWithPasswordChangedBetween", User.class);
            result = query;
        }};

        userDao.findWithPasswordChangedBetween(start, end, withDefaultPassword);

        new Verifications() {{
            query.setParameter("START_DATE", start.atStartOfDay());
            times = 1;

            query.setParameter("END_DATE", end.atStartOfDay());
            times = 1;

            query.setParameter("DEFAULT_PASSWORD", withDefaultPassword);
            times = 1;

            query.getResultList().stream().collect(Collectors.toList());
            times = 1;
        }};
    }

    @Test
    public void updateUser() {
        final boolean flush = true;

        userDao.update(user, flush);

        new Verifications() {{
            userDao.update(user);
            times = 1;
            userDao.flush();
            times = 1;
        }};
    }

    @Test
    public void findByRole() {
        final String username = "admin";

        new Expectations() {{
            entityManager.createNamedQuery("User.findByRoleName", User.class);
            result = namedQuery;
        }};

        userDao.findByRole(username);

        new Verifications() {{
            namedQuery.getResultList().stream().collect(Collectors.toList());
            times = 1;
        }};
    }


    @Test
    public void existsWithId() {
        final String userID = "admin";

        new Expectations() {{
            entityManager.createNamedQuery("User.findAllByUserName", User.class);
            result = namedQuery;
        }};

        userDao.existsWithId(userID);

        new Verifications() {{
            namedQuery.getResultList().size();
            times = 1;
        }};
    }
}