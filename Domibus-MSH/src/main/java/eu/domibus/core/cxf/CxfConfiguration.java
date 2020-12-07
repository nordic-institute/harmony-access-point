package eu.domibus.core.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;

@Configuration
public class CxfConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DomibusURLConnectionHTTPConduit domibusURLConnectionHTTPConduit(DomibusHttpsURLConnectionFactory domibusHttpsURLConnectionFactory,
                                                                           Bus bus,
                                                                           EndpointInfo endpointInfo,
                                                                           EndpointReferenceType target) throws IOException {
        return new DomibusURLConnectionHTTPConduit(domibusHttpsURLConnectionFactory, bus, endpointInfo, target);
    }
}