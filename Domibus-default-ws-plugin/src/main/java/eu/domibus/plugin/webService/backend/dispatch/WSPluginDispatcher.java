
package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.plugin.webService.exception.WSPluginException;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import java.net.ConnectException;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginDispatcher {

    private final DomainContextExtService domainContextExtService;

    private final WSPluginDispatchClientProvider wsPluginDispatchClientProvider;

    public WSPluginDispatcher(DomainContextExtService domainContextExtService,
                              WSPluginDispatchClientProvider wsPluginDispatchClientProvider) {
        this.domainContextExtService = domainContextExtService;
        this.wsPluginDispatchClientProvider = wsPluginDispatchClientProvider;
    }

    @Timer(clazz = WSPluginDispatcher.class, value = "dispatch")
    @Counter(clazz = WSPluginDispatcher.class, value = "dispatch")
    public SOAPMessage dispatch(final SOAPMessage soapMessage, String endpoint) {
        DomainDTO domain = domainContextExtService.getCurrentDomain();

        final Dispatch<SOAPMessage> dispatch = wsPluginDispatchClientProvider.getClient(domain.getCode(), endpoint);

        final SOAPMessage result;
        try {
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

