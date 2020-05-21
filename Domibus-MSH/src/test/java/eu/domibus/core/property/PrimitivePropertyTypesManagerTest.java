package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class PrimitivePropertyTypesManagerTest {
    @Tested
    PrimitivePropertyTypesManager primitivePropertyTypesManager;

    @Injectable
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Injectable
    @Qualifier("domibusDefaultProperties")
    protected Properties domibusDefaultProperties;

    @Injectable
    protected PropertyResolver propertyResolver;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    ConfigurableEnvironment environment;

    @Injectable
    DomibusPropertyProviderDispatcher domibusPropertyProviderDispatcher;

    @Injectable
    DomibusPropertyProviderImpl domibusPropertyProvider;

    private String propertyName = "domibus.property.name";
    private String propertyValue = "domibus.property.value";
    private Domain domain = new Domain("domain1", "Domain 1");

//    @Test()
//    public void getLongProperty() {
//        String val = "2";
//        new Expectations(domibusPropertyProvider) {{
//            domibusPropertyProvider.getProperty(propertyName);
//            result = val;
//        }};
//
//        Long res = domibusPropertyProvider.getLongProperty(propertyName);
//
//        assertEquals(Long.valueOf(val), res);
//    }
}
