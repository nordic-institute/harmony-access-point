package eu.domibus.core.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.security.DomibusUserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    private final static IDomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDetailServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainService domainService;

    @Autowired
    private UserDomainService userDomainService;

    @Override
    @Transactional(readOnly = true, noRollbackFor = UsernameNotFoundException.class)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userDao.loadActiveUserByUsername(userName);
        if (user == null) {
            String msg = userName + " has not been found in system";
            LOG.warn(msg);
            throw new UsernameNotFoundException(msg);
        }

        DomibusUserDetailsImpl domibusUserDetails = new DomibusUserDetailsImpl(user);
        domibusUserDetails.setDefaultPasswordUsed(isDefaultPasswordUsed(user));
        domibusUserDetails.setDaysTillExpiration(userService.getDaysTillExpiration(userName));

        Set<String> availableDomains = getAvailableDomains(user);
        LOG.debug("Available domains: [{}]", availableDomains);
        domibusUserDetails.setAvailableDomainCodes(availableDomains);

        return domibusUserDetails;
    }

    private Set<String> getAvailableDomains(User user) {
        // Return all domains for the super admin user; otherwise, return the user's domain
        return user.isSuperAdmin()
                ? domainService.getDomains().stream().map(Domain::getCode).collect(Collectors.toSet())
                : Collections.singleton(userDomainService.getDomainForUser(user.getUserName()));
    }

    private boolean isDefaultPasswordUsed(final User user) {
        boolean checkDefaultPassword = Boolean.parseBoolean(domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD));
        if (!checkDefaultPassword) {
            return false;
        }
        return user.hasDefaultPassword();
    }
}
