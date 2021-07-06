package eu.domibus.core.message.dictionary;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MessageProperty;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

/**
 * @author François Gautier
 * @since 5.0
 */
@Transactional
public class MessagePropertyDaoIT extends AbstractIT {

    @Autowired
    private MessagePropertyDao propertyDao;

    @Test
    public void testFindPropertyByNameValueAndType() {
        final String name = "prop1";
        final String value = "value1";
        final String type = "string";
        MessageProperty property = buildProperty(name, value, type);
        propertyDao.create(property);

        final MessageProperty foundProperty = propertyDao.findOrCreateProperty(name, value, type);
        assertNotNull(foundProperty);

        Assert.assertEquals(property.getEntityId(), foundProperty.getEntityId());
        Assert.assertEquals(name, foundProperty.getName());
        Assert.assertEquals(value, foundProperty.getValue());
        Assert.assertEquals(type, foundProperty.getType());

        final MessageProperty foundProperty1 = propertyDao.findOrCreateProperty(name, value, type);

        Assert.assertEquals(foundProperty.getEntityId(), foundProperty1.getEntityId());
    }

    @Test
    public void testFindPropertyByNameAndValue() {
        final String name = "prop1";
        final String value = "value1";
        final String type = null;
        MessageProperty property = buildProperty(name, value, type);
        propertyDao.create(property);

        final MessageProperty foundProperty = propertyDao.findOrCreateProperty(name, value, type);
        assertNotNull(foundProperty);

        Assert.assertEquals(property.getEntityId(), foundProperty.getEntityId());
        Assert.assertEquals(name, foundProperty.getName());
        Assert.assertEquals(value, foundProperty.getValue());
        Assert.assertEquals(type, foundProperty.getType());

        final MessageProperty foundProperty1 = propertyDao.findOrCreateProperty(name, value, type);

        Assert.assertEquals(foundProperty.getEntityId(), foundProperty1.getEntityId());
    }


    @Test
    public void testFindOrCreate() {
        final String name = "name1";
        final String value = "value1";

        final MessageProperty foundEntity1 = propertyDao.findOrCreateProperty(name, value, "  ");
        assertNotNull(foundEntity1);

        final MessageProperty foundEntity2 = propertyDao.findOrCreateProperty(name, value, "");
        assertNotNull(foundEntity2);

        final MessageProperty foundEntity3 = propertyDao.findOrCreateProperty(name, value, null);
        assertNotNull(foundEntity3);

        final MessageProperty foundEntity4 = propertyDao.findOrCreateProperty(name, value, "type1");
        assertNotNull(foundEntity4);

        Assert.assertEquals(foundEntity1.getEntityId(), foundEntity2.getEntityId());
        Assert.assertEquals(foundEntity1.getEntityId(), foundEntity3.getEntityId());
        Assert.assertNotEquals(foundEntity1.getEntityId(), foundEntity4.getEntityId());
    }


    private MessageProperty buildProperty(String name, String value, String type) {
        MessageProperty property = new MessageProperty();
        property.setName(name);
        property.setValue(value);
        property.setType(type);
        return property;
    }
}