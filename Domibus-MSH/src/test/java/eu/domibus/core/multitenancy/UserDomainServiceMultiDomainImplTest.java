package eu.domibus.core.multitenancy;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.user.User;
import eu.domibus.core.user.ui.converters.UserConverter;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UserDomainServiceMultiDomainImplTest {

    @Mock
    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected UserDomainDao userDomainDao;

    @Injectable
    protected UserDao userDao;

    @Injectable
    protected UserConverter userConverter;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusCacheService domibusCacheService;

    @Injectable
    DomainCoreConverter domainCoreConverter;

    @Tested
    UserDomainServiceMultiDomainImpl userDomainServiceMultiDomainImpl;

    @Captor
    ArgumentCaptor argCaptor;

    @Test
    public void testGetDomainForUser() throws Exception {
        String user = "user1";
        String domain = "domain1";

        new Expectations() {{
            userDomainDao.findDomainByUser(user);
            result = domain;
        }};

        String mockResult = userDomainServiceMultiDomainImpl.getDomainForUser(user);
        String result = mockExecutorSubmit();

        assertEquals(result, domain);
    }

    @Test
    public void testGetPreferredDomainForUser() throws Exception {
        String user = "user1";
        String domain = "domain1";

        new Expectations() {{
            userDomainDao.findPreferredDomainByUser(user);
            result = domain;
        }};

        String mockResult = userDomainServiceMultiDomainImpl.getPreferredDomainForUser(user);
        String result = mockExecutorSubmit();

        assertEquals(result, domain);
    }

//    @Test
//    public void testGetSuperUsers() throws Exception {
//        eu.domibus.core.user.ui.User userEntity = new eu.domibus.core.user.ui.User();
//        List<eu.domibus.core.user.ui.User> userEntities = Arrays.asList(userEntity);
//        User user = new User();
//        List<User> users = Arrays.asList(user);
//
//        new Expectations() {{
//            userDao.listUsers();
//            result = userEntities;
//            userConverter.convert(userEntities);
//            result = users;
//        }};
//
//        List<User> mockResult = userDomainServiceMultiDomainImpl.getSuperUsers();
//        List<User> result = mockExecutorSubmit();
//
//        Assert.assertEquals(users, result);
//    }

    @Test
    public void setDomainForUser() throws Exception {
        String user = "user1";
        String domainCode = "domain1";

        userDomainServiceMultiDomainImpl.setDomainForUser(user, domainCode);
        mockExecutorSubmit();

        new Verifications() {{
            userDomainDao.setDomainByUser(user, domainCode);
            times = 1;
            domibusCacheService.clearCache(DomibusCacheService.USER_DOMAIN_CACHE);
            times = 1;
        }};
    }

    @Test
    public void setPreferredDomainForUser() throws Exception {
        String user = "user1";
        String domainCode = "domain1";

        userDomainServiceMultiDomainImpl.setPreferredDomainForUser(user, domainCode);
        mockExecutorSubmit();

        new Verifications() {{
            userDomainDao.setPreferredDomainByUser(user, domainCode);
            times = 1;
            domibusCacheService.clearCache(DomibusCacheService.PREFERRED_USER_DOMAIN_CACHE);
            times = 1;
        }};
    }

    private <T> T mockExecutorSubmit() throws Exception {
        Mockito.verify(domainTaskExecutor).submit((Callable) argCaptor.capture());
        Callable<T> callable = (Callable<T>) argCaptor.getValue();
        return callable.call();
    }
}
