package eu.domibus.ext.delegate.services.pluginUser;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.api.user.plugin.PluginUserService;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.PluginUserDTO;
import eu.domibus.ext.exceptions.PluginUserExtServiceException;
import eu.domibus.ext.services.PluginUserExtService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserServiceDelegate.class);

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
        //currently from ext service only Basic authentication will be supported
        AuthenticationEntity authenticationEntity = domibusExtMapper.pluginUserDTOToAuthenticationEntity(pluginUserDTO);
        LOG.debug("At Create Plugin User ext service, AuthenticationEntity created:[{}]", authenticationEntity);

        List<AuthenticationEntity> newUsers = Collections.singletonList(authenticationEntity);
        pluginUserService.updateUsers(newUsers, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

}
