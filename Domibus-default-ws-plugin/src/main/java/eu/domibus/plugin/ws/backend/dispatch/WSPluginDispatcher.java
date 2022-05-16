
package eu.domibus.plugin.ws.backend.dispatch;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ws.exception.WSPluginException;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import java.net.ConnectException;
import java.util.Base64;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginDispatcher {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginDispatcher.class);
    private final DomainContextExtService domainContextExtService;

    private final WSPluginDispatchClientProvider wsPluginDispatchClientProvider;
    private final WSPluginPropertyManager wsPluginPropertyManager;

    public WSPluginDispatcher(DomainContextExtService domainContextExtService,
                              WSPluginDispatchClientProvider wsPluginDispatchClientProvider,
                              WSPluginPropertyManager wsPluginPropertyManager) {
        this.domainContextExtService = domainContextExtService;
        this.wsPluginDispatchClientProvider = wsPluginDispatchClientProvider;
        this.wsPluginPropertyManager = wsPluginPropertyManager;
    }

    public SOAPMessage dispatch(final SOAPMessage soapMessage, String endpoint) {
        DomainDTO domain = domainContextExtService.getCurrentDomain();

        final Dispatch<SOAPMessage> dispatch = wsPluginDispatchClientProvider.getClient(domain.getCode(), endpoint);

        final SOAPMessage result;
        try {
            // adding basic authentication when notifying C4 via push events
            String username = wsPluginPropertyManager.getKnownPropertyValue(WSPluginPropertyManager.DISPATCHER_PUSH_AUTH_USERNAME);
            String password = wsPluginPropertyManager.getKnownPropertyValue(WSPluginPropertyManager.DISPATCHER_PUSH_AUTH_PASSWORD);
            if (isNoneBlank(username, password)) {
                String credentials = username+":"+password;
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                soapMessage.getMimeHeaders().addHeader("Authorization", "Basic " + encodedCredentials);
                LOG.debug("Authorization header added for user [{}]", username);
            }

            result = dispatch.invoke(soapMessage);
        } catch (final WebServiceException e) {
            Exception exception = e;
            if (e.getCause() instanceof ConnectException) {
                exception = new WebServiceException("Error dispatching message to [" + endpoint + "]: possible reason is that the receiver is not available", e);
            }
            throw new WSPluginException("Error dispatching message to " + endpoint, exception);
        }
        return result;
    }
}

