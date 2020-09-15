package eu.domibus.core.ebms3.receiver;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.crypto.Wss4JMultiDomainCryptoProvider;
import eu.domibus.core.cxf.DomibusBus;
import eu.domibus.core.ebms3.receiver.interceptor.*;
import eu.domibus.core.ebms3.receiver.policy.SetPolicyInInterceptor;
import eu.domibus.core.ebms3.receiver.policy.SetPolicyInServerInterceptor;
import eu.domibus.core.ebms3.receiver.policy.SetPolicyOutInterceptorServer;
import eu.domibus.core.ebms3.sender.interceptor.HttpHeaderInInterceptor;
import eu.domibus.core.ebms3.sender.interceptor.HttpHeaderOutInterceptor;
import eu.domibus.core.logging.cxf.DomibusLoggingEventSender;
import eu.domibus.core.message.pull.SaveRawPulledMessageInterceptor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.handler.SetCodeValueFaultOutInterceptor;
import org.apache.cxf.ws.security.tokenstore.EHCacheTokenStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class MSHWebserviceConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebserviceConfiguration.class);

    @Bean("msh")
    public Endpoint msh(DomibusBus domibusBus,
                        MSHWebservice mshWebservice,
                        @Qualifier("loggingFeature") LoggingFeature loggingFeature,
                        @Qualifier("ehCacheTokenStore") EHCacheTokenStore ehCacheTokenStore,
                        SimpleKeystorePasswordCallback simpleKeystorePasswordCallback,
                        Wss4JMultiDomainCryptoProvider wss4JMultiDomainCryptoProvider,
                        DomibusReadyInterceptor domibusReadyInterceptor,
                        SetDomainInInterceptor setDomainInInterceptor,
                        TrustSenderInterceptor trustSenderInterceptor,
                        SetPolicyInServerInterceptor setPolicyInServerInterceptor,
                        PropertyValueExchangeInterceptor propertyValueExchangeInterceptor,
                        HttpHeaderInInterceptor httpHeaderInInterceptor,
                        ClearMDCInterceptor clearMDCInterceptor,
                        SetPolicyOutInterceptorServer setPolicyOutInterceptorServer,
                        SaveRawPulledMessageInterceptor saveRawPulledMessageInterceptor,
                        HttpHeaderOutInterceptor httpHeaderOutInterceptor,
                        @Qualifier("domibusSetCodeValueFaultOutInterceptor") SetCodeValueFaultOutInterceptor setCodeValueFaultOutInterceptor,
                        FaultInHandler faultInHandler) {
        EndpointImpl endpoint = new EndpointImpl(domibusBus, mshWebservice);
        Map<String, Object> endpointProperties = getEndpointProperties(ehCacheTokenStore, simpleKeystorePasswordCallback, wss4JMultiDomainCryptoProvider);
        endpoint.setProperties(endpointProperties);
        endpoint.setInInterceptors(Arrays.asList(domibusReadyInterceptor, setDomainInInterceptor, trustSenderInterceptor, setPolicyInServerInterceptor, propertyValueExchangeInterceptor, httpHeaderInInterceptor));
        endpoint.setOutInterceptors(Arrays.asList(clearMDCInterceptor, setPolicyOutInterceptorServer, saveRawPulledMessageInterceptor, httpHeaderOutInterceptor));
        endpoint.setOutFaultInterceptors(Arrays.asList(setCodeValueFaultOutInterceptor, clearMDCInterceptor));
        endpoint.setFeatures(Arrays.asList(loggingFeature));
        endpoint.setHandlers(Arrays.asList(faultInHandler));

        endpoint.publish("/msh");
        return endpoint;
    }

    protected Map<String, Object> getEndpointProperties(EHCacheTokenStore ehCacheTokenStore,
                                                        SimpleKeystorePasswordCallback simpleKeystorePasswordCallback,
                                                        Wss4JMultiDomainCryptoProvider wss4JMultiDomainCryptoProvider) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("org.apache.cxf.ws.security.tokenstore.TokenStore", ehCacheTokenStore);
        properties.put("ws-security.cache.config.file", "/cxf-ehcache.xml");
        properties.put("ws-security.callback-handler", simpleKeystorePasswordCallback);
        properties.put("ws-security.encryption.crypto", wss4JMultiDomainCryptoProvider);
        properties.put("ws-security.signature.crypto", wss4JMultiDomainCryptoProvider);
        properties.put("ws-security.encryption.username", "useReqSigCert");
        properties.put("faultStackTraceEnabled", "false");
        properties.put("exceptionMessageCauseEnabled", "false");

        return properties;
    }

    @Bean("mshWebservice")
    public MSHWebservice mshWebservice() {
        return new MSHWebservice();
    }

    @Bean("loggingFeature")
    public LoggingFeature loggingFeature(DomibusLoggingEventSender domibusLoggingEventSender,
                                         DomibusPropertyProvider domibusPropertyProvider) {
        LoggingFeature result = new LoggingFeature();
        result.setSender(domibusLoggingEventSender);
        Integer cxfLimit = domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_LOGGING_CXF_LIMIT);
        LOG.debug("CXF logging limit set to [{}]", cxfLimit);
        result.setLimit(cxfLimit);
        return result;
    }

    @Bean("loggingSender")
    public DomibusLoggingEventSender domibusLoggingEventSender(DomibusPropertyProvider domibusPropertyProvider) {
        DomibusLoggingEventSender result = new DomibusLoggingEventSender();
        Boolean printPayload = domibusPropertyProvider.getBooleanProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_LOGGING_PAYLOAD_PRINT);
        LOG.debug("Print payload activated [{}] ?", printPayload);
        result.setPrintPayload(printPayload);
        return result;
    }

    @Bean("domibusReadyInterceptor")
    public DomibusReadyInterceptor domibusReadyInterceptor() {
        DomibusReadyInterceptor readyInterceptor = new DomibusReadyInterceptor();
        Collection<String> beforeInterceptors = new ArrayList<>();
        beforeInterceptors.add(SetPolicyInInterceptor.class.getCanonicalName());
        readyInterceptor.setBefore(beforeInterceptors);
        return readyInterceptor;
    }

    @Bean("domibusSetCodeValueFaultOutInterceptor")
    public SetCodeValueFaultOutInterceptor setCodeValueFaultOutInterceptor() {
        return new SetCodeValueFaultOutInterceptor();
    }
}
