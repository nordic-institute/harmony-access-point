package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

@RunWith(JMockit.class)
public class DomibusPropertyManagerImplTest {

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomibusPropertyChangeNotifier propertyChangeNotifier;

    @Injectable
    DomibusPropertyMetadataManagerImpl domibusPropertyMetadataManager;

    @Tested
    DomibusPropertyManagerImpl domibusPropertyManager;

    Map<String, DomibusPropertyMetadata> props;

    @Before
    public void setUp() {
        props = Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, true, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_ENABLED, true, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, true, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_EMAIL, true, true),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));

    }

    @Test
    public void getKnownProperties() {
        new Expectations() {{
            domibusPropertyMetadataManager.getKnownProperties();
            result = props;
        }};

        Map<String, DomibusPropertyMetadata> actual = domibusPropertyManager.getKnownProperties();

        Assert.assertEquals(props, actual);
    }

    @Test
    public void hasKnownProperty() {
        new Expectations() {{
            domibusPropertyMetadataManager.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);
            result = true;
        }};

        boolean actual = domibusPropertyManager.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void getKnownPropertyValue() {
    }

    @Test
    public void setKnownPropertyValue() {
    }

    @Test
    public void testSetKnownPropertyValue() {
    }
}