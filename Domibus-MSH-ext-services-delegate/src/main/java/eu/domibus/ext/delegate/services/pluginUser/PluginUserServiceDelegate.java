package eu.domibus.ext.delegate.services.pluginUser;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.PluginUserDTO;
import eu.domibus.ext.exceptions.PluginUserExtServiceException;
import eu.domibus.ext.services.PluginUserExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * {@inheritDoc}
 *
 * @author Arun Raj
 * @since 5.0
 */
@Service
public class PluginUserServiceDelegate implements PluginUserExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserServiceDelegate.class);

//    protected PluginUserService pluginUserService;

    protected DomibusExtMapper domibusExtMapper;

    protected DomainContextProvider domainProvider;

    public PluginUserServiceDelegate(/*PluginUserService pluginUserService,*/
                                     DomibusExtMapper domibusExtMapper,
                                     DomainContextProvider domainProvider) {
        /*this.pluginUserService = pluginUserService*/;
        this.domibusExtMapper = domibusExtMapper;
        this.domainProvider = domainProvider;
    }

    /* TODO: remove
    public void createParty(PartyDTO partyDTO) {
        Party newParty = domibusExtMapper.partyDTOToParty(partyDTO);
        partyService.createParty(newParty, partyDTO.getCertificateContent());
    }*/

    /**
     * {@inheritDoc}
     *
     * @param pluginUserDTO
     */
    @Override
    public void createPluginUser(PluginUserDTO pluginUserDTO) throws PluginUserExtServiceException {
        LOG.info("@@@ Create Plugin User, domain retrieved is:[{}] ", domainProvider.getCurrentDomain().getCode());
        //domibusExtMapper.partyDTOToParty()
    }
}
