package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.PartProperty;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Transactional
public class PartPropertyDaoIT extends AbstractIT {

    @Autowired
    private PartPropertyDao propertyDao;

    @Test
    public void testFindPropertyByNameValueAndType() {
        final String name = "prop1";
        final String value = "value1";
        final String type = "string";
        PartProperty property = buildProperty(name, value, type);
        propertyDao.create(property);

        final PartProperty foundProperty = propertyDao.findOrCreateProperty(name, value, type);
        assertNotNull(foundProperty);

        Assert.assertEquals(property.getEntityId(), foundProperty.getEntityId());
        Assert.assertEquals(name, foundProperty.getName());
        Assert.assertEquals(value, foundProperty.getValue());
        Assert.assertEquals(type, foundProperty.getType());

        final PartProperty foundProperty1 = propertyDao.findOrCreateProperty(name, value, type);

        Assert.assertEquals(foundProperty.getEntityId(), foundProperty1.getEntityId());
    }

    @Test
    public void testFindPropertyByNameAndValue() {
        final String name = "prop1";
        final String value = "value1";
        final String type = null;
        PartProperty property = buildProperty(name, value, type);
        propertyDao.create(property);

        final PartProperty foundProperty = propertyDao.findOrCreateProperty(name, value, type);
        assertNotNull(foundProperty);

        Assert.assertEquals(property.getEntityId(), foundProperty.getEntityId());
        Assert.assertEquals(name, foundProperty.getName());
        Assert.assertEquals(value, foundProperty.getValue());
        Assert.assertEquals(type, foundProperty.getType());

        final PartProperty foundProperty1 = propertyDao.findOrCreateProperty(name, value, type);

        Assert.assertEquals(foundProperty.getEntityId(), foundProperty1.getEntityId());
    }


    private PartProperty buildProperty(String name, String value, String type) {
        PartProperty property = new PartProperty();
        property.setName(name);
        property.setValue(value);
        property.setType(type);
        return property;
    }
}