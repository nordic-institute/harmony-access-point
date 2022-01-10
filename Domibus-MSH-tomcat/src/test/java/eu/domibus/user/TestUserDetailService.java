package eu.domibus.user;

import eu.domibus.core.user.ui.User;
import eu.domibus.web.security.UserDetail;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author François Gautier
 * @since 5.0
 *
 * Providing UserDetail for annotation @WithUserDetails(value = LOGGED_USER, userDetailsServiceBeanName = "testUserDetailService")
 */
@Service
public class TestUserDetailService implements UserDetailsService {

    @Override
    @Transactional(readOnly = true, noRollbackFor = UsernameNotFoundException.class)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = new User();
        user.setUserName(userName);
        user.setPassword(userName);
        return new UserDetail(user);
    }

}
