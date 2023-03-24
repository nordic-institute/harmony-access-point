package eu.domibus.core.multitenancy;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.core.user.plugin.AuthenticationDAO;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author François Gautier
 * @version 5.1
 */
@Service
public class UserDomainManagementService implements DomainsAware {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDomainManagementService.class);

    protected final UserDomainDao userDomainDao;

    protected final DomibusLocalCacheService domibusLocalCacheService;
    private final DomainTaskExecutor domainTaskExecutor;
    private final AuthenticationDAO authenticationDAO;
    private final UserDao userDao;

    public UserDomainManagementService(UserDomainDao userDomainDao,
                                       DomibusLocalCacheService domibusLocalCacheService,
                                       DomainTaskExecutor domainTaskExecutor,
                                       AuthenticationDAO authenticationDAO,
                                       UserDao userDao) {
        this.userDomainDao = userDomainDao;
        this.domibusLocalCacheService = domibusLocalCacheService;
        this.domainTaskExecutor = domainTaskExecutor;
        this.authenticationDAO = authenticationDAO;
        this.userDao = userDao;
    }

    @Override
    public void onDomainAdded(Domain domain) {
        List<String> userNameToAdd = domainTaskExecutor.submit(this::getAllUserNamesToAdd, domain);

        domainTaskExecutor.submit(() -> {
            for (String userName : userNameToAdd) {
                userDomainDao.updateOrCreateUserDomain(userName, domain.getCode());
                LOG.info("DomainUser [{}] added for domain [{}]", userName, domain.getCode());
                domibusLocalCacheService.clearCache(DomibusLocalCacheService.USER_DOMAIN_CACHE);
            }
        });
    }

    private List<String> getAllUserNamesToAdd() {
        List<String> userNames = new ArrayList<>();
        List<AuthenticationEntity> pluginUsers = authenticationDAO.findAll();
        for (AuthenticationEntity pluginUser : pluginUsers) {
            LOG.debug("PluginUser found [{}]", pluginUser.getUserName());
            userNames.add(pluginUser.getUserName());
        }

        List<User> users = userDao.listUsers();
        for (User user : users) {
            LOG.debug("User found [{}]", user.getUserName());
            userNames.add(user.getUserName());
        }
        return userNames;
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        domainTaskExecutor.submit(() -> {
            int usersDeleted = userDomainDao.deleteByDomain(domain.getCode());
            LOG.debug("Remove [{}] User(s) Domain on general schema", usersDeleted);
            domibusLocalCacheService.clearCache(DomibusLocalCacheService.USER_DOMAIN_CACHE);
        });
    }
}
