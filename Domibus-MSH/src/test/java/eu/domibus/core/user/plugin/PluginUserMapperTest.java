package eu.domibus.core.user.plugin;

import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthType;
import eu.domibus.api.user.UserState;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.core.converter.AuthCoreMapper;
import eu.domibus.core.user.plugin.security.PluginUserSecurityPolicyManager;
import eu.domibus.web.rest.ro.PluginUserRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Unit tests for PluginUserMapper
 * @author Arun Raj
 * @since 5.0
 */
@RunWith(JMockit.class)
public class PluginUserMapperTest {

    @Tested
    private PluginUserMapper pluginUserMapper;

    @Injectable
    PluginUserSecurityPolicyManager userSecurityPolicyManager;

    @Injectable
    private AuthCoreMapper authCoreMapper;

    @Injectable
    private UserDomainService userDomainService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void convertAndPrepareUsers() {
        AuthenticationEntity user = new AuthenticationEntity();
        user.setUserName("user1");
        final List<AuthenticationEntity> userList = Arrays.asList(user);

        PluginUserRO userRO = new PluginUserRO();
        userRO.setUserName("user1");
        userRO.setExpirationDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(30));

        LocalDateTime expDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(30);

        new Expectations(pluginUserMapper) {{
            pluginUserMapper.convertAndPrepareUser(user);
            result = userRO;
        }};

        List<PluginUserRO> result = pluginUserMapper.convertAndPrepareUsers(userList);

        Assert.assertEquals(userList.size(), result.size());
        Assert.assertEquals(userRO, result.get(0));
    }

    @Test
    public void convertAndPrepareUser() {
        AuthenticationEntity user = new AuthenticationEntity();
        user.setUserName("user1");

        PluginUserRO userRO = new PluginUserRO();
        userRO.setUserName("user1");
        userRO.setExpirationDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(30));

        LocalDateTime expDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(30);

        new Expectations() {{
            authCoreMapper.authenticationEntityToPluginUserRO(user);
            result = userRO;
            userSecurityPolicyManager.getExpirationDate(user);
            result = expDate;
            userDomainService.getDomainForUser(user.getUniqueIdentifier());
            result="domain1";
        }};

        PluginUserRO result = pluginUserMapper.convertAndPrepareUser(user);

        Assert.assertEquals(userRO, result);
        Assert.assertEquals(UserState.PERSISTED.name(), result.getStatus());
        Assert.assertEquals(AuthType.BASIC.name(), result.getAuthenticationType());
        Assert.assertEquals(!user.isActive() && user.getSuspensionDate() != null, result.isSuspended());
        Assert.assertEquals("domain1", result.getDomain());
        Assert.assertEquals(expDate, result.getExpirationDate());
    }

}