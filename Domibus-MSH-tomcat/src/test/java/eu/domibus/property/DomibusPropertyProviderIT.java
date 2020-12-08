package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.core.property.DomibusPropertyProviderImpl;
import eu.domibus.core.property.GlobalPropertyMetadataManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.property.ExternalTestModulePropertyManager.*;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class DomibusPropertyProviderIT extends AbstractIT {

    @Autowired
    DomibusPropertyProviderImpl domibusPropertyProvider;

    @Autowired
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Autowired
    DomainContextProvider domainContextProvider;

    @Test()
    public void getPropertyValue_non_existing() {
        String propName = EXTERNAL_NOT_EXISTENT;
        DomibusPropertyMetadata result = globalPropertyMetadataManager.getPropertyMetadata(propName);
        Assert.assertEquals(DomibusPropertyMetadata.Usage.ANY.getValue(), result.getUsage());
    }

    @Test
    public void getPropertyValue_existing_storedLocally_notHandled() {
        String propName = EXTERNAL_MODULE_EXISTENT_NOT_HANDLED;
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        String propValue = "true";

        String result = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(null, result);

        domibusPropertyProvider.setProperty(currentDomain, propName, propValue);
        String result2 = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(null, result2);
    }

    @Test
    public void getPropertyValue_existing_storedLocally_handled() {
        String propName = EXTERNAL_MODULE_EXISTENT_LOCALLY_HANDLED;
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        String propValue = propName + ".value";

        String result = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(propValue, result);

        String newValue = "newValue";
        domibusPropertyProvider.setProperty(currentDomain, propName, newValue);
        String result2 = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(newValue, result2);
    }

    @Test
    public void getPropertyValue_existing_storedGlobally() {
        String propName = EXTERNAL_MODULE_EXISTENT_GLOBALLY_HANDLED;
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        String propValue = null;

        String result = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(propValue, result);

        String newValue = "newValue";
        domibusPropertyProvider.setProperty(currentDomain, propName, newValue);
        String result2 = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(newValue, result2);
    }
}
