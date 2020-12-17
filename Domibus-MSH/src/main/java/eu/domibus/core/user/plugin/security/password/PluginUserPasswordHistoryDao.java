package eu.domibus.core.user.plugin.security.password;

import eu.domibus.core.user.UserPasswordHistoryDao;
import eu.domibus.core.user.plugin.AuthenticationEntity;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface PluginUserPasswordHistoryDao extends UserPasswordHistoryDao<AuthenticationEntity> {
}
