package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.core.property.ConfigurationPropertyResourceHelper;
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

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllWritableProperties("title", true, null, null, null);
        Assert.assertTrue(list.size() > 0);

        List<DomibusProperty> list2 = configurationPropertyResourceHelper.getAllWritableProperties("domibus.ui.title.name", true, null, null, null);
        Assert.assertEquals(1, list2.size());
    }

    @Test
    public void testSet() {

        String name = DOMIBUS_UI_TITLE_NAME;

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
        Assert.assertEquals(1, list.size());

        String originalValue = list.get(0).getValue();
        String newValue = originalValue + "MODIFIED";

        configurationPropertyResourceHelper.setPropertyValue(name, true, newValue);

        list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }

    @Test
    public void testSetCronExpression() {

        String name = DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION;
        String newValue = "0 0/5 * * * ?"; // every 5 minutes

        configurationPropertyResourceHelper.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }

    @Test
    public void testSetConcurrency() {
        String name = DOMIBUS_PULL_QUEUE_CONCURENCY;

        String newValue = "1-1";

        configurationPropertyResourceHelper.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
        Assert.assertEquals(1, list.size());
        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);

        //wrong value
        list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
        String oldValue = list.get(0).getValue();
        String wrongValue = "1-1-";

        try {
            configurationPropertyResourceHelper.setPropertyValue(name, true, wrongValue);
        } catch (DomibusPropertyException ex) {
            list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
            Assert.assertEquals(1, list.size());
            actualValue = list.get(0).getValue();
            Assert.assertEquals(oldValue, actualValue);
        }
    }

    @Test
    public void testSetNumeric() {
        String name = DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS;

        //correct value
        String newValue = "-15";

        configurationPropertyResourceHelper.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
        Assert.assertEquals(1, list.size());
        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);

        //wrong value
        list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
        String oldValue = list.get(0).getValue();
        String wrongValue = "11q";

        try {
            configurationPropertyResourceHelper.setPropertyValue(name, true, wrongValue);
        } catch (DomibusPropertyException ex) {
            list = configurationPropertyResourceHelper.getAllWritableProperties(name, true, null, null, null);
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
