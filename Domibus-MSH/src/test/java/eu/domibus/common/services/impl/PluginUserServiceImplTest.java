package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthType;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.security.PluginUserSecurityPolicyManager;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.core.security.AuthenticationEntity;
import eu.domibus.core.security.PluginUserPasswordHistoryDao;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@RunWith(JMockit.class)
public class PluginUserServiceImplTest {

    @Tested
    private PluginUserServiceImpl pluginUserService;

    @Injectable
    @Qualifier("securityAuthenticationDAO")
    private AuthenticationDAO securityAuthenticationDAO;

    @Injectable
    private BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    private UserDomainService userDomainService;

    @Injectable
    private DomainContextProvider domainProvider;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PluginUserSecurityPolicyManager pluginUserSecurityPolicyManager;

    @Injectable
    PluginUserAlertsServiceImpl userAlertsService;

    @Injectable
    private PluginUserSecurityPolicyManager userSecurityPolicyManager;

    @Injectable
    PluginUserPasswordHistoryDao pluginUserPasswordHistoryDao;

    @Test(expected = UserManagementException.class)
    public void testUpdateUsersWithDuplicateName() {
        AuthenticationEntity user1 = new AuthenticationEntity();
        user1.setUserName("username1");
        AuthenticationEntity user2 = new AuthenticationEntity();
        user2.setUserName("username1");

        List<AuthenticationEntity> addedUsers = Arrays.asList(new AuthenticationEntity[]{user1, user2});
        List<AuthenticationEntity> updatedUsers = new ArrayList();
        List<AuthenticationEntity> removedUsers = new ArrayList();

        pluginUserService.updateUsers(addedUsers, updatedUsers, removedUsers);
    }

    @Test(expected = UserManagementException.class)
    public void testUpdateUsersWithDuplicateCertificateId() {
        AuthenticationEntity user1 = new AuthenticationEntity();
        user1.setCertificateId("aaa");
        AuthenticationEntity user2 = new AuthenticationEntity();
        user2.setCertificateId("aaa");

        List<AuthenticationEntity> addedUsers = Arrays.asList(new AuthenticationEntity[]{user1, user2});
        List<AuthenticationEntity> updatedUsers = new ArrayList();
        List<AuthenticationEntity> removedUsers = new ArrayList();

        pluginUserService.updateUsers(addedUsers, updatedUsers, removedUsers);
    }

    @Test()
    public void testUpdateUsers() {
        Domain currentDomain = new Domain("d1","D1");

        AuthenticationEntity added_user = new AuthenticationEntity();
        added_user.setCertificateId("added_user");
        List<AuthenticationEntity> addedUsers = Arrays.asList(new AuthenticationEntity[]{added_user});

        AuthenticationEntity updated_user = new AuthenticationEntity();
        updated_user.setCertificateId("updated_user");
        List<AuthenticationEntity> updatedUsers = Arrays.asList(new AuthenticationEntity[]{updated_user});

        AuthenticationEntity deleted_user = new AuthenticationEntity();
        updated_user.setCertificateId("deleted_user");
        List<AuthenticationEntity> removedUsers = Arrays.asList(new AuthenticationEntity[]{deleted_user});

        new Expectations() {{
            domainProvider.getCurrentDomain();
            result = currentDomain;
        }};

        pluginUserService.updateUsers(addedUsers, updatedUsers, removedUsers);

        new Verifications() {{
            pluginUserService.insertNewUser(added_user, currentDomain);
            times = 1;

            pluginUserService.updateUser(updated_user);
            times = 1;

            pluginUserService.deleteUser(deleted_user);
            times = 1;
        }};
    }

    @Test()
    public void createFilterMapTest() {
        AuthType authType = AuthType.BASIC;
        AuthRole authRole = AuthRole.ROLE_ADMIN;
        String originalUser = "originalUser1";
        String userName  = "userName1";

        Map<String, Object> filters = pluginUserService.createFilterMap(authType, authRole, originalUser, userName);

        Assert.assertEquals("BASIC" , filters.get("authType"));
        Assert.assertEquals("ROLE_ADMIN" , filters.get("authRoles"));
        Assert.assertEquals("originalUser1" , filters.get("originalUser"));
    }

    @Test()
    public void triggerPasswordAlertsTest() {
        pluginUserService.triggerPasswordAlerts();

        new Verifications() {{
            userAlertsService.triggerPasswordExpirationEvents();
            times = 1;
        }};
    }

    @Test()
    public void reactivateSuspendedUsersTest() {
        pluginUserService.reactivateSuspendedUsers();

        new Verifications() {{
            userSecurityPolicyManager.reactivateSuspendedUsers();
            times = 1;
        }};
    }
}
