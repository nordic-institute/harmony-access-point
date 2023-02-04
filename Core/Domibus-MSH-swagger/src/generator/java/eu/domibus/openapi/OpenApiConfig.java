package eu.domibus.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.message.validation.UserMessageValidatorServiceDelegate;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Class contains spring MVC configuration to startup MockMVC with the spring-doc. The class generates all (mocked) beans
 * needed by the domibus REST API defined in module domibus-msh-ext-services-delegate
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */

@ComponentScan(basePackages = {
        "org.springdoc",
        "org.springframework.web",
        "eu.domibus.ext.rest"})
@EnableWebMvc
@Configuration
public class OpenApiConfig {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(OpenApiConfig.class);

    public static final String OPEN_API_DOCS_URL = "/v3/api-docs";
    public static final String DOMIBUS_PROPERTIES = "domibus-swagger.properties";

    /**
     * Open api document configuration. For setting up OpenAPI info modify method below,
     *
     * @return OpenApi
     */
    @Bean
    public OpenAPI customOpenAPI() {

        Server serverDemo = new Server();
        serverDemo.setUrl("/domibus");
        serverDemo.description("Domibus services");
        Properties properties = getProperties();
        return new OpenAPI()
                .info(new Info().title("Domibus API")
                        .description(properties.getProperty("name"))
                        .version(properties.getProperty("version"))
                        .license(new License().name("EUPL 1.2")
                                .url("https://www.eupl.eu/")
                        ))
                .servers(Collections.singletonList(serverDemo));
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(DOMIBUS_PROPERTIES)){
            if (is == null) {
                LOG.warn("[{}] not found", DOMIBUS_PROPERTIES);
            }
            properties.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Could not find the properties in file "+ DOMIBUS_PROPERTIES, e);
        }
        return properties;
    }

    /**
     * Object mapper needed by the springdoc to serialize the  OpenAPI document to JSON.
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper beanJacksonObjectMapper() {
        ObjectMapper jacksonObjectMapper = new ObjectMapper();
        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return jacksonObjectMapper;
    }

    /**
     * Mocked beans for starting up REST API services!
     */
    @Bean
    public MessageAcknowledgeExtService beanMessageAcknowledgeExtService() {
        return Mockito.mock(MessageAcknowledgeExtService.class);
    }

    @Bean
    public DomibusMonitoringExtService beanDomibusMonitoringExtService() {
        return Mockito.mock(DomibusMonitoringExtService.class);
    }

    @Bean
    public PartyExtService beanPartyExtService() {
        return Mockito.mock(PartyExtService.class);
    }

    @Bean
    public CacheExtService beanCacheExtService() {
        return Mockito.mock(CacheExtService.class);
    }

    @Bean
    public PModeExtService beanPModeExtService() {
        return Mockito.mock(PModeExtService.class);
    }

    @Bean
    public UserMessageExtService beanUserMessageExtService() {
        return Mockito.mock(UserMessageExtService.class);
    }

    @Bean
    public DomibusEArchiveExtService beanDomibusEArchiveExtService() {
        return Mockito.mock(DomibusEArchiveExtService.class);
    }

    @Bean
    public MessageMonitorExtService beanMessageMonitorExtService() {
        return Mockito.mock(MessageMonitorExtService.class);
    }

    @Bean
    public PluginUserExtService beanPluginUserExtService() {
        return Mockito.mock(PluginUserExtService.class);
    }

    @Bean
    public AuthenticationExtService beanAuthenticationExtService() {
        return Mockito.mock(AuthenticationExtService.class);
    }

    @Bean
    public PayloadExtService beanPayloadExtService() {
        return Mockito.mock(PayloadExtService.class);
    }

    @Bean
    public UserMessageValidatorServiceDelegate userMessageValidatorServiceDelegate() {
        return Mockito.mock(UserMessageValidatorServiceDelegate.class);
    }

    @Bean
    public PayloadExtService payloadExtService() {
        return Mockito.mock(PayloadExtService.class);
    }

    @Bean
    public DateExtService dateExtService() {
        return Mockito.mock(DateExtService.class);
    }

    @Bean
    public TLSTrustStoreExtService tlsTruststoreExtService() {
        return Mockito.mock(TLSTrustStoreExtService.class);
    }

    @Bean
    public TrustStoreExtService truststoreExtService() {
        return Mockito.mock(TrustStoreExtService.class);
    }

    @Bean
    public DistributedCacheExtService distributedCacheExtService() {
        return Mockito.mock(DistributedCacheExtService.class);
    }

    @Bean
    public MultiPartFileUtil multiPartFileUtil() {
        return Mockito.mock(MultiPartFileUtil.class);
    }

    @Bean DomainContextExtService domainContextExtService() {
        return Mockito.mock(DomainContextExtService.class);
    }

    @Bean DomibusConfigurationExtService domibusConfigurationExtService() {
        return Mockito.mock(DomibusConfigurationExtService.class);
    }
}
