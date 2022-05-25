package eu.domibus.core.ebms3.sender;

import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
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
        return JAXBContext.newInstance(Ebms3UserMessage.class.getPackage().getName());
    }

    @Bean("jaxbContextMessageFragment")
    public JAXBContext jaxbContextMessageFragment() throws JAXBException {
        return JAXBContext.newInstance(Ebms3MessageFragmentType.class.getPackage().getName());
    }
}
