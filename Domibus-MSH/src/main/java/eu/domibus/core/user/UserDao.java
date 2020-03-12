package eu.domibus.core.user;

import java.util.Collection;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserDao extends UserDaoBase<User> {
    List<User> listUsers();

    void create(final User user);

    User loadUserByUsername(String userName);

    User loadActiveUserByUsername(String userName);

    void update(final User entity);

    void delete(final Collection<User> delete);

    void flush();
}
