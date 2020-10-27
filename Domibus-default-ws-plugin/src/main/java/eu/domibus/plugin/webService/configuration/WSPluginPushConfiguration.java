package eu.domibus.plugin.webService.configuration;

import eu.domibus.webservice.backend.generated.BackendInterface;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Class responsible for the configuration of the push functionality of the ws plugin
 *
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class WSPluginPushConfiguration {

    public static final String JAXB_CONTEXT_WEBSERVICE_BACKEND = "jaxbContextWebserviceBackend";

    @Bean(JAXB_CONTEXT_WEBSERVICE_BACKEND)
    public JAXBContext jaxbContextEBMS() throws JAXBException {
        return JAXBContext.newInstance(BackendInterface.class.getPackage().getName());
    }
}
