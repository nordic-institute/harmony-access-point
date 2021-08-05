package eu.domibus.core.converter;

import eu.domibus.api.user.User;
import eu.domibus.core.user.plugin.AuthenticationEntity;
import eu.domibus.core.user.ui.UserRole;
import eu.domibus.web.rest.ro.PluginUserRO;
import eu.domibus.web.rest.ro.UserResponseRO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface AuthCoreMapper {

    UserResponseRO userToUserResponseRO(User user);

    User userResponseROToUser(UserResponseRO user);

    @WithoutAuditAndEntityId
    @Mapping(ignore = true, target = "attemptCount")
    @Mapping(ignore = true, target = "suspensionDate")
    @Mapping(ignore = true, target = "passwordChangeDate")
    @Mapping(ignore = true, target = "defaultPassword")
    @Mapping(ignore = true, target = "version")
    default eu.domibus.core.user.ui.User userApiToUserSecurity(User user) {
        if (user == null) {
            return null;
        }

        eu.domibus.core.user.ui.User userUi = new eu.domibus.core.user.ui.User();

        userUi.setActive(user.isActive());
        userUi.setUserName(user.getUserName());
        userUi.setEmail(user.getEmail());
        userUi.setPassword(user.getPassword());
        userUi.setDeleted(user.isDeleted());

        return userUi;
    }

    @Mapping(source = "roles", target = "authorities")
    @Mapping(ignore = true, target = "status")
    @Mapping(ignore = true, target = "suspended")
    @Mapping(ignore = true, target = "domain")
    @Mapping(ignore = true, target = "expirationDate")
    User userSecurityToUserApi(eu.domibus.core.user.ui.User user);
    
    default String userRoleToAuthority(UserRole userRole) {
        return userRole.getName();
    }

    List<User> userResponseROListToUserList(List<UserResponseRO> userResponseROList);

    List<UserResponseRO> userListToUserResponseROList(List<User> userList);

    List<AuthenticationEntity> pluginUserROListToAuthenticationEntityList(List<PluginUserRO> pluginUserROList);

    @Mapping(ignore = true, target = "status")
    @Mapping(ignore = true, target = "domain")
    @Mapping(ignore = true, target = "expirationDate")
    PluginUserRO authenticationEntityToPluginUserRO(AuthenticationEntity authenticationEntity);

    @WithoutAudit
    @Mapping(ignore = true, target = "attemptCount")
    @Mapping(ignore = true, target = "suspensionDate")
    @Mapping(ignore = true, target = "passwordChangeDate")
    @Mapping(ignore = true, target = "defaultPassword")
    @Mapping(ignore = true, target = "backend")
    AuthenticationEntity pluginUserROToAuthenticationEntity(PluginUserRO pluginUserRO);

}
