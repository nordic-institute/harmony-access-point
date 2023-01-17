package eu.domibus.core.multitenancy;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.core.user.plugin.AuthenticationDAO;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import mockit.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static java.util.Arrays.asList;

/**
 * @author François Gautier
 * @version 5.1
 */
@RunWith(MockitoJUnitRunner.class)
public class UserDomainManagementServiceTest {

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected UserDomainDao userDomainDao;
    @Injectable

    protected DomibusLocalCacheService domibusLocalCacheService;
    @Injectable
    private AuthenticationDAO authenticationDAO;
    @Injectable
    private UserDao userDao;
    @Tested
    UserDomainManagementService userDomainManagementService;
    private Domain domain;


    @Before
    public void setUp() throws Exception {
        domain = new Domain("red", "Red");
    }

    @Test
    public void addDomain() {
        ReflectionTestUtils.setField(userDomainManagementService, "domainTaskExecutor", new DomainTaskExecutorTestImpl());
        new Expectations() {{
            authenticationDAO.findAll();
            result = asList(getPluginUser("pluginUser1"),
                    getPluginUser("pluginUser2"));
            userDao.listUsers();
            result = asList(getUser("user1"), getUser("user2"));
        }};
        userDomainManagementService.onDomainAdded(domain);

        new Verifications() {{
            userDomainDao.updateOrCreateUserDomain("pluginUser1", domain.getCode());
            times = 1;
            userDomainDao.updateOrCreateUserDomain("pluginUser2", domain.getCode());
            times = 1;
            userDomainDao.updateOrCreateUserDomain("user1", domain.getCode());
            times = 1;
            userDomainDao.updateOrCreateUserDomain("user2", domain.getCode());
            times = 1;
        }};
    }

    private User getUser(String userName) {
        User user = new User();
        user.setUserName(userName);
        return user;
    }

    private AuthenticationEntity getPluginUser(String userName) {
        AuthenticationEntity authenticationEntity = new AuthenticationEntity();
        authenticationEntity.setUserName(userName);
        return authenticationEntity;
    }

    @Test
    public void removeDomain() throws Exception {
        ReflectionTestUtils.setField(userDomainManagementService, "domainTaskExecutor", new DomainTaskExecutorTestImpl());

        new Expectations() {{
            userDomainDao.deleteByDomain(domain.getCode());
            result = 2;
        }};

        userDomainManagementService.onDomainRemoved(domain);

        new FullVerifications() {{
            domibusLocalCacheService.clearCache(DomibusLocalCacheService.USER_DOMAIN_CACHE);
            times = 1;
        }};
    }

}
