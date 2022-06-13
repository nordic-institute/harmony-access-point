package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.SEND_WORKER_INTERVAL;
import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.SENT_PURGE_WORKER_CRONEXPRESSION;

@RunWith(JMockit.class)
public class FSPluginPropertiesMetadataManagerImplTest {

    @Injectable
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Tested
    FSPluginPropertiesMetadataManagerImpl propertyMetadataManager;

    @Test
    public void getKnownProperties_nonExisting() {
        Map<String, DomibusPropertyMetadataDTO> props = propertyMetadataManager.getKnownProperties();
        DomibusPropertyMetadataDTO actual = props.get("non_existing");

        Assert.assertEquals(null, actual);
    }

    @Test
    public void getKnownProperties() {
        Map<String, DomibusPropertyMetadataDTO> props = propertyMetadataManager.getKnownProperties();
        DomibusPropertyMetadataDTO actual = props.get(SEND_WORKER_INTERVAL);

        Assert.assertEquals(SEND_WORKER_INTERVAL, actual.getName());
        Assert.assertEquals(true, actual.isDomain());
        Assert.assertEquals(true, actual.isWithFallback());
    }

    @Test
    public void hasKnownProperty_nonExisting() {
        boolean actual = propertyMetadataManager.hasKnownProperty("non_existing");

        Assert.assertEquals(false, actual);
    }

    @Test
    public void hasKnownProperty() {
        boolean actual = propertyMetadataManager.hasKnownProperty(SENT_PURGE_WORKER_CRONEXPRESSION);

        Assert.assertEquals(true, actual);
    }

}