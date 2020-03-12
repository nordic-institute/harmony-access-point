package eu.domibus.core.user;

import eu.domibus.common.model.security.UserRole;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface UserRoleDao {

    List<UserRole> listRoles();

    UserRole findByName(final String roleName);
}
