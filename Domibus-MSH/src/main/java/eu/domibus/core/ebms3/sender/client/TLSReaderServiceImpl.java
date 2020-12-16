
package eu.domibus.core.ebms3.sender.client;

import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.jaxb.JAXBContextCache;
import org.apache.cxf.common.jaxb.JAXBUtils;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.jsse.TLSClientParametersConfig;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.apache.cxf.staxutils.StaxUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class TLSReaderServiceImpl implements TLSReaderService {
    public static final String CLIENT_AUTHENTICATION_XML = "clientauthentication.xml";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSReaderServiceImpl.class);

    public static final String REGEX_DOMIBUS_CONFIG_LOCATION = "\\Q${domibus.config.location}\\E";

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Cacheable("tlsCache")
    public TLSClientParameters getTlsClientParameters(String domainCode) {
        Optional<Path> path = getClientAuthenticationPath(domainCode);
        if (!path.isPresent()) {
            return null;
        }
        try {
            byte[] encoded = Files.readAllBytes(path.get());
            String config = new String(encoded, "UTF-8");
            //TODO this replacement should be extracted into a service method
            config = config.replaceAll(REGEX_DOMIBUS_CONFIG_LOCATION, domibusConfigurationService.getConfigLocation().replace('\\', '/'));

            return (TLSClientParameters) TLSClientParametersConfig.createTLSClientParameters(config);
        } catch (Exception e) {
            LOG.warn("Mutual authentication will not be supported for [{}]", path);
            LOG.trace("", e);
            return null;
        }
    }

    public TLSClientParametersType getTlsClientParametersType(String domainCode) {
        Optional<Path> path = getClientAuthenticationPath(domainCode);
        if (!path.isPresent()) {
            return null;
        }
        try {
            byte[] encoded = Files.readAllBytes(path.get());
            String config = new String(encoded, "UTF-8");
            //TODO this replacement should be extracted into a service method
            config = config.replaceAll(REGEX_DOMIBUS_CONFIG_LOCATION, domibusConfigurationService.getConfigLocation().replace('\\', '/'));

            return createTLSClientParameters(config);
        } catch (Exception e) {
            LOG.warn("Mutual authentication will not be supported for [{}]", path);
            LOG.trace("", e);
            return null;
        }

    }

    private TLSClientParametersType createTLSClientParameters(String s) {
        StringReader reader = new StringReader(s);
        XMLStreamReader data = StaxUtils.createXMLStreamReader(reader);

        try {
            JAXBElement<TLSClientParametersType> type = JAXBUtils.unmarshall(getContext(), data, TLSClientParametersType.class);
            return (TLSClientParametersType) type.getValue();
        } catch (RuntimeException var15) {
            throw var15;
        } catch (Exception var16) {
            throw new RuntimeException(var16);
        } finally {
            try {
                StaxUtils.close(data);
            } catch (XMLStreamException var14) {
                throw new RuntimeException(var14);
            }
        }
    }

    private static Set<Class<?>> classes;
    private static JAXBContext context;

    private synchronized JAXBContext getContext() throws JAXBException {
        if (context == null || classes == null) {
            Set<Class<?>> c2 = new HashSet();
            JAXBContextCache.addPackage(c2, PackageUtils.getPackageName(TLSClientParametersType.class), TLSClientParametersConfig.class.getClassLoader());
            JAXBContextCache.CachedContextAndSchemas ccs = JAXBContextCache.getCachedContextAndSchemas(c2, (String) null, (Map) null, (Collection) null, false);
            classes = ccs.getClasses();
            context = ccs.getContext();
        }

        return context;
    }


    /**
     * <p>Returns the path to the file that contains the TLS client configuration parameters.</p><br />
     *
     * <p>The file is searched in the Domibus configuration location under a file named {@value #CLIENT_AUTHENTICATION_XML} by default. The first lookup happens by prefixing
     * this default name with the domain code followed by an underscore character. If the file having this name does not exist, another lookup happens by using just the default
     * name, without any prefixes. An optional path is returned when neither of these two files is found.</p>
     *
     * @param domainCode The domain code used to prefix the default file name during the first lookup.
     * @return the path to an existing domain specific client authentication file or the path to an existing default client authentication file; otherwise, an empty
     * {@code Optional} path.
     */
    private Optional<Path> getClientAuthenticationPath(String domainCode) {
        String domainSpecificFileName = StringUtils.stripToEmpty(domainCode) + "_" + CLIENT_AUTHENTICATION_XML;
        Path domainSpecificPath = Paths.get(domibusConfigurationService.getConfigLocation(), domainSpecificFileName);
        boolean domainSpecificPathExists = Files.exists(domainSpecificPath);
        LOG.debug("Client authentication file [{}] at the domain specific path [{}] exists [{}]", domainSpecificFileName, domainSpecificPath, domainSpecificPathExists);
        if (domainSpecificPathExists) {
            return Optional.of(domainSpecificPath);
        }

        Path defaultPath = Paths.get(domibusConfigurationService.getConfigLocation(), CLIENT_AUTHENTICATION_XML);
        boolean defaultPathExists = Files.exists(defaultPath);
        LOG.debug("Client authentication file at the default path [{}] exists [{}]", defaultPath, defaultPathExists);
        if (defaultPathExists) {
            return Optional.of(defaultPath);
        }

        return Optional.empty();
    }

}
