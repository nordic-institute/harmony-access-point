package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

@RunWith(JMockit.class)
public class DomibusPropertyChangeManagerTest {

    @Tested
    DomibusPropertyChangeManager domibusPropertyChangeManager;

    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    private DomibusPropertyProviderImpl domibusPropertyProvider;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomibusPropertyChangeNotifier propertyChangeNotifier;

    Map<String, DomibusPropertyMetadata> props;
    String domainCode = "domain1";
    Domain domain = new Domain(domainCode, "DomainName1");

    @Before
    public void setUp() {
        props = Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_ENABLED, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_EMAIL, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, DomibusPropertyMetadata.Usage.DOMAIN, false),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Test
    public void setPropertyValue() {
        String propValue = "propValue";
        String propertyName = DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN;
        DomibusPropertyMetadata propMeta = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);
        String oldValue = "old_value";

        new Expectations(domibusPropertyChangeManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);
            domibusPropertyProvider.getInternalProperty(domain, propertyName);
            result = oldValue;
        }};

        domibusPropertyChangeManager.setPropertyValue(domain, propertyName, propValue, true);

        new Verifications() {{
            domibusPropertyProvider.getInternalProperty(domain, propertyName);
            domibusPropertyChangeManager.doSetPropertyValue(domain, propertyName, propValue);
            domibusPropertyChangeManager.signalPropertyValueChanged(domain, propertyName, propValue, true, propMeta, oldValue);
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setPropertyValue_exception() {
        String propValue = "propValue";
        String propertyName = DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN;
        DomibusPropertyMetadata propMeta = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);

        new Expectations(domibusPropertyChangeManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);
            domibusPropertyChangeManager.validatePropertyValue(propMeta, propValue);
            result = new DomibusPropertyException("Property change listener error");
        }};

        domibusPropertyChangeManager.setPropertyValue(domain, propertyName, propValue, true);

        new Verifications() {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            domibusPropertyProvider.getInternalProperty(domain, propertyName);
            times = 0;
            domibusPropertyChangeManager.doSetPropertyValue(domain, propertyName, propValue);
            times = 0;

        }};
    }
}
