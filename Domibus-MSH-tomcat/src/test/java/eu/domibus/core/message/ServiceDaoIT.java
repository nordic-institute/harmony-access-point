package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.ServiceEntity;
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
public class ServiceDaoIT extends AbstractIT {

    @Autowired
    private ServiceDao serviceDao;

    @Test
    public void testFindPropertyByNameValueAndType() {
        final String value = "value1";
        final String type = "string";
        ServiceEntity property = buildProperty(value, type);
        serviceDao.create(property);

        final ServiceEntity foundProperty = serviceDao.findOrCreateService(value, type);
        assertNotNull(foundProperty);

        Assert.assertEquals(property.getEntityId(), foundProperty.getEntityId());
        Assert.assertEquals(value, foundProperty.getValue());
        Assert.assertEquals(type, foundProperty.getType());

        final ServiceEntity foundProperty1 = serviceDao.findOrCreateService(value, type);

        Assert.assertEquals(foundProperty.getEntityId(), foundProperty1.getEntityId());
    }

    @Test
    public void testFindPropertyByNameAndValue() {
        final String value = "value1";
        final String type = null;
        ServiceEntity property = buildProperty(value, type);
        serviceDao.create(property);

        final ServiceEntity foundProperty = serviceDao.findOrCreateService(value, type);
        assertNotNull(foundProperty);

        Assert.assertEquals(property.getEntityId(), foundProperty.getEntityId());
        Assert.assertEquals(value, foundProperty.getValue());
        Assert.assertEquals(type, foundProperty.getType());

        final ServiceEntity foundProperty1 = serviceDao.findOrCreateService(value, type);

        Assert.assertEquals(foundProperty.getEntityId(), foundProperty1.getEntityId());
    }

    private ServiceEntity buildProperty(String value, String type) {
        ServiceEntity property = new ServiceEntity();
        property.setValue(value);
        property.setType(type);
        return property;
    }
}