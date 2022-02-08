package eu.domibus.ext.delegate.services.pluginUser;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.api.user.plugin.PluginUserService;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.PluginUserDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.PluginUserExtServiceException;
import eu.domibus.ext.services.PluginUserExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * {@inheritDoc}
 *
 * @author Arun Raj
 * @since 5.0
 */
@Service
public class PluginUserServiceDelegate implements PluginUserExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserServiceDelegate.class);

    PluginUserService pluginUserService;

    DomibusExtMapper domibusExtMapper;

    DomainContextProvider domainProvider;

    public PluginUserServiceDelegate(PluginUserService pluginUserService,
                                     DomibusExtMapper domibusExtMapper,
                                     DomainContextProvider domainProvider) {
        this.pluginUserService = pluginUserService;
        this.domibusExtMapper = domibusExtMapper;
        this.domainProvider = domainProvider;
    }

    /**
     * {@inheritDoc}
     *
     * @param pluginUserDTO
     */
    @Override
    public void createPluginUser(PluginUserDTO pluginUserDTO) throws PluginUserExtServiceException {
        //validateCreatePluginUserInput(pluginUserDTO);

        //currently from ext service only Basic authentication will be supported
        AuthenticationEntity authenticationEntity = domibusExtMapper.pluginUserDTOToAuthenticationEntity(pluginUserDTO);
        LOG.debug("At Create Plugin User ext service, AuthenticationEntity created:[{}]", authenticationEntity);

        List<AuthenticationEntity> newUsers = Collections.singletonList(authenticationEntity);
        pluginUserService.updateUsers(newUsers, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    private void validateCreatePluginUserInput(PluginUserDTO pluginUserDTO) {
        validateDomain(pluginUserDTO);
    }

    protected void validateDomain(PluginUserDTO pluginUserDTO) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication securityContextAuthentication = securityContext.getAuthentication();
        LOG.debug("At Plugin user ext service, authenticated with user:[{}]", securityContextAuthentication.getCredentials());
        for (GrantedAuthority authority : securityContextAuthentication.getAuthorities()) {
            LOG.debug("At Plugin user ext service, authenticated with user role:[{}]", authority.getAuthority());
            if (StringUtils.equalsIgnoreCase(AuthRole.ROLE_AP_ADMIN.name(), authority.getAuthority())) {
                if (StringUtils.isBlank(pluginUserDTO.getDomain())) {
                    //if authenticating using super user, then domain must be specified in input
                    throw new PluginUserExtServiceException(DomibusErrorCode.DOM_001, "When authenticating with super user, domain must be specified.");
                }
                domainProvider.setCurrentDomain(pluginUserDTO.getDomain());
            }
        }
        LOG.debug("At Create Plugin User, tenant domain is:[{}] ", domainProvider.getCurrentDomain().getCode());
    }

}
