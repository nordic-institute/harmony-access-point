package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.core.property.PropertyService;
import eu.domibus.web.rest.ro.PropertyRO;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class PropertyResourceIT extends AbstractIT {

    @Autowired
    PropertyService propertyService;


    @Test
    public void testFind() throws Exception {

        List<PropertyRO> list = propertyService.getProperties("title");
        Assert.assertTrue(list.size() > 0);

        List<PropertyRO> list2 = propertyService.getProperties("domibus.ui.title.name");
        Assert.assertEquals(1, list2.size());
    }

    @Test
    public void testSet() throws Exception {

        String name = "domibus.UI.title.name";

        List<PropertyRO> list = propertyService.getProperties(name);
        Assert.assertEquals(1, list.size());

        String originalValue = list.get(0).getValue();
        String newValue = originalValue + "MODIFIED";

        propertyService.setPropertyValue(name, newValue);

        list = propertyService.getProperties(name);
        Assert.assertEquals(1, list.size());

        String actualValue = list.get(0).getValue();
        Assert.assertEquals(newValue, actualValue);

    }
}
