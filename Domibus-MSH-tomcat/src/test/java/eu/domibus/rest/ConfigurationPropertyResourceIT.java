package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.core.property.ConfigurationPropertyService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;


public class ConfigurationPropertyResourceIT extends AbstractIT {

    @Autowired
    ConfigurationPropertyService configurationPropertyService;

    @Test
    public void testFind() {

        List<DomibusProperty> list = configurationPropertyService.getAllWritableProperties("title", true);
        Assert.assertTrue(list.size() > 0);

        List<DomibusProperty> list2 = configurationPropertyService.getAllWritableProperties("domibus.ui.title.name", true);
        Assert.assertEquals(1, list2.size());
    }

    @Test
    public void testSet() {

        String name = DOMIBUS_UI_TITLE_NAME;

        List<DomibusProperty> list = configurationPropertyService.getAllWritableProperties(name, true);
        Assert.assertEquals(1, list.size());

        String originalValue = list.get(0).getValue();
        String newValue = originalValue + "MODIFIED";

        configurationPropertyService.setPropertyValue(name, true, newValue);

        list = configurationPropertyService.getAllWritableProperties(name, true);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }

    @Test
    public void testSetCronExpression() {

        String name = DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION;
        String newValue = "0 0/5 * * * ?"; // every 5 minutes

        configurationPropertyService.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyService.getAllWritableProperties(name, true);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }

    @Test
    public void testSetConcurrency() {
        String name = DOMIBUS_PULL_QUEUE_CONCURENCY;

        String newValue = "1-1";

        configurationPropertyService.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyService.getAllWritableProperties(name, true);
        Assert.assertEquals(1, list.size());
        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);

        //wrong value
        list = configurationPropertyService.getAllWritableProperties(name, true);
        String oldValue = list.get(0).getValue();
        String wrongValue = "1-1-";

        try {
            configurationPropertyService.setPropertyValue(name, true, wrongValue);
        } catch (DomibusPropertyException ex) {
            list = configurationPropertyService.getAllWritableProperties(name, true);
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

        configurationPropertyService.setPropertyValue(name, true, newValue);

        List<DomibusProperty> list = configurationPropertyService.getAllWritableProperties(name, true);
        Assert.assertEquals(1, list.size());
        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);

        //wrong value
        list = configurationPropertyService.getAllWritableProperties(name, true);
        String oldValue = list.get(0).getValue();
        String wrongValue = "11q";

        try {
            configurationPropertyService.setPropertyValue(name, true, wrongValue);
        } catch (DomibusPropertyException ex) {
            list = configurationPropertyService.getAllWritableProperties(name, true);
            Assert.assertEquals(1, list.size());
            actualValue = list.get(0).getValue();
            Assert.assertEquals(oldValue, actualValue);
        }
    }
}
