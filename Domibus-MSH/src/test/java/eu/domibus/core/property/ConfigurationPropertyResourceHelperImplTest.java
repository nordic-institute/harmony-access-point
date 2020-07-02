package eu.domibus.core.property;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.*;
import eu.domibus.api.security.AuthUtils;
import mockit.*;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationPropertyResourceHelperImplTest {

    @Tested
    ConfigurationPropertyResourceHelperImpl configurationPropertyResourceHelper;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private AuthUtils authUtils;

    @Mock
    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Captor
    ArgumentCaptor argCaptor;

    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    Map<String, DomibusPropertyMetadata> props1, props2, allProps;
    List<DomibusPropertyMetadata> propertiesMetadataList;

    @Before
    public void setUp() {
        props1 = Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_ENABLED, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, DomibusPropertyMetadata.Usage.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN, DomibusPropertyMetadata.Usage.DOMAIN, true),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));

        props2 = Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST, DomibusPropertyMetadata.Usage.DOMAIN, true),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));

        allProps = new HashedMap();
        allProps.putAll(props1);
        allProps.putAll(props2);

        propertiesMetadataList = props1.values().stream().collect(Collectors.toList());
    }

    @Test
    public void getAllWritableProperties() {
        String name = "domibus.UI";
        Boolean showDomain = true;

        List<DomibusProperty> properties = propertiesMetadataList.stream().map(el -> {
            DomibusProperty res = new DomibusProperty();
            res.setMetadata(el);
            res.setValue("val1");
            return res;
        }).collect(Collectors.toList());

        new Expectations(configurationPropertyResourceHelper) {{
            globalPropertyMetadataManager.getAllProperties();
            result = allProps;
            configurationPropertyResourceHelper.filterProperties(allProps, name, showDomain, null, null);
            result = propertiesMetadataList;
            configurationPropertyResourceHelper.createProperties(propertiesMetadataList);
            result = properties;
        }};

        List<DomibusProperty> actual = configurationPropertyResourceHelper.getAllWritableProperties(name, showDomain, null, null);

        Assert.assertEquals(4, actual.size());
        Assert.assertEquals(true, actual.stream().anyMatch(el -> el.getMetadata().getName().equals(DOMIBUS_UI_TITLE_NAME)));
        Assert.assertEquals(true, actual.stream().anyMatch(el -> el.getMetadata().getName().equals(DOMIBUS_UI_REPLICATION_ENABLED)));
        Assert.assertEquals("val1", actual.stream().filter(el -> el.getMetadata().getName().equals(DOMIBUS_UI_TITLE_NAME)).findFirst().get().getValue());
    }

    @Test
    public void setPropertyValue() {
        String name = DOMIBUS_UI_TITLE_NAME;
        boolean isDomain = true;
        String value = "propValue";

        configurationPropertyResourceHelper.setPropertyValue(name, isDomain, value);

        new Verifications() {{
            domibusPropertyProvider.setProperty(name, value);
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setPropertyValue_error() {
        String name = DOMIBUS_UI_TITLE_NAME;
        boolean isDomain = false;
        String value = "propValue";

        new Expectations(configurationPropertyResourceHelper) {{
            authUtils.isSuperAdmin();
            result = false;
        }};

        configurationPropertyResourceHelper.setPropertyValue(name, isDomain, value);

        new Verifications() {{
            domibusPropertyProvider.setProperty(name, value);
            times = 0;
        }};
    }

    @Test
    public void setPropertyValue_global() throws Exception {
        String name = "some.global.property";
        boolean isDomain = false;
        String value = "propValue";

        new Expectations(configurationPropertyResourceHelper) {{
            authUtils.isSuperAdmin();
            result = true;
        }};

        configurationPropertyResourceHelper.setPropertyValue(name, isDomain, value);
        mockExecutorSubmit_void();

        new Verifications() {{
            domibusPropertyProvider.setProperty(name, value);
            times = 1;
        }};
    }

    @Test
    public void createProperties() {
        new Expectations(configurationPropertyResourceHelper) {{
            domibusPropertyProvider.getProperty(propertiesMetadataList.get(0).getName());
            result = "val1";
        }};

        List<DomibusProperty> actual = configurationPropertyResourceHelper.createProperties(propertiesMetadataList);

        Assert.assertEquals(4, actual.size());
        Assert.assertEquals("val1", actual.get(0).getValue());

        new Verifications() {{
            domibusPropertyProvider.getProperty(propertiesMetadataList.get(0).getName());
            domibusPropertyProvider.getProperty(propertiesMetadataList.get(1).getName());
            domibusPropertyProvider.getProperty(propertiesMetadataList.get(2).getName());
            domibusPropertyProvider.getProperty(propertiesMetadataList.get(3).getName());
        }};
    }

    @Test
    public void filterProperties() {
        String name = "domibus.UI";
        Boolean showDomain = true;

        new Expectations(configurationPropertyResourceHelper) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
        }};

        List<DomibusPropertyMetadata> actual = configurationPropertyResourceHelper.filterProperties(props1, name, showDomain, null, null);

        Assert.assertEquals(2, actual.size());
        Assert.assertTrue(actual.stream().anyMatch(el -> el.getName().equals(DOMIBUS_UI_TITLE_NAME)));
    }

    @Test
    public void filterProperties_singleTenancy() {
        String name = "domibus.UI";
        Boolean showDomain = false;

        new Expectations(configurationPropertyResourceHelper) {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;
        }};

        List<DomibusPropertyMetadata> actual = configurationPropertyResourceHelper.filterProperties(props1, name, showDomain, null, null);

        Assert.assertEquals(2, actual.size());
        Assert.assertTrue(actual.stream().anyMatch(el -> el.getName().equals(DOMIBUS_UI_TITLE_NAME)));
    }

    @Test
    public void getValueAndCreateProperty(@Mocked DomibusPropertyMetadata propMeta) {
        String propertyValue = "prop value";

        new Expectations(configurationPropertyResourceHelper) {{
            propMeta.isDomain();
            result = true;
            domibusPropertyProvider.getProperty(propMeta.getName());
            result = propertyValue;
        }};

        DomibusProperty actual = configurationPropertyResourceHelper.getValueAndCreateProperty(propMeta);

        Assert.assertNotNull(actual);
        Assert.assertEquals(propertyValue, actual.getValue());
    }

    @Test
    public void getPropertyValue_global(@Mocked DomibusPropertyMetadata propMeta) throws Exception {
        String expectedValue = "my val";
        new Expectations(configurationPropertyResourceHelper) {{
            propMeta.isDomain();
            result = false;
            propMeta.getName();
            result = "my.global.prop.name";
            domibusPropertyProvider.getProperty(propMeta.getName());
            result = expectedValue;
        }};

        configurationPropertyResourceHelper.getPropertyValue(propMeta);
        String returnedValue = mockExecutorSubmit();

        Assert.assertEquals(expectedValue, returnedValue);
    }

    private <T> T mockExecutorSubmit() throws Exception {
        Mockito.verify(domainTaskExecutor).submit((Callable) argCaptor.capture());
        Callable<T> callable = (Callable<T>) argCaptor.getValue();
        return callable.call();
    }

    private void mockExecutorSubmit_void() {
        Mockito.verify(domainTaskExecutor).submit((Runnable) argCaptor.capture());
        Runnable runnable = (Runnable) argCaptor.getValue();
        runnable.run();
    }

}