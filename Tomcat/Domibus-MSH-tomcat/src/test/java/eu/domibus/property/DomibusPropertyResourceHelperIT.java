package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.core.property.DomibusPropertyResourceHelperImpl;
import eu.domibus.core.property.DomibusPropertiesFilter;
import eu.domibus.core.property.GlobalPropertyMetadataManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DomibusPropertyResourceHelperIT extends AbstractIT {

    @Autowired
    DomibusPropertyResourceHelperImpl configurationPropertyResourceHelper;

    @Autowired
    private GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Test
    public void setProperty_readonly() {
        boolean isDomain = true;
        String propertyValue = "100";

        try {
            configurationPropertyResourceHelper.setPropertyValue(DOMIBUS_DISPATCHER_TIMEOUT, isDomain, propertyValue);
            Assert.fail();
        } catch (DomibusPropertyException ex) {
            Assert.assertTrue(ex.getMessage().contains("it is not writable"));
        }
    }

    @Ignore
    @Test
    public void setProperty_nonexistent() {
        String propertyName = "non-existent-property-test";
        boolean isDomain = true;
        String propertyValue = "100";

        try {
            configurationPropertyResourceHelper.setPropertyValue(propertyName, isDomain, propertyValue);
            Assert.fail();
        } catch (DomibusPropertyException ex) {
            Assert.assertTrue(ex.getMessage().contains("it does not exist"));
        }
    }

    @Test
    public void setProperty_composable() {
        String propertyName = "composable_property_name";
        boolean isDomain = false;
        DomibusPropertyMetadata propertyMetadata = DomibusPropertyMetadata.getGlobalProperty(propertyName);
        propertyMetadata.setComposable(true);
        String propertyValue = "100";
        globalPropertyMetadataManager.getAllProperties().put(propertyName, propertyMetadata);

        DomibusPropertiesFilter filter = new DomibusPropertiesFilter();
        filter.setName(propertyName);
        filter.setShowDomain(isDomain);

        try {
            configurationPropertyResourceHelper.setPropertyValue(propertyName, isDomain, propertyValue);
            Assert.fail();
        } catch (DomibusPropertyException ex) {
            Assert.assertTrue(ex.getMessage().contains("You can only set its nested properties"));

            List<DomibusProperty> result2 = configurationPropertyResourceHelper.getAllProperties(filter);
            Assert.assertTrue(1 <= result2.size());
        }
    }

    @Test
    public void testSetProperty() {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        boolean isDomain = true;
        String propertyValue = "val1";

        DomibusProperty initial = configurationPropertyResourceHelper.getProperty(propertyName);

        configurationPropertyResourceHelper.setPropertyValue(propertyName, isDomain, propertyValue);

        DomibusProperty result = configurationPropertyResourceHelper.getProperty(propertyName);

        Assert.assertEquals(propertyName, result.getMetadata().getName());
        Assert.assertEquals(propertyValue, result.getValue());

        configurationPropertyResourceHelper.setPropertyValue(propertyName, isDomain, initial.getValue());
    }

    /**
     * tests adding a nested property: checking that is is added with the correct name
     */
    @Test
    public void setProperty_nested() {
        String composablePropertyName = "composable_property_nested";
        String nestedPropertyName = composablePropertyName + ".prop1";
        boolean isDomain = true;
        DomibusPropertyMetadata propertyMetadata = DomibusPropertyMetadata.getGlobalProperty(composablePropertyName);
        propertyMetadata.setComposable(true);
        String propertyValue = "100";

        DomibusPropertiesFilter filter = new DomibusPropertiesFilter();
        filter.setName(composablePropertyName);
        filter.setShowDomain(isDomain);
        filter.setOrderBy("name");
        filter.setAsc(true);

        globalPropertyMetadataManager.getAllProperties().put(composablePropertyName, propertyMetadata);

        configurationPropertyResourceHelper.setPropertyValue(nestedPropertyName, isDomain, propertyValue);

        DomibusProperty result = configurationPropertyResourceHelper.getProperty(nestedPropertyName);

        Assert.assertEquals(nestedPropertyName, result.getMetadata().getName());
        Assert.assertEquals(propertyValue, result.getValue());

        List<DomibusProperty> result2 = configurationPropertyResourceHelper.getAllProperties(filter);
        Assert.assertEquals(2, result2.size());
        Assert.assertEquals(composablePropertyName, result2.get(0).getMetadata().getName());
        Assert.assertEquals(nestedPropertyName, result2.get(1).getMetadata().getName());
    }

    @Test
    public void getProperty_nested_notfound() {
        String composablePropertyName = "composable_property_inexistent";
        //TODO EDELIVERY-7553 if we use "composable_property1" here the test
        // will fail while running at package or project level but pass at class level
        //so we need to check if is not related also to @Cacheable
        String nestedPropertyName = composablePropertyName + ".prop1";
        DomibusPropertyMetadata propertyMetadata = DomibusPropertyMetadata.getGlobalProperty(composablePropertyName);
        propertyMetadata.setComposable(true);
        globalPropertyMetadataManager.getAllProperties().put(composablePropertyName, propertyMetadata);

        try {
            DomibusProperty result = configurationPropertyResourceHelper.getProperty(nestedPropertyName);
            Assert.fail();
        } catch (DomibusPropertyException ex) {
            Assert.assertTrue(ex.getMessage().contains("Unknown property"));
        }
    }

    @Test
    public void testGetPropertyWithValidValue() {
        DomibusProperty result = configurationPropertyResourceHelper.getProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_FORMAT_SQL);
        Assert.assertEquals(result.getUsedValue(), result.getValue());
    }

    @Test
    public void testGetPropertyWithInvalidValue() {
        DomibusProperty result = configurationPropertyResourceHelper.getProperty(DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE);
        Assert.assertNotEquals(result.getUsedValue(), result.getValue());
    }

    @Test
    public void testGetPropertyDefaultInvalidValue() {
        DomibusProperty result = configurationPropertyResourceHelper.getProperty(DOMIBUS_PROXY_HTTP_PORT);
        Assert.assertEquals(StringUtils.EMPTY, result.getValue());
        Assert.assertEquals(StringUtils.EMPTY, result.getUsedValue());
    }
}
