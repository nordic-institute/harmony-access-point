package eu.domibus.core.alerts.configuration.password.imminent;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class ConsolePasswordImminentExpirationAlertConfigurationReaderTest {

    @Tested
    ConsolePasswordImminentExpirationAlertConfigurationReader reader;

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
        new Expectations() {{
            domibusConfigurationService.isExtAuthProviderEnabled();
            result = true;
        }};

        boolean res = reader.shouldCheckExtAuthEnabled();
        assertEquals(res, true);
    }

    @Test
    public void getAlertType() {
        AlertType res = reader.getAlertType();
        assertEquals(res, AlertType.PASSWORD_IMMINENT_EXPIRATION);
    }
}