package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.core.property.DomibusPropertyService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION;
import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_UI_TITLE_NAME;


public class PropertyResourceIT extends AbstractIT {

    @Autowired
    DomibusPropertyService domibusPropertyService;

    @Test
    public void testFind() throws Exception {

        List<DomibusProperty> list = domibusPropertyService.getProperties("title");
        Assert.assertTrue(list.size() > 0);

        List<DomibusProperty> list2 = domibusPropertyService.getProperties("domibus.ui.title.name");
        Assert.assertEquals(1, list2.size());
    }

    @Test
    public void testSet() throws Exception {

        String name = DOMIBUS_UI_TITLE_NAME;

        List<DomibusProperty> list = domibusPropertyService.getProperties(name);
        Assert.assertEquals(1, list.size());

        String originalValue = list.get(0).getValue();
        String newValue = originalValue + "MODIFIED";

        domibusPropertyService.setPropertyValue(name, newValue);

        list = domibusPropertyService.getProperties(name);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }

    @Test
    public void testSetCronExpression() throws Exception {

        String name = DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION;
        String newValue = "0 0/5 * * * ?"; // every 5 minutes

        domibusPropertyService.setPropertyValue(name, newValue);

        List<DomibusProperty> list = domibusPropertyService.getProperties(name);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);
    }
}
