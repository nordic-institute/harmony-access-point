//package eu.domibus.core.property;
//
//import eu.domibus.api.multitenancy.Domain;
//import eu.domibus.api.multitenancy.DomainContextProvider;
//import eu.domibus.api.multitenancy.DomainTaskExecutor;
//import eu.domibus.api.property.DomibusConfigurationService;
//import eu.domibus.api.property.DomibusProperty;
//import eu.domibus.api.property.DomibusPropertyException;
//import eu.domibus.api.property.DomibusPropertyMetadata;
//import eu.domibus.api.security.AuthUtils;
//import eu.domibus.api.util.ClassUtil;
//import eu.domibus.ext.delegate.converter.DomainExtConverter;
//import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
//import eu.domibus.ext.services.DomibusPropertyManagerExt;
//import mockit.*;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Spy;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;
//
//public class ConfigurationPropertyServiceImplTest {
//
//    @Injectable
//    protected DomainExtConverter domainConverter;
//
//    @Injectable
//    protected DomainContextProvider domainContextProvider;
//
//    @Tested
//    ConfigurationPropertyServiceImpl configurationPropertyService;
//
//    @Injectable
//    protected DomibusConfigurationService domibusConfigurationService;
//
//    @Injectable
//    private List<DomibusPropertyManagerExt> propertyManagers;
//
//    @Injectable
//    private AuthUtils authUtils;
//
//    @Injectable
//    private DomainTaskExecutor domainTaskExecutor;
//
//    @Injectable
//    ClassUtil classUtil;
//
//    @Mocked
//    @Spy
//    private DomibusPropertyManagerExt propertyManager1;
//
//    @Mocked
//    private DomibusPropertyManagerExt propertyManager2;
//
//    Map<String, DomibusPropertyMetadataDTO> props1, props2;
//    String domainCode = "domain1";
//    Domain domain = new Domain(domainCode, "DomainName1");
//
//    @Before
//    public void setUp() {
//        props1 = Arrays.stream(new DomibusPropertyMetadataDTO[]{
//                new DomibusPropertyMetadataDTO(DOMIBUS_UI_TITLE_NAME, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
//                new DomibusPropertyMetadataDTO(DOMIBUS_UI_REPLICATION_ENABLED, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
//                new DomibusPropertyMetadataDTO(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, DomibusPropertyMetadataDTO.Usage.DOMAIN, false),
//                new DomibusPropertyMetadataDTO(DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
//        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
//
//        props2 = Arrays.stream(new DomibusPropertyMetadataDTO[]{
//                new DomibusPropertyMetadataDTO(DOMIBUS_UI_SUPPORT_TEAM_NAME, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
//                new DomibusPropertyMetadataDTO(DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
//                new DomibusPropertyMetadataDTO(DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
//                new DomibusPropertyMetadataDTO(DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
//                new DomibusPropertyMetadataDTO(DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
//        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
//
//        propertyManagers = Arrays.asList(propertyManager1, propertyManager2);
//    }
//
//    DomibusPropertyMetadata convert(DomibusPropertyMetadataDTO meta) {
//        return new DomibusPropertyMetadata(meta.getName(), meta.getModule(), meta.isWritable(), meta.getUsage(),
//                meta.isWithFallback(), meta.isClusterAware(), meta.isEncrypted(), meta.isComposable());
//    }
//
//    @Test
//    public void getProperties() {
//        new Expectations(configurationPropertyService) {{
//
//            propertyManager1.getKnownProperties();
//            result = props1;
//
//            propertyManager2.getKnownProperties();
//            result = props2;
//
//            domibusConfigurationService.isMultiTenantAware();
//            result = true;
//
//            propertyManager1.getKnownPropertyValue(DOMIBUS_UI_TITLE_NAME);
//            result = "val1";
//
//            domainConverter.convert(props1.get(DOMIBUS_UI_TITLE_NAME), DomibusPropertyMetadata.class);
//            result = convert(props1.get(DOMIBUS_UI_TITLE_NAME));
//
//            propertyManager1.getKnownPropertyValue(DOMIBUS_UI_REPLICATION_ENABLED);
//            result = "val2";
//
//            domainConverter.convert(props1.get(DOMIBUS_UI_REPLICATION_ENABLED), DomibusPropertyMetadata.class);
//            result = convert(props1.get(DOMIBUS_UI_REPLICATION_ENABLED));
//
//            propertyManager2.getKnownPropertyValue(DOMIBUS_UI_SUPPORT_TEAM_NAME);
//            result = "val3";
//
//            domainConverter.convert(props2.get(DOMIBUS_UI_SUPPORT_TEAM_NAME), DomibusPropertyMetadata.class);
//            result = convert(props2.get(DOMIBUS_UI_SUPPORT_TEAM_NAME));
//
//            classUtil.isMethodDefined((DomibusPropertyManagerExt) any, "getKnownPropertyValue", new Class[]{String.class});
//            result = true;
//        }};
//
//        List<DomibusProperty> actual = configurationPropertyService.getAllWritableProperties("domibus.UI", true);
//
//        Assert.assertEquals(3, actual.size());
//        Assert.assertEquals(true, actual.stream().anyMatch(el -> el.getMetadata().getName().equals(DOMIBUS_UI_TITLE_NAME)));
//        Assert.assertEquals(true, actual.stream().anyMatch(el -> el.getMetadata().getName().equals(DOMIBUS_UI_REPLICATION_ENABLED)));
//        Assert.assertEquals(true, actual.stream().anyMatch(el -> el.getMetadata().getName().equals(DOMIBUS_UI_SUPPORT_TEAM_NAME)));
//        Assert.assertEquals("val1", actual.stream().filter(el -> el.getMetadata().getName().equals(DOMIBUS_UI_TITLE_NAME)).findFirst().get().getValue());
//    }
//
//    @Test
//    public void setPropertyValue() {
//        new Expectations(configurationPropertyService) {{
//            configurationPropertyService.getManagerForProperty(DOMIBUS_UI_TITLE_NAME);
//            result = propertyManager1;
//            propertyManager1.getKnownProperties();
//            result = props1;
//            classUtil.isMethodDefined((DomibusPropertyManagerExt) any, "setKnownPropertyValue", new Class[]{String.class, String.class});
//            result = true;
//        }};
//
//        configurationPropertyService.setPropertyValue(DOMIBUS_UI_TITLE_NAME, true, "val11");
//
//        new Verifications() {{
//            propertyManager1.setKnownPropertyValue(DOMIBUS_UI_TITLE_NAME, "val11");
//        }};
//    }
//
//    @Test
//    public void getManagerForProperty() {
//        new Expectations(configurationPropertyService) {{
//            propertyManager1.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);
//            result = false;
//            propertyManager2.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);
//            result = true;
//        }};
//
//        DomibusPropertyManagerExt manager = configurationPropertyService.getManagerForProperty(DOMIBUS_UI_TITLE_NAME);
//
//        Assert.assertEquals(propertyManager2, manager);
//    }
//
//    @Test(expected = DomibusPropertyException.class)
//    public void setPropertyValue_notMatch() {
//        new Expectations() {{
//            propertyManager1.hasKnownProperty("non_existing_prop");
//            result = false;
//
//            propertyManager2.hasKnownProperty("non_existing_prop");
//            result = false;
//        }};
//
//        configurationPropertyService.setPropertyValue("non_existing_prop", true, "val11");
//    }
//
//    @Test
//    public void validatePropertyValue_noValidation(@Mocked DomibusPropertyMetadataDTO propMeta) {
//        new Expectations(configurationPropertyService) {{
//            propMeta.getType();
//            returns("NON_EXISTING", "STRING");
//        }};
//
//        try {
//            configurationPropertyService.validatePropertyValue(propMeta, "doesn't matter");
//            configurationPropertyService.validatePropertyValue(propMeta, "doesn't matter");
//        } catch (DomibusPropertyException ex) {
//            Assert.fail();
//        }
//    }
//
//    @Test
//    public void validatePropertyValue_success(@Mocked DomibusPropertyMetadataDTO propMeta) {
//        new Expectations(configurationPropertyService) {{
//            propMeta.getType();
//            returns("NUMERIC");
//        }};
//
//        try {
//            configurationPropertyService.validatePropertyValue(propMeta, "123");
//        } catch (DomibusPropertyException ex) {
//            Assert.fail();
//        }
//    }
//
//    @Test(expected = DomibusPropertyException.class)
//    public void validatePropertyValue_Invalid(@Mocked DomibusPropertyMetadataDTO propMeta) {
//        new Expectations(configurationPropertyService) {{
//            propMeta.getType();
//            result = "NUMERIC";
//        }};
//
//        configurationPropertyService.validatePropertyValue(propMeta, "non_numeric_value");
//    }
//

//}