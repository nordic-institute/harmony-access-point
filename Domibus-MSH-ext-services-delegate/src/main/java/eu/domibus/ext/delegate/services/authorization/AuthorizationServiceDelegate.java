package eu.domibus.ext.delegate.services.authorization;

import com.mchange.v2.lang.StringUtils;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.usermessage.domain.PartyId;
import eu.domibus.api.usermessage.domain.Service;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.core.crypto.spi.PullRequestPmodeData;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
@org.springframework.stereotype.Service
public class AuthorizationServiceDelegate implements eu.domibus.api.authorization.AuthorizationService{

    protected DomibusExtMapper domibusExtMapper;

    protected PModeService pModeService;

    protected AuthorizationSpiProvider authorizationSpiProvider;

    public AuthorizationServiceDelegate(DomibusExtMapper domibusExtMapper, PModeService pModeService, AuthorizationSpiProvider authorizationSpiProvider) {
        this.domibusExtMapper = domibusExtMapper;
        this.pModeService = pModeService;
        this.authorizationSpiProvider = authorizationSpiProvider;
    }

    public void authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            UserMessage userMessage) {
        Service service = userMessage.getCollaborationInfo().getService();
        String serviceName = pModeService.findServiceName(service.getValue(), service.getType());
        String actionName = pModeService.findActionName(userMessage.getCollaborationInfo().getAction());
        String partyName = null;
        Set<PartyId> partyIds = userMessage.getPartyInfo().getFrom().getPartyId();
        for (PartyId partyId : partyIds) {
            partyName = pModeService.findPartyName(partyId.getValue(), partyId.getType());
            if (StringUtils.nonEmptyString(partyName)) {
                break;
            }
        }
        UserMessageDTO userMessageDTO = domibusExtMapper.userMessageToUserMessageDTO(userMessage);
        UserMessagePmodeData userMessagePmodeData = new UserMessagePmodeData(serviceName, actionName, partyName);
        authorizationSpiProvider.getAuthorizationService().authorize(signingCertificateTrustChain, signingCertificate, userMessageDTO, userMessagePmodeData);
    }

    @Override
    public void authorize(List<X509Certificate> signingCertificateTrustChain, X509Certificate signingCertificate, String mpc) {
        final String mpcName;
        try {
            mpcName = pModeService.findMpcName(mpc);
        } catch (Exception e) {
            throw new AuthorizationException(e);
        }
        final PullRequestPmodeData pullRequestPmodeData=new PullRequestPmodeData(mpcName);
        final PullRequestDTO pullRequestDTO = new PullRequestDTO();
        pullRequestDTO.setMpc(mpc);
        authorizationSpiProvider.getAuthorizationService().authorize(signingCertificateTrustChain, signingCertificate,pullRequestDTO,pullRequestPmodeData);
    }
}
