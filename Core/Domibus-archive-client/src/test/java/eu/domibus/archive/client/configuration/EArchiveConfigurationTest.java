package eu.domibus.archive.client.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.archive.client.api.ArchiveWebhookApi;
import eu.domibus.archive.client.invoker.auth.HttpBasicAuth;
import eu.domibus.core.proxy.DomibusProxyService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class EArchiveConfigurationTest {
    @Tested
    private EArchiveConfiguration eArchiveConfiguration;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;
    @Injectable
    private DomibusProxyService domibusProxyService;
    @Injectable
    private ObjectMapper objectMapper;

    @Test
    public void initializeEarchivingClientApi() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_URL);
            result = "url";

            domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_USERNAME);
            result = "username";

            domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_PASSWORD);
            result = "password";
        }};

        ArchiveWebhookApi archiveWebhookApi = eArchiveConfiguration.getEarchivingClientApi();

        HttpBasicAuth basicAuth = (HttpBasicAuth) archiveWebhookApi
                .getApiClient()
                .getAuthentications()
                .values()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Authentication found"));
        assertEquals("username", basicAuth.getUsername());
        assertEquals("password", basicAuth.getPassword());
    }
}
