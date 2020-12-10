package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.core.property.ConfigurationPropertyResourceHelper;
import eu.domibus.core.property.DomibusPropertiesFilter;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;


public class ConfigurationPropertyResourceIT extends AbstractIT {

    @Autowired
    ConfigurationPropertyResourceHelper configurationPropertyResourceHelper;

    @Test
    public void testFind() {
        DomibusPropertiesFilter filter = new DomibusPropertiesFilter();
        filter.setName("title");
        filter.setShowDomain(true);
        filter.setWritable(true);

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllProperties(filter);
        Assert.assertTrue(list.size() > 0);

        filter.setName("domibus.ui.title.name");
        List<DomibusProperty> list2 = configurationPropertyResourceHelper.getAllProperties(filter);
        Assert.assertEquals(1, list2.size());
    }

    @Test
    public void testSet() {

        String name = DOMIBUS_UI_TITLE_NAME;

        DomibusPropertiesFilter filter = new DomibusPropertiesFilter();
        filter.setName(name);
        filter.setShowDomain(true);
        filter.setWritable(true);

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllProperties(filter);
        Assert.assertEquals(1, list.size());

        String originalValue = list.get(0).getValue();
        String newValue = originalValue + "MODIFIED";

        configurationPropertyResourceHelper.setPropertyValue(name, true, newValue);

        list = configurationPropertyResourceHelper.getAllProperties(filter);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }

    @Test
    public void testSetCronExpression() {

        String name = DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION;
        String newValue = "0 0/5 * * * ?"; // every 5 minutes

        DomibusPropertiesFilter filter = new DomibusPropertiesFilter();
        filter.setName(name);
        filter.setShowDomain(true);
        filter.setWritable(true);

        configurationPropertyResourceHelper.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllProperties(filter);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }

    @Test
    public void testSetConcurrency() {
        String name = DOMIBUS_PULL_QUEUE_CONCURENCY;

        DomibusPropertiesFilter filter = new DomibusPropertiesFilter();
        filter.setName(name);
        filter.setShowDomain(true);
        filter.setWritable(true);

        String newValue = "1-1";

        configurationPropertyResourceHelper.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllProperties(filter);
        Assert.assertEquals(1, list.size());
        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);

        //wrong value
        list = configurationPropertyResourceHelper.getAllProperties(filter);
        String oldValue = list.get(0).getValue();
        String wrongValue = "1-1-";

        try {
            configurationPropertyResourceHelper.setPropertyValue(name, true, wrongValue);
        } catch (DomibusPropertyException ex) {
            list = configurationPropertyResourceHelper.getAllProperties(filter);
            Assert.assertEquals(1, list.size());
            actualValue = list.get(0).getValue();
            Assert.assertEquals(oldValue, actualValue);
        }
    }

    @Test
    public void testSetNumeric() {
        String name = DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS;

        DomibusPropertiesFilter filter = new DomibusPropertiesFilter();
        filter.setName(name);
        filter.setShowDomain(true);
        filter.setWritable(true);

        //correct value
        String newValue = "-15";

        configurationPropertyResourceHelper.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllProperties(filter);
        Assert.assertEquals(1, list.size());
        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);

        //wrong value
        list = configurationPropertyResourceHelper.getAllProperties(filter);
        String oldValue = list.get(0).getValue();
        String wrongValue = "11q";

        try {
            configurationPropertyResourceHelper.setPropertyValue(name, true, wrongValue);
        } catch (DomibusPropertyException ex) {
            list = configurationPropertyResourceHelper.getAllProperties(filter);
            Assert.assertEquals(1, list.size());
            actualValue = list.get(0).getValue();
            Assert.assertEquals(oldValue, actualValue);
        }
    }

    @Test
    public void testGetProperty() {
        DomibusProperty prop = configurationPropertyResourceHelper.getProperty(DOMIBUS_UI_TITLE_NAME);
        Assert.assertNotNull(prop);
        Assert.assertEquals(DOMIBUS_UI_TITLE_NAME, prop.getMetadata().getName());
    }
}
