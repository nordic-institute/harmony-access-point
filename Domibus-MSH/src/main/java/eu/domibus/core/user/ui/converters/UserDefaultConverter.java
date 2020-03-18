package eu.domibus.core.user.ui.converters;

import eu.domibus.api.user.User;
import eu.domibus.api.user.UserState;
import eu.domibus.core.user.ui.UserRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Component
public class UserDefaultConverter implements UserConverter {

    @Override
    public User convert(eu.domibus.core.user.ui.User userEntity) {
        List<String> authorities = new ArrayList<>();
        Collection<UserRole> roles = userEntity.getRoles();
        for (UserRole role : roles) {
            authorities.add(role.getName());
        }
        return new User(
                userEntity.getUserName(),
                userEntity.getEmail(),
                userEntity.isActive(),
                authorities,
                UserState.PERSISTED,
                userEntity.getSuspensionDate(),
                userEntity.isDeleted());
    }

    @Override
    public List<User> convert(List<eu.domibus.core.user.ui.User> sourceList) {
        if (sourceList == null) {
            return null;
        }
        List<User> result = new ArrayList<>();
        for (eu.domibus.core.user.ui.User sourceObject : sourceList) {
            result.add(convert(sourceObject));
        }
        return result;
    }
}
