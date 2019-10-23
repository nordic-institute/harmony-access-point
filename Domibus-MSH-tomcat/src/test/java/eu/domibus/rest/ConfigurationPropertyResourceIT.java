package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.core.property.ConfigurationPropertyService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION;
import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_UI_TITLE_NAME;


public class ConfigurationPropertyResourceIT extends AbstractIT {

    @Autowired
    ConfigurationPropertyService configurationPropertyService;

    @Test
    public void testFind() throws Exception {

        List<DomibusProperty> list = configurationPropertyService.getAllWritableProperties("title", false);
        Assert.assertTrue(list.size() > 0);

        List<DomibusProperty> list2 = configurationPropertyService.getAllWritableProperties("domibus.ui.title.name", false);
        Assert.assertEquals(1, list2.size());
    }

    @Test
    public void testSet() throws Exception {

        String name = DOMIBUS_UI_TITLE_NAME;

        List<DomibusProperty> list = configurationPropertyService.getAllWritableProperties(name, false);
        Assert.assertEquals(1, list.size());

        String originalValue = list.get(0).getValue();
        String newValue = originalValue + "MODIFIED";

        configurationPropertyService.setPropertyValue(name, newValue);

        list = configurationPropertyService.getAllWritableProperties(name, false);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }

    @Test
    public void testSetCronExpression() throws Exception {

        String name = DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION;
        String newValue = "0 0/5 * * * ?"; // every 5 minutes

        configurationPropertyService.setPropertyValue(name, newValue);

        List<DomibusProperty> list = configurationPropertyService.getAllWritableProperties(name, false);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }
}
