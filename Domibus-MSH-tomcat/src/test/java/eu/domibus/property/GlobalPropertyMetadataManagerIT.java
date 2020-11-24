package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.core.property.GlobalPropertyMetadataManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_TITLE_NAME;

public class GlobalPropertyMetadataManagerIT extends AbstractIT {

    @Autowired
    private GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Test
    public void hasKnownProperty_nonexistent() {
        String propertyName = "non-existent-property";

        boolean res = globalPropertyMetadataManager.hasKnownProperty(propertyName);
        Assert.assertFalse(res);
    }

    @Test
    public void hasKnownProperty_existent() {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        boolean res = globalPropertyMetadataManager.hasKnownProperty(propertyName);
        Assert.assertTrue(res);
    }

    @Test
    public void getPropertyMetadata_nonexistent_createOnTheFly() {
        String propertyName = "non-existent-property";

        DomibusPropertyMetadata res = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        Assert.assertNotNull(res);
        Assert.assertEquals(propertyName, res.getName());
        Assert.assertEquals(DomibusPropertyMetadata.Usage.ANY.getValue(), res.getUsage());
    }

    @Test
    public void getPropertyMetadata_existent() {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        DomibusPropertyMetadata res = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        Assert.assertNotNull(res);
        Assert.assertEquals(propertyName, res.getName());
    }

    @Test
    public void getProperty_createNested() {
        String composablePropertyName = "composable_property";
        String nestedPropertyName = composablePropertyName + ".prop1";
        DomibusPropertyMetadata propertyMetadata = DomibusPropertyMetadata.getGlobalProperty(composablePropertyName);
        propertyMetadata.setComposable(true);
        globalPropertyMetadataManager.getAllProperties().put(composablePropertyName, propertyMetadata);

        DomibusPropertyMetadata res = globalPropertyMetadataManager.getPropertyMetadata(nestedPropertyName);

        Assert.assertNotNull(res);
        Assert.assertEquals(nestedPropertyName, res.getName());
        Assert.assertEquals(false, res.isComposable());
    }

    @Test
    public void getComposableProperty_createNested() {
        String composablePropertyName = "composable_property";
        String nestedPropertyName = composablePropertyName + ".prop1";
        DomibusPropertyMetadata propertyMetadata = DomibusPropertyMetadata.getGlobalProperty(composablePropertyName);
        propertyMetadata.setComposable(true);
        globalPropertyMetadataManager.getAllProperties().put(composablePropertyName, propertyMetadata);

        DomibusPropertyMetadata res = globalPropertyMetadataManager.getComposableProperty(nestedPropertyName);

        Assert.assertNotNull(res);
        Assert.assertEquals(composablePropertyName, res.getName());
        Assert.assertEquals(true, res.isComposable());
    }

    @Test
    public void hasKnownProperty_composable() {
        String propertyName = DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC + ".MPC_NAME";

        boolean res = globalPropertyMetadataManager.hasKnownProperty(propertyName);
        Assert.assertTrue(res);
    }
}
