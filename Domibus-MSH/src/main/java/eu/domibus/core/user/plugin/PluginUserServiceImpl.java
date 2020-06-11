package eu.domibus.core.user.plugin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthType;
import eu.domibus.api.user.UserBase;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.user.plugin.security.PluginUserSecurityPolicyManager;
import eu.domibus.core.user.plugin.security.password.PluginUserPasswordHistoryDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.PluginUserRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel, Catalin Enache
 * @since 4.0
 */
@Service
public class PluginUserServiceImpl implements PluginUserService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserServiceImpl.class);

    @Autowired
    @Qualifier("securityAuthenticationDAO")
    private AuthenticationDAO authenticationDAO;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    private UserDomainService userDomainService;

    @Autowired
    private DomainContextProvider domainProvider;

    @Autowired
    private PluginUserSecurityPolicyManager userSecurityPolicyManager;

    @Autowired
    PluginUserAlertsServiceImpl userAlertsService;

    @Autowired
    PluginUserPasswordHistoryDao pluginUserPasswordHistoryDao;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Override
    public List<PluginUserRO> findUsers(AuthType authType, AuthRole authRole, String originalUser, String userName, int page, int pageSize) {
        Map<String, Object> filters = createFilterMap(authType, authRole, originalUser, userName);
        List<AuthenticationEntity> users = authenticationDAO.findPaged(page * pageSize, pageSize, "entityId", true, filters);
        List<PluginUserRO> res = convertEntities(users);
        return res;
    }

    @Override
    public long countUsers(AuthType authType, AuthRole authRole, String originalUser, String userName) {
        Map<String, Object> filters = createFilterMap(authType, authRole, originalUser, userName);
        return authenticationDAO.countEntries(filters);
    }

    @Override
    @Transactional
    public void updateUsers(List<AuthenticationEntity> addedUsers, List<AuthenticationEntity> updatedUsers, List<AuthenticationEntity> removedUsers) {

        final Domain currentDomain = domainProvider.getCurrentDomain();

        checkUsers(addedUsers);

        addedUsers.forEach(u -> insertNewUser(u, currentDomain));

        updatedUsers.forEach(u -> updateUser(u));

        removedUsers.forEach(u -> deleteUser(u));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void triggerPasswordAlerts() {
        userAlertsService.triggerPasswordExpirationEvents();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void reactivateSuspendedUsers() {
        userSecurityPolicyManager.reactivateSuspendedUsers();
    }

    protected List<PluginUserRO> convertEntities(List<AuthenticationEntity> userEntities) {
        List<PluginUserRO> users = new ArrayList<>();

        userEntities.forEach(userEntity -> {
            PluginUserRO user = domainConverter.convert(userEntity, PluginUserRO.class);

            user.setStatus(UserState.PERSISTED.name());
            user.setPassword(null);

            AuthType authenticationType = StringUtils.isEmpty(user.getCertificateId()) ? AuthType.BASIC : AuthType.CERTIFICATE;
            user.setAuthenticationType(authenticationType.name());

            boolean isSuspended = !userEntity.isActive() && userEntity.getSuspensionDate() != null;
            user.setSuspended(isSuspended);

            String domainCode = userDomainService.getDomainForUser(userEntity.getUniqueIdentifier());
            user.setDomain(domainCode);

            LocalDateTime expDate = userSecurityPolicyManager.getExpirationDate(userEntity);
            user.setExpirationDate(expDate);

            users.add(user);
        });

        return users;
    }

    /**
     * get all users from general schema and validate new users against existing names
     *
     * @param addedUsers
     */
    private void checkUsers(List<AuthenticationEntity> addedUsers) {
        // check duplicates with other plugin users
        for (AuthenticationEntity user : addedUsers) {
            if (!StringUtils.isEmpty(user.getUserName())) {
                if (addedUsers.stream().anyMatch(x -> x != user && user.getUserName().equalsIgnoreCase(x.getUserName())))
                    throw new UserManagementException("Cannot add user " + user.getUserName() + " more than once.");
            }
            if (StringUtils.isNotBlank(user.getCertificateId())) {
                if (addedUsers.stream().anyMatch(x -> x != user && user.getCertificateId().equalsIgnoreCase(x.getCertificateId())))
                    throw new UserManagementException("Cannot add user with certificate " + user.getCertificateId() + " more than once.");
            }
        }

        // check for duplicates with other users or plugin users in single and multi-tenancy modes
        for (UserBase user : addedUsers) {
            userSecurityPolicyManager.validateUniqueUser(user);
        }
    }

    protected Map<String, Object> createFilterMap(AuthType authType, AuthRole authRole, String originalUser, String userName) {
        HashMap<String, Object> filters = new HashMap<>();
        if (authType != null) {
            filters.put("authType", authType.name());
        }
        if (authRole != null) {
            filters.put("authRoles", authRole.name());
        }
        filters.put("originalUser", originalUser);
        filters.put("userName", userName);
        return filters;
    }

    protected void insertNewUser(AuthenticationEntity u, Domain domain) {
        if (u.getPassword() != null) {
            userSecurityPolicyManager.validateComplexity(u.getUserName(), u.getPassword());
            u.setPassword(bcryptEncoder.encode(u.getPassword()));
        }
        authenticationDAO.create(u);

        userDomainService.setDomainForUser(u.getUniqueIdentifier(), domain.getCode());
    }

    protected void updateUser(AuthenticationEntity modified) {
        AuthenticationEntity existing = authenticationDAO.read(modified.getEntityId());

        if (StringUtils.isBlank(existing.getCertificateId())) {
            // locking policy is only applicable to Basic auth plugin users
            userSecurityPolicyManager.applyLockingPolicyOnUpdate(modified);
        }

        if (!StringUtils.isEmpty(modified.getPassword())) {
            changePassword(existing, modified.getPassword());
        }

        existing.setAuthRoles(modified.getAuthRoles());
        existing.setOriginalUser(modified.getOriginalUser());

        authenticationDAO.update(existing);
    }

    private void changePassword(AuthenticationEntity user, String newPassword) {
        userSecurityPolicyManager.changePassword(user, newPassword);
    }

    protected void deleteUser(AuthenticationEntity u) {
        AuthenticationEntity entity = authenticationDAO.read(u.getEntityId());
        delete(entity);

        userDomainService.deleteDomainForUser(u.getUniqueIdentifier());
    }

    private void delete(AuthenticationEntity user) {
        //delete password history
        pluginUserPasswordHistoryDao.removePasswords(user, 0);
        //delete actual user
        authenticationDAO.delete(user);
    }
}
