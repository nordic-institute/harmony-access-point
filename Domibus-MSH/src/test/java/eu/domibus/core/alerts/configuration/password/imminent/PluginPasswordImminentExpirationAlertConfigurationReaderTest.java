package eu.domibus.core.alerts.configuration.password.imminent;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.password.imminent.plugin.PluginPasswordImminentExpirationAlertConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class PluginPasswordImminentExpirationAlertConfigurationReaderTest {

    @Tested
    PluginPasswordImminentExpirationAlertConfigurationReader reader;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    public void shouldCheckExtAuthEnabled() {
        boolean res = reader.shouldCheckExtAuthEnabled();
        assertEquals(res, false);
    }

    @Test
    public void getAlertType() {
        AlertType res = reader.getAlertType();
        assertEquals(res, AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION);
    }
}