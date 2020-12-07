package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.core.property.ConfigurationPropertyResourceHelperImpl;
import eu.domibus.core.property.DomibusPropertiesFilter;
import eu.domibus.core.property.GlobalPropertyMetadataManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_TIMEOUT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_TITLE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class ConfigurationPropertyResourceHelperIT extends AbstractIT {

    @Autowired
    ConfigurationPropertyResourceHelperImpl configurationPropertyResourceHelper;

    @Autowired
    private GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Test
    public void setProperty_readonly() {
        String propertyName = DOMIBUS_DISPATCHER_TIMEOUT;
        boolean isDomain = true;
        String propertyValue = "100";

        try {
            configurationPropertyResourceHelper.setPropertyValue(propertyName, isDomain, propertyValue);
            Assert.fail();
        } catch (DomibusPropertyException ex) {
            Assert.assertTrue(ex.getMessage().contains("it is not writable"));
        }
    }

    @Test
    public void setProperty_nonexistent() {
        String propertyName = "non-existent-property";
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
        String propertyName = "composable_property";
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
            Assert.assertEquals(1, result2.size());
        }
    }

    @Test
    public void testSetProperty() {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        boolean isDomain = true;
        String propertyValue = "val1";

        configurationPropertyResourceHelper.setPropertyValue(propertyName, isDomain, propertyValue);

        DomibusProperty result = configurationPropertyResourceHelper.getProperty(propertyName);

        Assert.assertEquals(propertyName, result.getMetadata().getName());
        Assert.assertEquals(propertyValue, result.getValue());
    }

    /**
     * tests adding a nested property: checking that is is added with the correct name
     */
    @Test
    public void setProperty_nested() {
        String composablePropertyName = "composable_property";
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
        String composablePropertyName = "composable_property2";
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
}
