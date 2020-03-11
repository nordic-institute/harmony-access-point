package eu.domibus.core.pmode;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class PModeBeanConfiguration {

    @Bean("jaxbContextConfig")
    public JAXBContext jaxbContextConfig() throws JAXBException {
        return JAXBContext.newInstance("eu.domibus.common.model.configuration");
    }

}
