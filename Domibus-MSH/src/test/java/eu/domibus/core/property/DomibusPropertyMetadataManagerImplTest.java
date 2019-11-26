package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadata;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_UI_TITLE_NAME;

@RunWith(JMockit.class)
public class DomibusPropertyMetadataManagerImplTest {

    @Tested
    DomibusPropertyMetadataManagerImpl domibusPropertyMetadataManager;

    @Test
    public void getKnownProperties_nonExisting() {
        Map<String, DomibusPropertyMetadata> props = domibusPropertyMetadataManager.getKnownProperties();
        DomibusPropertyMetadata actual = props.get("non_existing");

        Assert.assertEquals(null, actual);
    }

    @Test
    public void getKnownProperties() {
        Map<String, DomibusPropertyMetadata> props = domibusPropertyMetadataManager.getKnownProperties();
        DomibusPropertyMetadata actual = props.get(DOMIBUS_UI_TITLE_NAME);

        Assert.assertEquals(DOMIBUS_UI_TITLE_NAME, actual.getName());
        Assert.assertEquals(true, actual.isDomainSpecific());
        Assert.assertEquals(true, actual.isWithFallback());
    }

    @Test
    public void hasKnownProperty_nonExisting() {
        boolean actual = domibusPropertyMetadataManager.hasKnownProperty("non_existing");

        Assert.assertEquals(false, actual);
    }

    @Test
    public void hasKnownProperty() {
        boolean actual = domibusPropertyMetadataManager.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);

        Assert.assertEquals(true, actual);
    }
}