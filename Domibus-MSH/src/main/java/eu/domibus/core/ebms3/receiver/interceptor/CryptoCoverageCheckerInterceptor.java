package eu.domibus.core.ebms3.receiver.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.wss4j.DefaultCryptoCoverageChecker;
import org.springframework.stereotype.Component;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * Interceptor that checks whether the SOAP message body has been signed and/or encrypted correctly throwing the
 * corresponding errors when this is not the case.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
@Component("cryptoCoverageChecker")
public class CryptoCoverageCheckerInterceptor extends DefaultCryptoCoverageChecker {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CryptoCoverageCheckerInterceptor.class);

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        LOG.debug("Intercepted the SOAP message to check whether the SOAP message body has been signed and/or encrypted correctly or not");
        SAAJInInterceptor.INSTANCE.handleMessage(message);
        super.handleMessage(message);
    }
}
