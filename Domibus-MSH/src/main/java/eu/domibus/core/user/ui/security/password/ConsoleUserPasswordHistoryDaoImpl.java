package eu.domibus.core.user.ui.security.password;

import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.UserPasswordHistoryDaoImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Repository
public class ConsoleUserPasswordHistoryDaoImpl extends UserPasswordHistoryDaoImpl<User, ConsoleUserPasswordHistory>
        implements ConsoleUserPasswordHistoryDao {

    public ConsoleUserPasswordHistoryDaoImpl() {
        super(ConsoleUserPasswordHistory.class, "ConsoleUserPasswordHistory.findPasswords");
    }

    @Override
    protected ConsoleUserPasswordHistory createNew(final User user, String passwordHash, LocalDateTime passwordDate) {
        return new ConsoleUserPasswordHistory(user, passwordHash, passwordDate);
    }

}

