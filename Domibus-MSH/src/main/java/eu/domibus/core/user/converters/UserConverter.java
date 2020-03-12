package eu.domibus.core.user.converters;

import eu.domibus.api.user.User;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.0
 *
 * Class responsible of conversion from the user entity to the corresponding api user object
 *
 */
public interface UserConverter {

    User convert(eu.domibus.core.user.User source);

    List<User> convert(List<eu.domibus.core.user.User> sourceList);
}

