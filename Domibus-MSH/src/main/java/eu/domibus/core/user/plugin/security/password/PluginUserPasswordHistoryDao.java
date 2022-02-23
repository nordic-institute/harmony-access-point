package eu.domibus.core.user.plugin.security.password;

import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.core.user.UserPasswordHistoryDao;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface PluginUserPasswordHistoryDao extends UserPasswordHistoryDao<AuthenticationEntity> {
}
