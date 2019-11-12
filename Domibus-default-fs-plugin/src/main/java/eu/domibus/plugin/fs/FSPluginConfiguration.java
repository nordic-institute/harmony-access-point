package eu.domibus.plugin.fs;

import eu.domibus.common.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.ebms3.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
@Configuration("fsPluginConfiguration")
public class FSPluginConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginConfiguration.class);

    @Bean("fsPluginJaxbContext")
    public JAXBContext jaxbContext() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        return jaxbContext;
    }

    @Bean
    public FSXMLHelper fsxmlHelper(@Qualifier("fsPluginJaxbContext") JAXBContext jaxbContext) {
        FSXMLHelperImpl result = new FSXMLHelperImpl(jaxbContext);
        return result;
    }

    @Bean
    public FSMimeTypeHelper fsMimeTypeHelper() {
        FSMimeTypeHelperImpl result = new FSMimeTypeHelperImpl();
        return result;
    }

    @Bean
    public FSFileNameHelper fsFileNameHelper() {
        List<String> stateSuffixes = getStateSuffixes();
        LOG.debug("Using state suffixes [{}]", stateSuffixes);
        FSFileNameHelper result = new FSFileNameHelper(stateSuffixes);
        return result;
    }

    protected List<String> getStateSuffixes() {
        List<String> result = new LinkedList<>();
        for (MessageStatus status : MessageStatus.values()) {
            result.add(FSFileNameHelper.EXTENSION_SEPARATOR + status.name());
        }
        return result;
    }

}
