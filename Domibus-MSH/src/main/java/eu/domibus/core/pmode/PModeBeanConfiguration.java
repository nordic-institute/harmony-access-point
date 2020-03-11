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

    public static final String COMMON_MODEL_CONFIGURATION_JAXB_CONTEXT_PATH = "eu.domibus.common.model.configuration";

    @Bean("jaxbContextConfig")
    public JAXBContext jaxbContextConfig() throws JAXBException {
        return JAXBContext.newInstance(COMMON_MODEL_CONFIGURATION_JAXB_CONTEXT_PATH);
    }

}
