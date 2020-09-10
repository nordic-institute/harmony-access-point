package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cxf.DomibusBus;
import eu.domibus.core.ebms3.receiver.interceptor.CheckEBMSHeaderInterceptor;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.util.MessageUtil;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Configuration
public class LocalEndpointConfiguration {

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected DomainService domainService;

    @Bean(name = "localMSH")
    public Endpoint createMSHEndpoint(DomibusBus bus, MSHSourceMessageWebservice mshWebserviceSerializer) {
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);

        LocalTransportFactory localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://cxf.apache.org/transports/local", localTransport);

        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator("http://cxf.apache.org/transports/local", localTransport);

        EndpointImpl endpoint = new EndpointImpl(bus, mshWebserviceSerializer);
        endpoint.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        endpoint.getInInterceptors().add(createSaveRequestToFileInInterceptor());
        endpoint.getInInterceptors().add(new CheckEBMSHeaderInterceptor());
        endpoint.setAddress(MSHDispatcher.LOCAL_MSH_ENDPOINT);
        endpoint.publish();

        return endpoint;
    }

    @Bean
    public MSHSourceMessageWebservice mshSourceMessageWebservice() {
        return new MSHSourceMessageWebservice();
    }

    protected SaveRequestToFileInInterceptor createSaveRequestToFileInInterceptor() {
        SaveRequestToFileInInterceptor result = new SaveRequestToFileInInterceptor();
        result.setDomainContextProvider(domainContextProvider);
        result.setDomibusPropertyProvider(domibusPropertyProvider);
        result.setMessageUtil(messageUtil);
        result.setSplitAndJoinService(splitAndJoinService);
        result.setDomainService(domainService);
        return result;
    }
}
