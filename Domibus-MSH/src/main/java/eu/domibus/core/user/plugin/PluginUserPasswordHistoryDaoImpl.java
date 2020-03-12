package eu.domibus.core.user.plugin;

import eu.domibus.core.user.UserPasswordHistoryDaoImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Repository
public class PluginUserPasswordHistoryDaoImpl extends UserPasswordHistoryDaoImpl<AuthenticationEntity, PluginUserPasswordHistory>
        implements PluginUserPasswordHistoryDao {

    public PluginUserPasswordHistoryDaoImpl() {
        super(PluginUserPasswordHistory.class, "PluginUserPasswordHistory.findPasswords");
    }

    @Override
    protected PluginUserPasswordHistory createNew(final AuthenticationEntity user, String passwordHash, LocalDateTime passwordDate) {
        return new PluginUserPasswordHistory(user, passwordHash, passwordDate);
    }

}


