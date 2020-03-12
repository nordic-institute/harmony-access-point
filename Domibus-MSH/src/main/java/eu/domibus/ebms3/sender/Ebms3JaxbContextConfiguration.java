package eu.domibus.ebms3.sender;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class Ebms3JaxbContextConfiguration {

    @Bean("jaxbContextEBMS")
    public JAXBContext jaxbContextEBMS() throws JAXBException {
        return JAXBContext.newInstance("eu.domibus.ebms3.common.model");
    }

    @Bean("jaxbContextMessageFragment")
    public JAXBContext jaxbContextMessageFragment() throws JAXBException {
        return JAXBContext.newInstance("eu.domibus.ebms3.common.model.mf");
    }
}
