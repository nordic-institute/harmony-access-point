package eu.domibus.core.user.plugin;

import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.user.UserState;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.core.converter.AuthCoreMapper;
import eu.domibus.core.user.plugin.security.PluginUserSecurityPolicyManager;
import eu.domibus.web.rest.ro.PluginUserRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PluginUserMapper {

    @Autowired
    private UserDomainService userDomainService;

    @Autowired
    private AuthCoreMapper authCoreMapper;

    @Autowired
    private PluginUserSecurityPolicyManager userSecurityPolicyManager;

    public List<PluginUserRO> convertAndPrepareUsers(List<AuthenticationEntity> userEntities) {
        List<PluginUserRO> users = new ArrayList<>();

        userEntities.forEach(userEntity -> {
            PluginUserRO user = convertAndPrepareUser(userEntity);
            users.add(user);
        });

        return users;
    }

    protected PluginUserRO convertAndPrepareUser(AuthenticationEntity userEntity) {
        PluginUserRO user = authCoreMapper.authenticationEntityToPluginUserRO(userEntity);

        user.setStatus(UserState.PERSISTED.name());
        user.setPassword(null);

        user.setAuthenticationType(userEntity.getAuthenticationType().name());
        user.setSuspended(userEntity.isSuspended());

        String uniqueIdentifier = userEntity.getUniqueIdentifier();
        String domainCode = userDomainService.getDomainForUser(uniqueIdentifier);
        user.setDomain(domainCode);

        if (userEntity.isBasic()) {
            LocalDateTime expDate = userSecurityPolicyManager.getExpirationDate(userEntity);
            user.setExpirationDate(expDate);
        }

        return user;
    }
}
